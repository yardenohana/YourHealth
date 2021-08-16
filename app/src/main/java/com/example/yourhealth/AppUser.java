package com.example.yourhealth;

import android.widget.SearchView;

import org.jetbrains.annotations.NotNull;

/*
TODO: add to UML
A user which is saved in the database.
 */
public class AppUser {
    private String username;
    private String email;
    private String password;
    private int weight;
    private int height;

    public AppUser(String username, String email, String password, int weight, int height) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.height = height;
        this.weight = weight;
    }

    // default constructor
    public AppUser() {
        this.username = "Not found";
        this.email = "Not found";
        this.password = "Not found";
        this.height = 0;
        this.weight = 0;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getHeight() {
        return this.height;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @NotNull
    public String toString() {
        return "Username: " + username + ", " +
                "Password: " + password + ", " +
                "Email: " + email + ", " +
                "Weight: " + weight + ", " +
                "Height: " + height;
    }
}
