package com.fooddeliveryapp.remote.dto;

public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public String phone;
    public String address;
    public String role; // CUSTOMER | MERCHANT

    public RegisterRequest(String name, String email, String password, String phone, String address, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }
}

