package com.example.second;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.facebook.*;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private LoginButton txtFbLogin;
    private AccessToken mAccessToken;
    private CallbackManager callbackManager;
    private String user_email;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        txtFbLogin = findViewById(R.id.login_button);
        //  to handle login responses by calling CallbackManager.Factory.create.
        callbackManager = CallbackManager.Factory.create();
        txtFbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override public void onSuccess(LoginResult loginResult) {
                mAccessToken = loginResult.getAccessToken();
                getUserProfile(mAccessToken);
            }
            @Override public void onCancel() {

            }
            @Override public void onError(FacebookException error) {

            }
        });
        LoginManager.getInstance().logOut();
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {

                    @Override public void onCompleted(JSONObject object, GraphResponse  response) {
                        try {
                            //You can fetch user info like this…
                            //object.getJSONObject(“picture”).
                            object.getJSONObject("data").getString("url");
                            //object.getString(“name”);
                            user_email = object.getString("email");
                            //object.getString(“id”));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        request.executeAsync();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user_email", user_email);
        startActivity(intent);
        finish();
    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode,  data);
    }
}