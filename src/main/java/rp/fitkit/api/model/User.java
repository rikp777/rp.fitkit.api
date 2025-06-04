package rp.fitkit.api.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"passwordHash"})
public class User {
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDate dateJoined;

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
