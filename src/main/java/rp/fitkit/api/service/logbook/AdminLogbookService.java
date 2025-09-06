package rp.fitkit.api.service.logbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.repository.logbook.DailyLogRepository;
import rp.fitkit.api.repository.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLogbookService {
    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;

    public Mono<Page<DailyLog>> getAllDailyLogs(Pageable pageable) {
        log.info("Admin fetching all daily logs. Page: {}, Size: {}.", pageable.getPageNumber(), pageable.getPageSize());

        return this.dailyLogRepository.count()
                .doOnSuccess(total -> log.debug("Total daily logs in DB: {}", total))
                .flatMap(total ->
                        this.dailyLogRepository.findAll(pageable.getSort())
                                .skip(pageable.getOffset())
                                .take(pageable.getPageSize())
                                .collectList()
                                .doOnSuccess(list -> log.info("Returning page with {} of {} total daily logs.", list.size(), total))
                                .map(list -> new PageImpl<>(list, pageable, total))
                );
    }

    public Mono<Page<DailyLog>> searchDailyLogsByUsername(String username, Pageable pageable) {
        log.info("Admin searching for daily logs by username containing: '{}'. Page: {}, Size: {}", username, pageable.getPageNumber(), pageable.getPageSize());

        if (username == null || username.isBlank()) {
            log.warn("Search username is blank, returning empty page.");
            return Mono.just(Page.empty(pageable));
        }

        return userRepository.findByUsernameContainingIgnoreCase(username)
                .map(User::getId)
                .collectList()
                .doOnSuccess(userIds -> log.debug("Found {} user(s) matching search term '{}': {}", userIds.size(), username, userIds))
                .flatMap(userIds -> {
                    if (userIds.isEmpty()) {
                        log.info("No users found for search term: '{}'. Returning empty page.", username);
                        return Mono.just(Page.empty(pageable));
                    }

                    Mono<Long> totalMono = dailyLogRepository
                            .countByUserIdIn(userIds)
                            .doOnSuccess(count -> log.debug("Found a total of {} log(s) for the matched users.", count));

                    Mono<List<DailyLog>> contentMono = dailyLogRepository.findByUserIdIn(userIds, pageable.getSort())
                            .skip(pageable.getOffset())
                            .take(pageable.getPageSize())
                            .collectList();

                    return Mono.zip(contentMono, totalMono)
                            .doOnSuccess(tuple -> log.info("Returning page with {} of {} total search results for username '{}'", tuple.getT1().size(), tuple.getT2(), username))
                            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
                });
    }


    public Mono<Void> deleteDailyLog(Long logId) {
        log.info("Admin attempting to delete daily log with ID: {}", logId);

        return dailyLogRepository.findById(logId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Admin tried to delete non-existent log with ID: {}", logId);
                    return Mono.error(new ResourceNotFoundException("DailyLog not found with id: " + logId));
                }))
                .doOnSuccess(dailyLog -> log.debug("Found log to delete: {}. Proceeding with deletion.", dailyLog))
                .flatMap(dailyLogRepository::delete)
                .doOnSuccess(v -> log.info("Successfully deleted daily log with ID: {}", logId));
    }
}

