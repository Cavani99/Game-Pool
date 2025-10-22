package main.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import main.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@AllArgsConstructor
public class AuthenticationDetails implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private UserRole role;
    private boolean isBanned;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority role = new SimpleGrantedAuthority(this.role.name());

        return List.of(role);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isBanned;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isBanned;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isBanned;
    }

    @Override
    public boolean isEnabled() {
        return this.isBanned;
    }
}
