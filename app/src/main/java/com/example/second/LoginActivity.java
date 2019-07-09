package com.example.second;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private ArrayList<String> ids = new ArrayList<>();
    private LoginButton txtFbLogin;
    private AccessToken mAccessToken;
    private CallbackManager callbackManager;
    public String id;
    public String user_name;
    private JSONObject add_id;
    private String db_exist = "YES";
    private String ip = "13.124.13.185:8080";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        try {
            new JSONTaskGet().execute("http://" + ip + "/ids").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try { PackageInfo info = getPackageManager().getPackageInfo("com.example.second", PackageManager.GET_SIGNATURES); for (Signature signature : info.signatures) { MessageDigest md = MessageDigest.getInstance("SHA"); md.update(signature.toByteArray()); String str = Base64.encodeToString(md.digest(), Base64.DEFAULT); Log.d("KeyHash:", str); Toast.makeText(this, str, Toast.LENGTH_LONG).show(); } }catch(NoSuchAlgorithmException e){ e.printStackTrace(); }catch (PackageManager.NameNotFoundException e){ e.printStackTrace(); }

        txtFbLogin = findViewById(R.id.login_button);
        //to handle login responses by calling CallbackManager.Factory.create.
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
                        if(!ids.contains(id)){
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("id", id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            add_id = jsonObject;
                            try {
                                new JSONTaskPostObj().execute("http://" + ip + "/ids").get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            db_exist = "NO";
                        }
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user_email", id);
                        intent.putExtra("name", user_name);
                        intent.putExtra("db_exist", db_exist);
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


    //하나의 contact 추가
    public class JSONTaskPostObj extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                JSONObject jsonObject = add_id;
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));

                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();
                    //버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    //기존의 DBLIst에 있다면 get dbTag
    public class JSONTaskGet extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);//url을 가져온다.
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();//연결 수행

                    //입력 스트림 생성
                    InputStream stream = con.getInputStream();

                    //속도를 향상시키고 부하를 줄이기 위한 버퍼를 선언한다.
                    reader = new BufferedReader(new InputStreamReader(stream));

                    //실제 데이터를 받는곳
                    StringBuffer buffer = new StringBuffer();

                    //line별 스트링을 받기 위한 temp 변수
                    String line = "";

                    //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String... urls) 니까
                    parseJsonData(buffer.toString());
                    return buffer.toString();

                    //아래는 예외처리 부분이다.
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //종료가 되면 disconnect메소드를 호출한다.
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        //버퍼를 닫아준다.
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }//finally 부분
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void parseJsonData(String jsonResponse){
        try
        {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for(int i=0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ids.add(jsonObject.getString("id"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}