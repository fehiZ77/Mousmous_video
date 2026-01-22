package com.daniax.auth_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "user_name")
    private String userName;

    private String email;

    @Column(name = "password")
    private String mdp;

    @Column(name = "is_first_login")
    private Boolean isFirstLogin;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Constructor
    public User() {
    }

    public User(String userName, String email, String mdp, int roleHtml) throws Exception{
        this.setUserName(userName);
        this.setEmail(email);
        this.setMdp(mdp);
        this.setRole(roleHtml);
        this.setFirstLogin(true);
    }

    // Getters and setters
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    public void setRole(int roleHtml) throws Exception{
        switch (roleHtml){
            case 1 :
                this.role = Role.USER;
                break;
            case 10 :
                this.role = Role.ADMIN;
                break;
            default :
                throw new Exception("Role not set correctly");
        }
    }
}
