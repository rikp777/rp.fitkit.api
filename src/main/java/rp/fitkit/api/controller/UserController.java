package rp.fitkit.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.*;
import rp.fitkit.api.dto.auth.*;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.user.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponseDto>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto
    ) {
        return userService.registerUser(registrationDto)
                .map(savedUser -> {

                    UserResponseDto responseDto = new UserResponseDto(
                            savedUser.getId(),
                            savedUser.getUsername(),
                            savedUser.getEmail(),
                            savedUser.getDateJoined()
                    );
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
                });
    }

    @PostMapping(
            path = "/login",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    public Mono<ResponseEntity<LoginResponseDto>> loginUser(
            @Valid UserLoginDto loginDto
    ) {
        return userService.loginUserAndGenerateToken(loginDto)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AccessTokenResponseDto>> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        return userService.refreshAccessToken(request.getRefreshToken())
                .map(newAccessToken -> ResponseEntity.ok(new AccessTokenResponseDto(newAccessToken, "Bearer")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<UserResponseDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = (User) userDetails;

        UserResponseDto responseDto = new UserResponseDto(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getDateJoined()
        );
        return Mono.just(responseDto);
    }
}
