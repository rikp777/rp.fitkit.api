package rp.fitkit.api.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.auth.*;
import rp.fitkit.api.exception.UserAlreadyExistsException;
import rp.fitkit.api.model.user.PasswordRecoveryCode;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.model.user.UserRole;
import rp.fitkit.api.repository.user.RecoveryCodeRepository;
import rp.fitkit.api.repository.user.UserRepository;
import rp.fitkit.api.repository.user.UserRoleRepository;
import rp.fitkit.api.util.JwtUtil;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RecoveryCodeRepository recoveryCodeRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RecoveryCodeRepository recoveryCodeRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Mono<User> registerUser(UserRegistrationDto registrationDto) {
        return userRepository.findByUsername(registrationDto.getUsername())
                .map(Optional::ofNullable)
                .defaultIfEmpty(Optional.empty())
                .flatMap(existingUserByUsername -> {
                    if (existingUserByUsername.isPresent()) {
                        return Mono.error(new UserAlreadyExistsException(
                                "Username '" + registrationDto.getUsername() + "' is al in gebruik."));
                    }
                    return userRepository.findByEmail(registrationDto.getEmail())
                            .map(Optional::ofNullable)
                            .defaultIfEmpty(Optional.empty());
                })
                .flatMap(existingUserByEmail -> {
                    if (existingUserByEmail.isPresent()) {
                        return Mono.error(new UserAlreadyExistsException(
                                "Email '" + registrationDto.getEmail() + "' is al geregistreerd."));
                    }
                    String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());

                    User newUser = new User(
                            registrationDto.getUsername(),
                            registrationDto.getEmail(),
                            hashedPassword
                    );

                    return userRepository.save(newUser) .flatMap(savedUser -> userRoleRepository
                            .save(new UserRole(savedUser.getId(), "ROLE_USER"))
                            .thenReturn(savedUser)
                    );
                });
    }

    /**
     * Verifieert de inloggegevens van een gebruiker.
     * @param loginDto De DTO met username/email en wachtwoord.
     * @return Een Mono die de User bevat bij succes, of een error Mono bij falen.
     */
    public Mono<LoginResponseDto> loginUserAndGenerateToken(UserLoginDto loginDto) {
        return userRepository.findByUsername(loginDto.getUsername())
                .switchIfEmpty(userRepository.findByEmail(loginDto.getUsername()))
                .flatMap(user -> userRoleRepository.findByUserId(user.getId())
                        .map(UserRole::getRoleName)
                        .map(SimpleGrantedAuthority::new)
                        .collectList()
                        .flatMap(authorities -> {
                            user.setAuthorities(authorities);

                            if (passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
                                String accessToken = jwtUtil.generateAccessToken(user);
                                String refreshToken = jwtUtil.generateRefreshToken(user);
                                UserResponseDto userDetails = new UserResponseDto(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getEmail(),
                                        user.getDateJoined()
                                );
                                return Mono.just(new LoginResponseDto(accessToken, refreshToken, "Bearer", userDetails));
                            } else {
                                return Mono.error(new BadCredentialsException("Ongeldige inloggegevens."));
                            }
                        }))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Gebruiker niet gevonden of ongeldige inloggegevens.")));
    }


    public Mono<String> refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            return Mono.error(new BadCredentialsException("Invalid refresh token."));
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new BadCredentialsException("User not found for refresh token.")))
                .map(jwtUtil::generateAccessToken);
    }

    @Transactional
    public Mono<List<String>> generateAndStoreRecoveryCodes(User user) {
        return recoveryCodeRepository.deleteAllByUserId(user.getId())
                .then(Mono.fromCallable(() -> {
                    List<String> plainTextCodes = new ArrayList<>();
                    List<PasswordRecoveryCode> codesToSave = new ArrayList<>();
                    SecureRandom random = new SecureRandom();

                    for (int i = 0; i < 10; i++) {
                        byte[] bytes = new byte[12];
                        random.nextBytes(bytes);
                        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                        plainTextCodes.add(code);

                        PasswordRecoveryCode recoveryCode = new PasswordRecoveryCode();
                        recoveryCode.setUserId(user.getId());
                        recoveryCode.setCodeHash(passwordEncoder.encode(code));
                        recoveryCode.setUsed(false);
                        codesToSave.add(recoveryCode);
                    }
                    return recoveryCodeRepository.saveAll(codesToSave).then(Mono.just(plainTextCodes));
                }).flatMap(mono -> mono));
    }

    @Transactional
    public Mono<Void> resetPasswordWithRecoveryCode(ResetPasswordWithCodeRequestDto dto) {
        return userRepository.findByUsername(dto.getUsername())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .flatMap(user -> recoveryCodeRepository.findByUserIdAndIsUsed(user.getId(), false)
                        .filter(code -> passwordEncoder.matches(dto.getRecoveryCode(), code.getCodeHash()))
                        .next()
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or used recovery code")))
                        .flatMap(code -> {
                            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

                            code.setUsed(true);
                            return userRepository.save(user)
                                    .then(recoveryCodeRepository.save(code));
                        })
                ).then();
    }
}
