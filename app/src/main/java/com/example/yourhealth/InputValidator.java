package com.example.yourhealth;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class InputValidator {

    /*
    Validate the user name to consist only of digits & letters.
    Input: the user name
    Output: true if the field is valid, else false
     */
    public static boolean isAlphanumeric(String field) {

        // check all characters are Letters or Digits.
        for (char ch : field.toCharArray())
        {
            if (!Character.isDigit(ch) && !Character.isLetter(ch))
                return false;
        }

        return true;
    }

    /*
    Checks if the login information, stands the validness rules we have defined.
    Input: user name, password
    Output: true if the fields are valid, else false
     */
    public static boolean checkLoginValidity(String username, String password, TextView errorPrompt) {
        // The method checks the username and password validness
        // Input: the username and password, the app content
        // Output: false if something wasn't valid & points it out as pop-up message

        CharSequence message = "";

        if (username == "" || password == "")
        {
            message = "Please fill all fields.";
            errorPrompt.setText(message);
            return false;
        }
        else if (username.contains(" ") || password.contains(" "))
        {
            message = "Please remove spaces.";
            errorPrompt.setText(message);
            Log.v("Input", username + ", " + password);
            return false;
        }
        else
        {
            // User name and password must consist only of alphanumeric characters.
            boolean areAlphaFields = isAlphanumeric(username) && isAlphanumeric(password);

            if (!areAlphaFields)
            {
                message = "User name and password must consist of letters and digits only.";
                errorPrompt.setText(message);
                return false;
            };
        }

        return true;
    }

    /*
    Checks if the login information, stands the validness rules we have defined.
    Input: user name, password
    Output: true if the fields are valid, else false
     */
    public static boolean checkSignUpValidity(String username, String password, String email, TextView errorPrompt)
    {
        // check if the password and user name are valid first
        boolean answerUsernamePassword = checkLoginValidity(username, password, errorPrompt);

        // in case the email or login info are invalid
        if (!isValidEmailAddress(email))
        {
            errorPrompt.setText((CharSequence) "Email format is unsupported.");
            return false;
        }
        else if (!answerUsernamePassword)
        {
            // No need to set any error message as it already being
            // set in the 'checkLoginValidity' method.
            return false;
        }

        return true;
    }

    /*
    Check if at least one of the fields is empty.
    Input: the list of fields
    Output: true or false
     */
    public static boolean doesSomeFieldsEmpty(TextView[] inputFields)
    {
        for (TextView field : inputFields)
        {
            if (doesFieldEmpty(field))
                return true;
        }

        return false;
    }

    /*
    Check if the input field is empty.
    Input: the input component
    Output: true if it's empty' else false
     */
    public static boolean doesFieldEmpty(TextView inputField) {
        if (inputField.getText().toString().trim().length() > 0)
            return false;

        return true;
    }

    /*
    Checks if the input consists only of letters.
    Input: the string
    Output: true if only letters, else false.
     */
    public static boolean isLettersOnly(String input) {
        for (char ch : input.toCharArray()) {
            // check if the character is in bounds of english letters
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' || ch <= 'Z'))) {
                return false;
            }
        }

        return true;
    }

    /*
    Approve the email format is valid.
    Input: the email
    Output: true if valid, else false
     */
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
