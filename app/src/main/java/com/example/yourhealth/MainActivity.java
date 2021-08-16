package com.example.yourhealth;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.StrictMode;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.Document;

public class MainActivity extends AppCompatActivity {

    private MongoCommunicator mongo_cummunicator;

    // Extra data passed to the home activity
    public static final String CURRENT_USERNAME = "com.example.yourHealth.CURRENT_USERNAME";
    public static final String CURRENT_EMAIL = "com.example.yourHealth.CURRENT_EMAIL";
    public static final String CURRENT_WEIGHT = "com.example.yourHealth.CURRENT_WEIGHT";
    public static final String CURRENT_HEIGHT = "com.example.yourHealth.CURRENT_HEIGHT";

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

        setContentView(R.layout.activity_main);

        // Avoid network errors
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // TEST ONLY (login with test user)
        //openHomeActivityTest();

        // register the broadcast receiver
        Broadcast broadcast = new Broadcast();
        registerReceiver(broadcast, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(broadcast, new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
        registerReceiver(broadcast, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        registerReceiver(broadcast, new IntentFilter(Intent.ACTION_POWER_CONNECTED));

        // Connect UI elements to the java objects
        final Button login_btn = (Button) findViewById(R.id.LoginButton);
        Button sign_up_btn = (Button) findViewById(R.id.GoToSignupButton);
        EditText personName = (EditText) findViewById(R.id.PersonName);
        EditText passwordEntered = (EditText) findViewById(R.id.Password);
        TextView errorPrompt = (TextView) findViewById(R.id.MessageReplyText);

        sign_up_btn.setBackground(getDrawable(R.drawable.buttons_design));
        login_btn.setBackground(getDrawable(R.drawable.buttons_design));

        // restrict specific input characters
        personName.setFilters(new InputFilter[]{ filter });
        passwordEntered.setFilters(new InputFilter[]{ filter });

        // Initialize the database to be communicative, login Realm app
        initializeDatabase();

        // Enter login info
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Start the loading dialog, while retrieving data from the database
                final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
                loadingDialog.startLoadingDialog();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                validateLogin(errorPrompt, personName, passwordEntered);
                                loadingDialog.dismissDialog(); // finally, dismiss the dialog
                            }
                        });
                    }
                }).start();
            }
        });

        // Sign up in case user don't have an account already
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpActivity();
            }
        });
    }

    /*
    Initializes the database and sets a message in accordance when error occurs.
    Input: void
    Output: void
     */
    private void initializeDatabase() {
        // DB initialization
        mongo_cummunicator = new MongoCommunicator();
        mongo_cummunicator.InitActivity(this); // get context

        try {
            mongo_cummunicator.InitializeDatabase(); // initialize Realm connection & atlas
        }
        catch (Exception e) {
            Log.v("Exception", "MongoDB connection failed");
            Toast.makeText(getApplicationContext(), "ERROR: can't connect to database", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    The function validates the login input and checks weather the user exists
    in the database. In accordance, it sets the error prompt in case needed
    Input: the error prompt, the person name and password.
    Output: void
     */
    private void validateLogin(TextView errorPrompt, EditText personName, EditText passwordEntered) {

        if (InputValidator.doesSomeFieldsEmpty(new TextView[]{personName, passwordEntered}))
        {
            errorPrompt.setText((CharSequence) "Some fields are empty");
        }
        else // all fields aren't empty
        {
            // check the input validity
            boolean isInputValid = InputValidator.checkLoginValidity(
                    personName.getText().toString(), passwordEntered.getText().toString(), errorPrompt
            );

            // quit if input not valid
            if (!isInputValid)
                return;

            // if the user exists in the database, continue to the home activity
            if (mongo_cummunicator.doesUserExist(personName.getText().toString()))
            {
                // continue to home only if the password matches to the user
                if (mongo_cummunicator.doesPasswordMatch(personName.getText().toString(), passwordEntered.getText().toString()))
                {
                    openHomeActivity();
                }
                else
                {
                    String notSignedUpMsg = "Wrong password, try again.";
                    errorPrompt.setText(notSignedUpMsg);
                }
            }
            else
            {
                String notSignedUpMsg = personName.getText().toString() + " not signed up yet.";
                errorPrompt.setText(notSignedUpMsg);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Opens the sign up activity in case the user
    do not have an account already.
     */
    private void openSignUpActivity() {
        Intent signUpIntent = new Intent(this, SignupActivity.class);
        startActivity(signUpIntent);
    }

    /*
    Opens the Home activity after successful login.
     */
    private void openHomeActivity() {

        String currentUsername = ((EditText) findViewById(R.id.PersonName)).getText().toString();
        AppUser appUser = mongo_cummunicator.getUser(currentUsername);

        Intent HomeIntent = new Intent(this, activity_home.class);

        // Pass the data to the home activity
        HomeIntent.putExtra(CURRENT_USERNAME, appUser.getUsername());
        HomeIntent.putExtra(CURRENT_EMAIL, appUser.getEmail());
        HomeIntent.putExtra(CURRENT_WEIGHT, appUser.getWeight());
        HomeIntent.putExtra(CURRENT_HEIGHT, appUser.getHeight());

        startActivity(HomeIntent);

        // finish the current activity,
        // so users can't go back with the native previous button.
        finish();
    }

    /*
    TESTING PURPOSE ONLY
    TODO: remove this
     */
    private void openHomeActivityTest() {
        Intent HomeIntent = new Intent(this, activity_home.class);

        // Pass the user name to the home activity
        HomeIntent.putExtra(CURRENT_USERNAME, "Yarden3");
        HomeIntent.putExtra(CURRENT_EMAIL, "Yarden@gmail.com");
        HomeIntent.putExtra(CURRENT_HEIGHT, 173);
        HomeIntent.putExtra(CURRENT_WEIGHT, 57);

        startActivity(HomeIntent);

        // finish the current activity,
        // so users can't go back with the native previous button.
        finish();
    }
}


