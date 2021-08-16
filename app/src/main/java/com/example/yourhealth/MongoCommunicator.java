package com.example.yourhealth;

import android.content.Context;
import android.util.Log;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import org.bson.Document;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.mongo.result.DeleteResult;
import io.realm.mongodb.mongo.result.UpdateResult;

/*
Enables easy connection to the MongoDB cluster, easy CRUD abilities
Using the Realm backend service and MongoDB atlas.
 */
public class MongoCommunicator implements IDatabase {

    private App realmBack; // Realm backend connection

    // Collections references, included in the MongoDB Database
    public MongoCollection<Document> usersCollection;
    public MongoCollection<Document> exercisesCollection;
    public MongoCollection<Document> FoodCollection;

    /*
    Deletes an exercise from the exercises collection.
    Input: the name of the exercise
    Output: true if managed to delete successfully
     */
    public boolean deleteExercise(String exercise) {

        RealmResultTask<DeleteResult> result = exercisesCollection.deleteOne(new Document().append("Name", exercise));
        DeleteResult res = result.get();

        if (res.getDeletedCount() == 0) {
            Log.v("[-] Communicator: deleteExercise", "failed to delete exercise '" + exercise + "'");
            return false;
        }

        return true;
    }

    /*
   Deletes a user from the Users collection.
   Input: the name of the user
   Output: true if managed to delete successfully
    */
    public boolean deleteUser(String username) {

        RealmResultTask<DeleteResult> result = usersCollection.deleteOne(new Document().append("Username", username));
        DeleteResult res = result.get();

        if (res.getDeletedCount() == 0) {
            Log.v("[-] Communicator: deleteExercise", "failed to delete user '" + username + "'");
            return false;
        }

        return true;
    }

    /*
    Retrieves the details of the user from the database.
    Assumption: User exists in the database
    Input: the user name
    Output: User object
     */
    public AppUser getUser(String username) {
        RealmResultTask<Document> resultDocument = usersCollection.findOne(new Document().append("Username", username));
        Document user = resultDocument.get();
        AppUser currentUser = new AppUser(
                user.get("Username").toString(),
                user.get("Email").toString(),
                user.get("Password").toString(),
                (int) user.get("Weight"),
                (int) user.get("Height"));

        return currentUser;
    }

    /*
    Get all the documents as list, from a collection.
    Input: the desired collection
    Output: a list of all the documents
     */
    public ArrayList<Document> getCollectionDocuments(MongoCollection<Document> mongoCollection) {
        FindIterable<Document> res = mongoCollection.find();
        MongoCursor<Document> cursor = res.iterator().get();
        ArrayList<Document> listOfDocs = new ArrayList<Document>();

        try {
            while(cursor.hasNext()) {
                listOfDocs.add(cursor.next());
            }
        } catch (Exception e) {
            Log.v("getCollectionDocuments", "Exception:" + e.getMessage());
        } finally {
            cursor.close();
        }

        return listOfDocs;
    }

    /*
    The method checks if the attribute value presents in a document, in the collection.
    Input: the attribute name, the attribute value, the collection in the database
    Output: true if the attribute presents, else false
     */
    public boolean doesAttrExists(String attrName, String attrVal, MongoCollection<Document> mongoCollection) {

        FindIterable<Document> res = mongoCollection.find(new Document().append(attrName, attrVal));
        MongoCursor<Document> cursor = res.iterator().get();

        try {
            while(cursor.hasNext()) {
                if (cursor.next().get(attrName).toString().equals(attrVal)) {
                    Log.v(attrName, "AttrValue: '" + attrVal + "', exists in the DB.");
                    return true;
                }
            }
        }
        catch (Exception e) {
            Log.v(attrName, "Exception was thrown while looking for '"
                    + attrVal + "' in the DB! Exception: " + e.getMessage());
        }
        finally {
            cursor.close();
        }

        return false;
    }

    /*
    Check if a user already signed up in the database.
    Input: the user name
    Output: true in case the user exists in the database, else false
     */
    @Override
    public boolean doesUserExist(String username) {
        return doesAttrExists("Username", username, usersCollection);
    }

    /*
    Check if the email already in use by some user in the DB.
    Input: the email to check
    Output: true if email in use, else false
     */
    @Override
    public boolean doesEmailInUse(String email) {
        return doesAttrExists("Email", email, usersCollection);
    }


    /*
    Check if a users password matches his username.
    Please check if the user exists before calling this method.
    Input: the user name, the password
    Output: true if the password matches the username assigned in the database.
     */
    @Override
    public boolean doesPasswordMatch(String username, String password) {

        boolean doesMatch = false;

        FindIterable<Document> iterable = usersCollection.find(
                new Document("Username", username).append(
                             "Password", password
                )
        );

        RealmResultTask<MongoCursor<Document>> cursorResult = iterable.iterator();
        MongoCursor<Document> cursor = cursorResult.get();

        try {
            while(cursor.hasNext()) {
                Document curr = cursor.next();
                if (curr.get("Username").toString().equals(username) &&
                    curr.get("Password").toString().equals(password))
                {
                    Log.v("Password & Username", "it matches!");
                    doesMatch = true;
                }
            }
        }
        catch (Exception e) {
            Log.v("Password & Username", "Exception: " + e.getMessage());
        }
        finally {
            cursor.close();
        }

        return doesMatch;
    }

    /*
    Inserts a new bson document which contains the user's data, into the DB.
    Input: the username, password & email
    Output: True if the operation succeeded, else false
     */
    @Override
    public boolean addNewUser(String username, String password, String email, int weight, int height) {

        boolean hasSucceeded = true;

        Document doc = new Document();
        doc.append("Username", username);
        doc.append("Password", password);
        doc.append("Email", email);
        doc.append("Weight", weight);
        doc.append("Height", height);
        doc.append("Trainings", new ArrayList<Document>());

        try {
            usersCollection.insertOne(doc).get();
        } catch (MongoWriteException e) {
            hasSucceeded = false;
            Log.v("Insertion error",
                 "Some error while inserting document with  _id: " + doc.get("_id")
            );
        }

        return hasSucceeded;
    }

    /*
    Retrieves the trainings list of a specific user.
    Input: the user name
    Output: exercises list
     */
    public ArrayList<Document> getTrainings(String username) {
        boolean hasSucceeded = true;

        RealmResultTask<Document> doc = this.usersCollection.findOne(new Document().append("Username", username));
        Document user = doc.get();

        return (ArrayList<Document>) user.get("Trainings");
    }

    /*
    Add a new training to a user trainings list.
    Input: the user name, all of the training details
    Output: add a new training to a user
     */
    public boolean addNewTraining(String username, String exercise, String date,
                                  String duration, int sets, int reps, int weight) {
        Document doc = new Document();
        boolean hasSucceeded = true;

        // Creating the new training document
        Log.v("exercise", exercise);
        doc.append("Exercise", exercise);
        doc.append("Date", date);
        doc.append("Duration", duration);
        doc.append("Sets", sets);
        doc.append("Repeats", reps);
        doc.append("Weight", weight);

        // Filter Query
        Document query = new Document();
        query.put("Username", username); // search the user query

        // Update document
        Document newDocument = new Document();

        // add previous trainings to the new one
        newDocument.put("Trainings", doc);

        Document updateObject = new Document();
        updateObject.put("$push", newDocument);
        Log.v("Add training query", query.toJson());

        try {
            // Update the user's trainings with the new data
            RealmResultTask<UpdateResult> res = this.usersCollection.updateOne(query, updateObject);
            UpdateResult result = res.get();

        } catch (MongoWriteException e) {
            hasSucceeded = false;
            Log.v("Update error",
                    "Some error while updating document with  _id: " + doc.get("_id")
            );
        }

        return hasSucceeded;
    }

    /*
    Initializes the Database, gets the current collections,
    and connect using the realm app in order to get those references.
    Input: void
    Output: void
     */
    @Override
    public void InitializeDatabase() {

        Credentials credentials = Credentials.emailPassword(RealmAppConfig.APP_USER_EMAIL, RealmAppConfig.APP_USER_PASS);

        this.realmBack.loginAsync(credentials, new App.Callback<User>() {

            @Override
            public void onResult(App.Result<User> result) {
                if (result.isSuccess()) {
                    Log.v("[+] User", "Logged into Realm app successfully!");
                } else {
                    Log.v("[-] User", "Failed to login Realm app.");
                }
            }
        });

        // get current realm user
        User user = this.realmBack.currentUser();

        // Instantiate a Remote Mongo Client
        assert user != null;
        MongoClient client = user.getMongoClient("mongodb-atlas");

        // Database reference
        MongoDatabase db = client.getDatabase(RealmAppConfig.DB_NAME);

        // Get collections references
        this.usersCollection = db.getCollection("Users");
        this.exercisesCollection = db.getCollection("Exercises");
    }

    /*
    Initializes the realm app with the context of the activity.
    Has to be called in the activity before initializing the DB.
    Input: the context object
    Output: void
     */
    public void InitActivity(Context context)
    {
        Realm.init(context);

        // link the realm app using the app ID
        realmBack = new App(new AppConfiguration.Builder(RealmAppConfig.APP_ID).build());
    }
}
