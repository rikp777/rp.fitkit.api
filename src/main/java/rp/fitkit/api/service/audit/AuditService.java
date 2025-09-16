package rp.fitkit.api.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.audit.AuditAction;
import rp.fitkit.api.model.audit.AuditLog;
import rp.fitkit.api.model.user.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Logs an action performed by the currently authenticated user on their own data.
     * The passThrough object is returned to allow chaining in a reactive stream.
     */
    public <T> Mono<T> logUserAction(AuditAction action, String entityType, String entityId, T passThrough) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (User) auth.getPrincipal())
                .flatMap(user -> log(user, action, user, entityType, entityId, null))
                .thenReturn(passThrough);
    }

    /**
     * Logs an action performed by an admin on another user's data.
     * The passThrough object is returned to allow chaining in a reactive stream.
     */
    public <T> Mono<T> logAdminAction(String justification, AuditAction action, User subject, String entityType, String entityId, T passThrough) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (User) auth.getPrincipal())
                .flatMap(admin -> log(admin, action, subject, entityType, entityId, justification))
                .thenReturn(passThrough);
    }

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

