package rp.fitkit.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.UserLoginDto;
import rp.fitkit.api.dto.UserRegistrationDto;
import rp.fitkit.api.exception.UserAlreadyExistsException;
import rp.fitkit.api.model.User;
import rp.fitkit.api.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

                    User newUser = new User();
                    newUser.setUsername(registrationDto.getUsername());
                    newUser.setEmail(registrationDto.getEmail());
                    newUser.setPasswordHash(hashedPassword);

                    return userRepository.save(newUser);
                });
    }

    /**
     * Verifieert de inloggegevens van een gebruiker.
     * @param loginDto De DTO met username/email en wachtwoord.
     * @return Een Mono die de User bevat bij succes, of een error Mono bij falen.
     */
    public Mono<User> loginUser(UserLoginDto loginDto) {
        return userRepository.findByUsername(loginDto.getUsernameOrEmail())
                .switchIfEmpty(userRepository.findByEmail(loginDto.getUsernameOrEmail()))
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
                        return Mono.just(user);
                    } else {
                        return Mono.error(new BadCredentialsException("Ongeldige inloggegevens."));
                    }
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException("Ongeldige inloggegevens.")));
    }
}
