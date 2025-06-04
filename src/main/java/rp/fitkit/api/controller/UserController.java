package rp.fitkit.api.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.UserLoginDto;
import rp.fitkit.api.dto.UserRegistrationDto;
import rp.fitkit.api.dto.UserResponseDto;
import rp.fitkit.api.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
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

    @PostMapping("/login")
    public Mono<ResponseEntity<UserResponseDto>> loginUser(
            @Valid @RequestBody UserLoginDto loginDto
    ) {
        return userService.loginUser(loginDto)
                .map(authenticatedUser -> {
                    UserResponseDto responseDto = new UserResponseDto(
                            authenticatedUser.getId(),
                            authenticatedUser.getUsername(),
                            authenticatedUser.getEmail(),
                            authenticatedUser.getDateJoined()
                    );
                    return ResponseEntity.ok(responseDto);
                });
    }
}
