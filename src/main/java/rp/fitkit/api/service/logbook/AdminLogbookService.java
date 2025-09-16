package rp.fitkit.api.service.logbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.audit.AuditAction;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.repository.logbook.DailyLogRepository;
import rp.fitkit.api.repository.user.UserRepository;
import rp.fitkit.api.service.audit.AuditService;
import rp.fitkit.api.service.audit.ConsentService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLogbookService {
    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;
    private final ConsentService consentService;
    private final AuditService auditService;


    public Mono<Page<DailyLog>> searchDailyLogsByUsername(String justification, String username, Pageable pageable) {
        log.info("Admin searching for daily logs for username: '{}' with justification '{}'", username, justification);

        return consentService.findAndValidateConsent(justification, username)
                .then(userRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found with username: " + username)))
                .flatMap(user -> {
                    Mono<Long> totalMono = dailyLogRepository.countByUserId(user.getId());
                    Mono<List<DailyLog>> contentMono = dailyLogRepository.findByUserId(user.getId(), pageable.getSort())
                            .skip(pageable.getOffset())
                            .take(pageable.getPageSize())
                            .collectList();

                    return Mono.zip(contentMono, totalMono)
                            .flatMap(tuple ->
                                    auditService.logAdminAction(
                                            justification,
                                            AuditAction.SEARCH,
                                            user,
                                            DailyLog.class.getSimpleName(),
                                            "multiple",
                                            tuple
                                    )
                            )
                            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
                });
    }

    public Mono<Void> deleteDailyLog(String justification, Long logId) {
        log.info("Admin attempting to delete daily log with ID: {} for justification: {}", logId, justification);

        return dailyLogRepository.findById(logId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("DailyLog not found with id: " + logId)))
                .flatMap(dailyLog ->
                        userRepository.findById(dailyLog.getUserId())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found for logId: " + logId)))
                                .flatMap(user ->
                                        // Validate consent for the user who owns the log.
                                        consentService.findAndValidateConsent(justification, user.getUsername())
                                                .then(
                                                        // Delete the log.
                                                        dailyLogRepository.delete(dailyLog)
                                                                .then(
                                                                        // Audit the action.
                                                                        auditService.logAdminAction(
                                                                                justification,
                                                                                AuditAction.DELETE,
                                                                                user,
                                                                                DailyLog.class.getSimpleName(),
                                                                                logId.toString(),
                                                                                Mono.empty()
                                                                        )
                                                                )
                                                )
                                )
                ).then();
    }
}


