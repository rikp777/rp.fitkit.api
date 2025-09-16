package rp.fitkit.api.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.LoginResponseDto;
import rp.fitkit.api.dto.UserLoginDto;
import rp.fitkit.api.dto.UserRegistrationDto;
import rp.fitkit.api.dto.UserResponseDto;
import rp.fitkit.api.exception.UserAlreadyExistsException;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.model.user.UserRole;
import rp.fitkit.api.repository.user.UserRepository;
import rp.fitkit.api.repository.user.UserRoleRepository;
import rp.fitkit.api.util.JwtUtil;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
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
                                String token = jwtUtil.generateToken(user);
                                UserResponseDto userDetails = new UserResponseDto(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getEmail(),
                                        user.getDateJoined()
                                );
                                return Mono.just(new LoginResponseDto(token, "Bearer", userDetails));
                            } else {
                                return Mono.error(new BadCredentialsException("Ongeldige inloggegevens."));
                            }
                        }))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Gebruiker niet gevonden of ongeldige inloggegevens.")));
    }
}
