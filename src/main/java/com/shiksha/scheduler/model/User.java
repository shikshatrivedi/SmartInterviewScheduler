package com.shiksha.scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "department")
    private String department;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ─── Constructors ───────────────────────────────────────────────────────────
    public User() {}

    // ─── Builder pattern ────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String fullName;
        private String email;
        private String password;
        private Role role;
        private boolean enabled = true;
        private String phoneNumber;
        private String department;

        public Builder fullName(String v)    { this.fullName    = v; return this; }
        public Builder email(String v)       { this.email       = v; return this; }
        public Builder password(String v)    { this.password    = v; return this; }
        public Builder role(Role v)          { this.role        = v; return this; }
        public Builder enabled(boolean v)    { this.enabled     = v; return this; }
        public Builder phoneNumber(String v) { this.phoneNumber = v; return this; }
        public Builder department(String v)  { this.department  = v; return this; }

        public User build() {
            User u = new User();
            u.fullName    = this.fullName;
            u.email       = this.email;
            u.password    = this.password;
            u.role        = this.role;
            u.enabled     = this.enabled;
            u.phoneNumber = this.phoneNumber;
            u.department  = this.department;
            return u;
        }
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────────
    public Long getId()                         { return id; }
    public String getFullName()                  { return fullName; }
    public void   setFullName(String v)          { this.fullName = v; }
    public String getEmail()                     { return email; }
    public void   setEmail(String v)             { this.email = v; }
    public String getPassword()                  { return password; }
    public void   setPassword(String v)          { this.password = v; }
    public Role   getRole()                      { return role; }
    public void   setRole(Role v)                { this.role = v; }
    public void   setEnabled(boolean v)          { this.enabled = v; }
    public String getPhoneNumber()               { return phoneNumber; }
    public void   setPhoneNumber(String v)       { this.phoneNumber = v; }
    public String getDepartment()                { return department; }
    public void   setDepartment(String v)        { this.department = v; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public LocalDateTime getLastLogin()          { return lastLogin; }
    public void   setLastLogin(LocalDateTime v)  { this.lastLogin = v; }

    // ─── UserDetails ────────────────────────────────────────────────────────────
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String  getUsername()              { return email; }
    @Override public boolean isEnabled()                { return enabled; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
}
