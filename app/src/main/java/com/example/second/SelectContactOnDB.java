package com.example.second;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.bson.internal.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class SelectContactOnDB extends AppCompatActivity {

    private TextView selected_item_textview;
    private ArrayList<ContactRecyclerItem> original_Data = new ArrayList<>();
    private ArrayList<ContactRecyclerItem> select_Data = new ArrayList<>();
    private ArrayList<Integer> select_num = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectcontact_item_on_db);
        new JSONTaskGet().execute("http://143.248.38.46:8080/api/contacts/tag/All");

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final ListView listview = findViewById(R.id.listview);
        selected_item_textview = findViewById(R.id.selected_item_textview);
        Button btn_check = findViewById(R.id.btn_check);

        //데이터를 저장하게 되는 리스트
        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < select_Data.size(); i++) {
            list.add(select_Data.get(i).getName());
        }
        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, list);

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);

        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!select_num.contains(i)) {
                    select_num.add(i);
                }
            }
        });

        //버튼 확인체크
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(select_num);
                setContacts();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    //연락처 리셋하는 부분
    public void setContacts(){
        original_Data = getContactList();
        int size = original_Data.size();
        for(int i = 0; i < size; i++) {
            getApplication().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts.CONTACT_ID + "=" + original_Data.get(i).getContactId(), null);
        }

        for(int i = 0; i < select_num.size();i++){
            String name = select_Data.get(select_num.get(i)).getName();
            String phone = select_Data.get(select_num.get(i)).getPhone();
            Drawable icon = select_Data.get(select_num.get(i)).getIcon();
            addtoContacts(name, phone, icon);
        }
        Log.i("success", "reset");
    }
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
                ContactRecyclerItem item = new ContactRecyclerItem();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                item.setName(jsonObject.getString("name"));
                item.setPhone(jsonObject.getString("phonenumber"));
                item.setContactId(jsonObject.getString("contact_id"));
                String icon = jsonObject.getString("icon");
                byte[] bytes = Base64.decode(icon);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                item.setIcon(drawable);
                select_Data.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /*전화번호부 로컬에 저장하기*/
    public void addtoContacts(String name, String phone, Drawable icon){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());


        if(icon != null) {
            Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
            //byte[]어레이 변환
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] bytes = stream.toByteArray();

            //사진 추가
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes)
                    .build());
        }

        try {
            getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(getApplicationContext(), "연락처가 저장되었습니다.", Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("ContactsAdder", "Exceptoin encoutered while inserting contact: " + e);
        }
    }

    public ArrayList<ContactRecyclerItem> getContactList(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        };
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cursor = getApplication().getContentResolver().query(uri, projection, null, null, sortOrder);
        ArrayList<ContactRecyclerItem> contactItems = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                String contact_id = cursor.getString(0);
                ContactRecyclerItem contactItem = new ContactRecyclerItem();
                contactItem.setContactId(contact_id);
                contactItems.add(contactItem);
            }while (cursor.moveToNext());
        }
        return contactItems;
    }
}
