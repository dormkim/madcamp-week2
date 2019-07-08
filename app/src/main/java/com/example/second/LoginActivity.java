package com.example.second;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
    public String id;
    public String user_name;

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
        GraphRequest request = GraphRequest.newMeRequest(currentAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override public void onCompleted(JSONObject object, GraphResponse  response) {
                        //You can fetch user info like this…
                        user_name = object.optString("name");
                        id = object.optString("id");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user_email", id);
                        intent.putExtra("name", user_name);
                        startActivity(intent);
                        finish();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        request.executeAsync();
    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode,  data);
    }
}