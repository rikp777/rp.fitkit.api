package rp.fitkit.api.model.user;

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
@NoArgsConstructor
@ToString(exclude = {"passwordHash"})
@EqualsAndHashCode(of = "id")
@Table("app_user")
public class User implements Persistable<UUID>, UserDetails {
    @Id
    private UUID id;

    private String username;
    private String email;

    @Column("password_hash")
    private String passwordHash;

    @Column("date_joined")
    private LocalDate dateJoined;

    @Column("is_enabled")
    private boolean enabled = true;

    @Column("is_account_non_locked")
    private boolean accountNonLocked = true;

    @Column("is_account_non_expired")
    private boolean accountNonExpired = true;

    @Column("is_credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Transient
    private boolean isNew;

    @Transient
    private Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

    public User(String username, String email, String passwordHash) {
        this.id = UUID.randomUUID();
        this.dateJoined = LocalDate.now();
        this.isNew = true;

        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void setPassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.credentialsNonExpired = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public void setAuthoritiesFromRoles(List<String> roles) {
        if (roles == null) {
            this.authorities = Collections.emptyList();
            return;
        }
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public boolean isPremium() {
        return this.authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PREMIUM") || role.equals("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }


    @Override
    @Transient
    public boolean isEnabled() {
        return this.enabled;
    }
}
