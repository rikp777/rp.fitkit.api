package rp.fitkit.api.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.audit.AuditLogDto;
import rp.fitkit.api.mapper.AuditMapper;
import rp.fitkit.api.model.audit.AuditAction;
import rp.fitkit.api.model.audit.AuditLog;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.repository.audit.AuditLogRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    /**
     * Fetches a paginated view of the audit history for a given user.
     * <p>
     * This method applies access restrictions based on the user's subscription tier. Normal (non-premium)
     * users are restricted to accessing only the first 500 audit records.
     *
     * @param user     The user whose audit history is to be fetched.
     * @param pageable The pagination and sorting information for the request.
     * @return A {@link Mono} emitting a {@link Page} of {@link AuditLogDto} objects.
     * The returned Mono will emit an error with {@link HttpStatus#FORBIDDEN} if a non-premium
     * user attempts to access records beyond their permitted limit.
     */
    public Mono<Page<AuditLogDto>> getAuditHistoryForUser(User user, Pageable pageable) {
        boolean isPremium = user.isPremium();
        log.debug("User {} premium status: {}", user.getId(), isPremium);

        if (!isPremium && pageable.getOffset() >= 500) {
            log.warn("Access forbidden for normal user {} requesting offset {}", user.getId(), pageable.getOffset());
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Normal users can only access the first 500 audit records."));
        }

        return auditLogRepository.countBySubjectId(user.getId())
                .flatMap(total -> {
                    log.debug("Found total of {} records for user {}", total, user.getId());
                    long displayTotal = isPremium ? total : Math.min(total, 500L);

                    return auditLogRepository.findBySubjectId(user.getId(), pageable)
                            .map(auditMapper::toDto) // Gebruik de mapper
                            .collectList()
                            .map(list -> (Page<AuditLogDto>) new PageImpl<>(list, pageable, displayTotal));
                });
    }

    /**
     * Fetches a paginated view of the audit history for a given user on a specific date.
     * Access restrictions for non-premium users are applied.
     *
     * @param user     The user whose audit history is to be fetched.
     * @param date     The specific date for which to fetch the audit history.
     * @param pageable The pagination and sorting information.
     * @return A {@link Mono} emitting a {@link Page} of {@link AuditLogDto} objects.
     */
    public Mono<Page<AuditLogDto>> getAuditHistoryForUserByDate(User user, LocalDate date, Pageable pageable) {
        boolean isPremium = user.isPremium();
        log.debug("User {} (premium: {}) requesting audit history for date {}", user.getId(), isPremium, date);

        if (!isPremium && pageable.getOffset() >= 500) {
            log.warn("Access forbidden for normal user {} requesting offset {}", user.getId(), pageable.getOffset());
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Normal users can only access the first 500 audit records."));
        }

        var startOfDay = date.atStartOfDay();
        var endOfDay = date.atTime(LocalTime.MAX);

        return auditLogRepository.countBySubjectIdAndTimestampBetween(user.getId(), startOfDay, endOfDay)
                .flatMap(total -> {
                    log.debug("Found total of {} audit records for user {} on date {}", total, user.getId(), date);
                    long displayTotal = isPremium ? total : Math.min(total, 500L);

                    return auditLogRepository.findBySubjectIdAndTimestampBetween(user.getId(), startOfDay, endOfDay, pageable)
                            .map(auditMapper::toDto)
                            .collectList()
                            .map(list -> new PageImpl<>(list, pageable, displayTotal));
                });
    }

    /**
     * Logs an action performed by a user on their own data.
     * <p>
     * This is a convenience method where the actor and the subject of the action are the same user.
     *
     * @param <T>         The type of the object to be passed through the reactive chain.
     * @param user        The user performing the action (both actor and subject).
     * @param action      The {@link AuditAction} being performed (e.g., CREATE, UPDATE).
     * @param entityType  The simple name of the class of the entity being affected (e.g., "Workout").
     * @param entityId    The unique identifier of the entity instance being affected.
     * @param passThrough The object to be returned in the Mono upon successful logging. This allows
     * the method to be used in a reactive chain without breaking the flow.
     * @return A {@link Mono} that completes the logging and then emits the {@code passThrough} object.
     */
    public <T> Mono<T> logUserAction(User user, AuditAction action, String entityType, String entityId, T passThrough) {
        return log(user, action, user, entityType, entityId, null)
                .thenReturn(passThrough);
    }

    /**
     * Logs an action performed by an administrator on a subject user's data.
     * <p>
     * The acting administrator is automatically retrieved from the current {@link SecurityContext}.
     *
     * @param <T>           The type of the object to be passed through the reactive chain.
     * @param justification The reason why the administrator is performing this action. Can be null.
     * @param action        The {@link AuditAction} being performed.
     * @param subject       The user whose data is being affected by the administrator's action.
     * @param entityType    The simple name of the class of the entity being affected.
     * @param entityId      The unique identifier of the entity instance being affected.
     * @param passThrough   The object to be returned in the Mono upon successful logging, for reactive chaining.
     * @return A {@link Mono} that completes the logging and then emits the {@code passThrough} object.
     */
    public <T> Mono<T> logAdminAction(String justification, AuditAction action, User subject, String entityType, String entityId, T passThrough) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (User) auth.getPrincipal())
                .flatMap(admin -> log(admin, action, subject, entityType, entityId, justification))
                .thenReturn(passThrough);
    }

    /**
     * Internal helper method to construct and save an {@link AuditLog} entity to the database.
     *
     * @param actor         The user performing the action.
     * @param action        The action being performed.
     * @param subject       The user whose data is being affected.
     * @param entityType    The type of the entity.
     * @param entityId      The ID of the entity.
     * @param justification The reason for the action (optional, typically for admins).
     * @return A {@link Mono} emitting the saved {@link AuditLog} entity.
     */
    private Mono<AuditLog> log(User actor, AuditAction action, User subject, String entityType, String entityId, String justification) {
        AuditLog entry = new AuditLog(actor, action, subject, entityType, entityId, justification);
        return auditLogRepository.save(entry)
                .doOnSuccess(saved -> log.info("AUDIT: Actor '{}' ({}) performed {} on {} targeting subject '{}'. Justification: {}",
                        actor.getUsername(),
                        entry.getActorRoles(),
                        action,
                        entityType,
                        subject.getUsername(),
                        justification));
    }
}

