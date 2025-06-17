package rp.fitkit.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

//Nodig want blijkbaar ben ik retarded
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/security")
    public Mono<String> testSecurityEndpoint(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("==> TEST ENDPOINT BEREIKT door gebruiker: {}", userDetails.getUsername());
        return Mono.just("Hello, " + userDetails.getUsername() + "! Je hebt succesvol toegang tot een beveiligd GET endpoint.");
    }

    //test endpoint no security
    @GetMapping("/no-security")
    public Mono<String> testNoSecurityEndpoint() {
        log.info("==> TEST ENDPOINT BEREIKT zonder beveiliging");
        return Mono.just("Hello! Je hebt succesvol toegang tot een onbeveiligd GET endpoint.");
    }
}
