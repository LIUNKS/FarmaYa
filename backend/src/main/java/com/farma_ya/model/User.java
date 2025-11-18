package com.farma_ya.model;

import jakarta.persistence.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "Usuario")
public class User implements UserDetails {
    public User() {
    }

    public User(Integer id, String username, String email, String password, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        // Determinar el rol basado en rolId: 1 = ADMIN, 2 = CLIENTE/USER, 35 = DELIVERY
        if (rolId == null)
            return Role.USER;
        switch (rolId) {
            case 1:
                return Role.ADMIN;
            case 35:
                return Role.DELIVERY;
            default:
                return Role.USER;
        }
    }

    public void setRole(Role role) {
        this.role = role;
        // Actualizar rolId basado en el role
        switch (role) {
            case ADMIN:
                this.rolId = 1;
                break;
            case DELIVERY:
                this.rolId = 35;
                break;
            default:
                this.rolId = 2;
                break;
        }
    }

    public Integer getRolId() {
        return rolId;
    }

    public void setRolId(Integer rolId) {
        this.rolId = rolId;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer id;

    @Column(name = "rol_id")
    private Integer rolId;

    @Column(name = "nombre", nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Transient
    private Role role = Role.USER;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Determinar el rol basado en rolId
        Role currentRole;
        if (rolId == null)
            currentRole = Role.USER;
        else if (rolId == 1)
            currentRole = Role.ADMIN;
        else if (rolId == 35)
            currentRole = Role.DELIVERY;
        else
            currentRole = Role.USER;
        return List.of(new SimpleGrantedAuthority("ROLE_" + currentRole.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}