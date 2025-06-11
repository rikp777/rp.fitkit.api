package rp.fitkit.api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"passwordHash"})
@Table("app_user")
public class User implements Persistable<String>, UserDetails {
    @Id
    private String id;

    private String username;
    private String email;


    @Column("password_hash")
    private String passwordHash;
    @Column("date_joined")
    private LocalDate dateJoined;

    @Transient
    private boolean isNew;

    public User(String username, String email, String passwordHash) {
        this.id = UUID.randomUUID().toString();
        this.dateJoined = LocalDate.now();
        this.isNew = true;

        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }

}
