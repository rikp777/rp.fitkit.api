package rp.fitkit.api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = {"passwordHash"})
@Table("app_user")
public class User implements Persistable<String> {
    @Id
    private String id = UUID.randomUUID().toString();
    private String username;
    private String email;
    @Column("password_hash")
    private String passwordHash;
    @Column("date_joined")
    private LocalDate dateJoined;

    @Transient
    private boolean isNew;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.dateJoined = LocalDate.now();
        this.isNew = true;
    }

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.isNew = true;
        this.dateJoined = LocalDate.now();
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }
}
