package com.example.yourhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    // Extra data passed to the home activity
    public static final String CURRENT_USERNAME = "com.example.yourHealth.CURRENT_USERNAME";
    public static final String CURRENT_EMAIL = "com.example.yourHealth.CURRENT_EMAIL";
    public static final String CURRENT_WEIGHT = "com.example.yourHealth.CURRENT_WEIGHT";
    public static final String CURRENT_HEIGHT = "com.example.yourHealth.CURRENT_HEIGHT";

    // Weight & Height constants
    private static final int MIN_WEIGHT = 20;
    private static final int MAX_WEIGHT = 550;
    private static final int MIN_HEIGHT = 120;
    private static final int MAX_HEIGHT = 200;
    private static final int DEFAULT_HEIGHT = 170;
    private static final int DEFAULT_WEIGHT = 55;

    private MongoCommunicator dbCommunicator;

    // Filter the input of the login of unnecessary characters
    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && "\n\t".contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Avoid network errors
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button signupBtn = (Button) findViewById(R.id.SignupButton);
        EditText passwordInput = (EditText) findViewById(R.id.PasswordInput);
        EditText usernameInput = (EditText) findViewById(R.id.UsernameInput);
        EditText emailInput = (EditText) findViewById(R.id.EmailAddress);
        TextView errorPrompt = (TextView) findViewById(R.id.MessageReplyText);
        NumberPicker weightPicker = (NumberPicker) findViewById(R.id.WeightNumberPicker);
        NumberPicker heightPicker = (NumberPicker) findViewById(R.id.HeightNumberPicker);

        signupBtn.setBackground(getDrawable(R.drawable.buttons_design));

        // restrict specific input characters
        usernameInput.setFilters(new InputFilter[]{ filter });
        passwordInput.setFilters(new InputFilter[]{ filter });
        emailInput.setFilters(new InputFilter[]{ filter });

        initializeDatabase();

        // Set the height & weight pickers
        setPickers(weightPicker, heightPicker);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoadingDialog loadingDialog = new LoadingDialog(SignupActivity.this);
                loadingDialog.startLoadingDialog();

                // Create a thread to contact
                // database asynchronously
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                validateSignUp(usernameInput, passwordInput, emailInput,
                                        weightPicker, heightPicker, errorPrompt);
                                loadingDialog.dismissDialog(); // finished retrieving data from the database
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /*

     */
    private void  validateSignUp(EditText usernameInput, EditText passwordInput, EditText emailInput,
                           NumberPicker weightPicker, NumberPicker heightPicker, TextView errorPrompt) {
        if (InputValidator.doesSomeFieldsEmpty(
                new TextView[]{usernameInput, passwordInput, emailInput}))
        {
            errorPrompt.setText((CharSequence) "Some fields are empty.");
        }
        else
        {
            boolean isInputValid = false;

            isInputValid = InputValidator.checkSignUpValidity(
                    usernameInput.getText().toString(),
                    passwordInput.getText().toString(),
                    emailInput.getText().toString(), errorPrompt
            );

            // End function when input is invalid
            if (!isInputValid)
                return;

            // In case user info exists in the DB
            if (dbCommunicator.doesUserExist(usernameInput.getText().toString()))
            {
                errorPrompt.setText((CharSequence)"This user name is already used.");
            }
            else if (dbCommunicator.doesEmailInUse(emailInput.getText().toString()))
            {
                errorPrompt.setText((CharSequence)"Email already in use by another user.");
            }
            else // all attributes are valid to insert
            {
                dbCommunicator.addNewUser(
                        usernameInput.getText().toString(),
                        passwordInput.getText().toString(),
                        emailInput.getText().toString(),
                        weightPicker.getValue(),
                        heightPicker.getValue()
                );

                // finally navigate to home
                openHomeActivity();
            }
        }
    }

    /*
    Initializes the database and sets a message in accordance when error occurs.
    Input: void
    Output: void
    */
    private void initializeDatabase() {
        dbCommunicator = new MongoCommunicator();
        dbCommunicator.InitActivity(this); // get context

        try {
            dbCommunicator.InitializeDatabase(); // initialize Realm connection & atlas
        }
        catch (Exception e) {
            Log.v("Exception", "MongoDB connection failed");
            Toast.makeText(getApplicationContext(), "ERROR: can't connect to database", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Set the default values to the pickers.
    Input: weight picker, height picker
    Output: void
     */
    private void setPickers(NumberPicker weightPicker, NumberPicker heightPicker) {
        // weight
        weightPicker.setMinValue(MIN_WEIGHT);
        weightPicker.setMaxValue(MAX_WEIGHT);
        weightPicker.setValue(DEFAULT_WEIGHT);

        // height
        heightPicker.setMinValue(MIN_HEIGHT);
        heightPicker.setMaxValue(MAX_HEIGHT);
        heightPicker.setValue(DEFAULT_HEIGHT);
    }

    /*
    Opens the home activity after successful sign up.
    Input: void
    Output: void
     */
    private void openHomeActivity() {
        Intent HomeIntent = new Intent(this, activity_home.class);

        // Pass the user info to the home activity
        HomeIntent.putExtra(CURRENT_USERNAME, ((EditText) findViewById(R.id.UsernameInput)).getText().toString());
        HomeIntent.putExtra(CURRENT_EMAIL, ((EditText) findViewById(R.id.EmailAddress)).getText().toString());
        HomeIntent.putExtra(CURRENT_HEIGHT, ((NumberPicker) findViewById(R.id.HeightNumberPicker)).getValue());
        HomeIntent.putExtra(CURRENT_WEIGHT, ((NumberPicker) findViewById(R.id.WeightNumberPicker)).getValue());
        // finish the current activity,
        // so users can't go back with the native previous button.
        finish();
    }
}