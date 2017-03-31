package in.co.prima.userlogin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import in.co.prima.userlogin.helper.SQLiteHandler;
import in.co.prima.userlogin.helper.SessionManager;

import java.util.HashMap;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private TextView txtName;
    private TextView txtusername;
    private Button btnLogout;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtName = (TextView) findViewById(R.id.name);
        txtusername = (TextView) findViewById(R.id.username);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String username = user.get("username");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtusername.setText(username);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
