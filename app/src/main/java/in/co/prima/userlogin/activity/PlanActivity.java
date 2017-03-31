package in.co.prima.userlogin.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import in.co.prima.userlogin.activity.*;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.co.prima.userlogin.MainActivity;
import in.co.prima.userlogin.R;
import in.co.prima.userlogin.app.AppConfig;
import in.co.prima.userlogin.app.AppController;
import in.co.prima.userlogin.helper.SQLiteHandler;
import in.co.prima.userlogin.helper.SessionManager;
import in.co.prima.userlogin.printer.BtPrint4;

public class PlanActivity extends AppCompatActivity /*implements Spinner.OnItemSelectedListener*/{
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogout;
    Context c;
    private Button btnPlan;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    TextView Data;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);



        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        final HashMap<String, String> user = db.getUserDetails();
        final String netid = user.get("created_at");
        final String username =user.get("uid");
        getPlanDetails(netid,username);
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser(username);
            }
        });

    }
    private void logoutUser(final String username)
    {
        TelephonyManager tManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final String macid = tManager.getDeviceId();

        String tag_string_req = "req_login";
        //pDialog.setMessage("Logging out ...");
        //showDialog();
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGOUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "Login Response: " + response.toString());
/*                hideDialog();*/


                try {
                    JSONObject jObj1 = new JSONObject(response);
                    int error = jObj1.getInt("err");

                    // Check for error node in json
                    if (error == 0) {
                        // user successfully logged in
                        // Create login session
                        // Launch main activity
                        session.setLogin(false);
                        db.deleteUsers();
                        Intent intent = new Intent(PlanActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else
                    {
                        // Error in login. Get the error message
                        String errorMsg = jObj1.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e)
                {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
              /*  hideDialog();*/
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("token", macid);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
            /*session.setLogin(false);*/
        db.deleteUsers();
    }
    //
    private void getPlanDetails(final String netid,final String username)
    {
        final ArrayList<String> users = new ArrayList<>();
        /*  final ListModel users = new ListModel();*/
        final ArrayAdapter sp=new ArrayAdapter(this, android.R.layout.simple_list_item_1,users);
        final ListView listview = (ListView)findViewById(R.id.listv);
        listview.setAdapter(sp);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Object o = listview.getItemAtPosition(position);
                String selected = (String) (listview.getItemAtPosition(position));
                Intent intent = new Intent(PlanActivity.this,
                        BtPrint4.class);
                intent.putExtra("groupname",selected);
                intent.putExtra("netid",netid);
                intent.putExtra("username",username);

                startActivity(intent);
                finish();
            }
        });
        String tag_string_req = "req_login";
        pDialog.setMessage("Getting Plan Details ...");
        showDialog();
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_PLAN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "Plan Detail Response: " + response.toString());
                hideDialog();


                try {
                    JSONObject jObj1 = new JSONObject(response);
                    JSONObject jObj = jObj1.getJSONObject("result");

                    JSONArray jArray = jObj.getJSONArray("data");
                   JSONObject jo =null;
                    int error = jObj1.getInt("err");

                    // Check for error node in json
                         if (error == 0) {
                         sp.clear();

                        for (int i=0; i< jArray.length();i++)
                        {
                          jo=jArray.getJSONObject(i);
                            users.add(jo.getString("group_hd"));
                            sp.notifyDataSetChanged();

                        }
                    } else
                    {
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("netid", netid);
                return params;
            }

        };


        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        sp.notifyDataSetChanged();

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}