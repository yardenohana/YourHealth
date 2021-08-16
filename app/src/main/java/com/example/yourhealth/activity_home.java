package com.example.yourhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class activity_home extends AppCompatActivity implements SensorEventListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String CURRENT_USERNAME = "com.example.yourHealth.CURRENT_USERNAME";
    public static final String CURRENT_EMAIL = "com.example.yourHealth.CURRENT_EMAIL";
    public static final String CURRENT_WEIGHT = "com.example.yourHealth.CURRENT_WEIGHT";
    public static final String CURRENT_HEIGHT = "com.example.yourHealth.CURRENT_HEIGHT";

    private MongoCommunicator mongoCommunicator;

    // constant code for runtime permissions (pdf operation)
    private static final int PERMISSION_REQUEST_CODE = 200;

    // declaring width and height
    // for our PDF file.
    int pageHeight = 1120;
    int pagewidth = 792;

    // creating a bitmap variable
    // for storing our images
    Bitmap bmp, scaledbmp;

    private DrawerLayout drawer;  // used for the side bar
    private AppUser currentUser;

    // Step counter
    private SensorManager sensorManager; // step counter sensor
    private TextView step_count;
    boolean isRunning;

    // Top tool bar menu inflation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tools_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Avoid network errors
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // DB initialize
        initializeDatabase();

        // Set up the side bar navigation drawer
        setSideNavViewer();

        // Get the current user name which is connected & email
        currentUser = new AppUser();
        getExtraUserData();

        // Assign the default fragment to be the Profile fragment
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, ProfileFragment.newInstance(currentUser.getUsername(), currentUser.getEmail())
        ).commit();

        //TextView welcomeUser = (TextView) findViewById(R.id.WelcomeUser);
        TextView currentDate = findViewById(R.id.currentDate);
        //welcomeUser.setText((CharSequence) (welcomeUser.getText().toString() + " " + currentUser));

        // Set the date view
        currentDate.setText(getCurrentDate());

        // Step count sensor initialization
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    /*
    This function serves as the handler for the pdf exports click.
    Input: void
    Output: void
     */
    private void PdfHandler() {
        // Logo
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.skeleton_logo);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        // checking our permissions.
        if (checkPermission()) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        generatePDF();
    }

    /*
    Set the side bar menu, a navigation viewer.
    Input: void
    Output: void
     */
    private void setSideNavViewer() {
        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Set the navigation item listener so it can recognize selecting items
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /*
    Get additional information about the user we handle.
    Input: void
    Output: void
     */
    private void getExtraUserData() {
        currentUser.setUsername(getIntent().getStringExtra(CURRENT_USERNAME));
        currentUser.setEmail(getIntent().getStringExtra(CURRENT_EMAIL));
        currentUser.setHeight(getIntent().getIntExtra(CURRENT_HEIGHT, 0));
        currentUser.setWeight(getIntent().getIntExtra(CURRENT_WEIGHT, 0));
    }

    // When back pressing, the side bar closes.
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
          super.onBackPressed();
        }
    }

    // The method handles the clicks in the toolbar menu items.
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.SideBarButton:
                // open side bar
                drawer.openDrawer(Gravity.LEFT);
                return true;
            case R.id.About:
                Toast.makeText(this, "About us...", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.ResetApp:
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                return true;
            case R.id.exportRecords:
                PdfHandler();
                return true;
            case R.id.ChangeProfile:
                // TODO
                return true;
            case R.id.CreateNewProfile:
                goToSignUp();
                return true;
            case R.id.DeleteProfile:
                DialogInterface.OnClickListener dialogClickListenerDelete = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            // Yes button clicked
                            mongoCommunicator.deleteUser(currentUser.getUsername());
                            goToSignUp();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                };

                AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
                myBuilder.setMessage("Are you sure you want to delete your profile?")
                        .setPositiveButton("Yes", dialogClickListenerDelete)
                        .setNegativeButton("No", dialogClickListenerDelete).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    Initializes the database and sets a message in accordance when error occurs.
    Input: void
    Output: void
    */
    private void initializeDatabase() {
        mongoCommunicator = new MongoCommunicator();
        mongoCommunicator.InitActivity(this); // get context

        try {
            mongoCommunicator.InitializeDatabase(); // initialize Realm connection & atlas
        }
        catch (Exception e) {
            Log.v("Exception", "MongoDB connection failed");
            Toast.makeText(getApplicationContext(), "ERROR: can't connect to database", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Gets the current date as a string.
    Input: void
    Output: the date
     */
    private String getCurrentDate() {
        Calendar calender = Calendar.getInstance();
        return DateFormat.getDateInstance(DateFormat.FULL).format(calender.getTime());
    }

    /*
    Navigate to the sign up activity.
    Input: void
    Output: void
     */
    private void goToSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    /*
     Starting walking or running again,
     so we continue checking the step using the sensor
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.isRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
            Toast.makeText(this, "Steps sensor not found, probably you are using emulator.", Toast.LENGTH_SHORT).show();
        }
    }

    // Stop walking or running
    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        //sensorManager.unregisterListener();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRunning) {
            this.step_count.setText(String.valueOf(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // The method handles the navigation drawer item clicks.
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Profile_nav:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, ProfileFragment.newInstance(currentUser.getUsername(), currentUser.getEmail())).commit();
                break;
            case R.id.Workout_nav:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, WorkingOutFragment.newInstance(currentUser.getUsername())).commit();
                break;
            case R.id.Exercises_nav:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, new ExercisesListFragment()).commit();
                break;
            case R.id.Weight_track_nav:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, new WeightFragment()).commit();
                break;
            case R.id.About_nav:
                Toast.makeText(this, "About us info", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Pdf operations

    /*
    This function generates the pdf trainings file.
    It uses the trainings listed in the database, and shows
    the 10 most updated exercises.
    Input: void
    Output: void
     */
    private void generatePDF() {
        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // for adding text or shapes in our PDF file.
        Paint paint = new Paint();
        Paint title = new Paint();

        // adding page info to our PDF file
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();

        // below line is used for setting
        // start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // draw our image on our PDF file.
        canvas.drawBitmap(scaledbmp, 56, 40, paint);

        // adding typeface for our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // setting text size
        title.setTextSize(15);

        // setting color of the text
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
        canvas.drawText("A portal for IT professionals.", 209, 100, title);
        canvas.drawText("Geeks for Geeks", 209, 80, title);

        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.purple_200));
        title.setTextSize(15);

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("This is sample document which we have created.", 396, 560, title);

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of
        // our PDF file and its path
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        File file = new File(directory, "TrainingsData.pdf");
        Log.v("file", file.getAbsolutePath());

        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(file));

            // below line is to print toast message
            // on completion of PDF generation.
            Toast.makeText(getApplicationContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // below line is used
            // to handle error
            e.printStackTrace();
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close();
    }

    /*
    Checks the permissions which should be listed in the AndroidManifest.xml file.
    Input: the app context
    Output: does it have permissions
     */
    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    /*
    Request the permissions in case there are none.
    Input: the current activity
    Output: void
     */
    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    /*
    Callback for requesting write-read storage when there are none permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}