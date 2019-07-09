package com.example.second;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    String user_email;
    Boolean db_exist;

    private String[] permissions = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int MULTIPLE_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try { PackageInfo info = getPackageManager().getPackageInfo("com.example.project1", PackageManager.GET_SIGNATURES); for (Signature signature : info.signatures) { MessageDigest md = MessageDigest.getInstance("SHA"); md.update(signature.toByteArray()); String str = Base64.encodeToString(md.digest(), Base64.DEFAULT); Log.d("KeyHash:", str); Toast.makeText(this, str, Toast.LENGTH_LONG).show(); } }catch(NoSuchAlgorithmException e){ e.printStackTrace(); }catch (PackageManager.NameNotFoundException e){ e.printStackTrace(); }


        user_email = getIntent().getStringExtra("user_email");
        db_exist = getIntent().getBooleanExtra("db_exist", false);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Play boy");
        ab.setSubtitle(getIntent().getStringExtra("name"));
        checkPermissions();
    }

    protected void initial_set(){
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        else{
            initial_set();
        }
        return true;
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "모든 권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                                return;
                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                    return;
                }
            }
            initial_set();
        }
    }
}
