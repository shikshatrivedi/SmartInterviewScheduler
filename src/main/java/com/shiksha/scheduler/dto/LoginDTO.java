package com.shiksha.scheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginDTO() {}

    public String getEmail()           { return email; }
    public void   setEmail(String v)   { this.email = v; }
    public String getPassword()        { return password; }
    public void   setPassword(String v){ this.password = v; }
}
