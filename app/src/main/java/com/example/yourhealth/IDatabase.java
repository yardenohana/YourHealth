package com.example.yourhealth;

import org.bson.Document;

import java.util.concurrent.ExecutionException;

import io.realm.mongodb.mongo.MongoCollection;

public interface IDatabase {

    // Check if the user is in the DB already
    public boolean doesUserExist(String username);

    // Does the email already in use by some user
    public boolean doesEmailInUse(String email);

    // Check if password matches to the user
    public boolean doesPasswordMatch(String username, String password);

    // Add new user into the DB
    public boolean addNewUser(String username, String password, String email, int weight, int height);

    public void InitializeDatabase();
}
