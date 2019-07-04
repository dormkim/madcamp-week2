package com.example.second;

import android.app.Activity;
import android.content.*;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.internal.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class TabFragment1 extends Fragment{
    private RecyclerView mRecyclerView;
    private RecyclerImageTextAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ContactRecyclerItem> mMyData;
    private View view;
    private JSONObject all_contact;
    private Button posttest;
    private Button gettest;
    private ImageView test;
    private static final int ADD_CONTACT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_1, container, false);
        mMyData = getContactList();
        all_contact = ArrListToJObj(mMyData, "All");
        posttest = (Button) view.findViewById(R.id.postTest);
        gettest = (Button) view.findViewById(R.id.getTest);

        posttest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTaskPost().execute("http://143.248.38.46:8080/api/contacts");
            }
        });

        gettest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONTaskGet().execute("http://143.248.38.46:8080/api/contacts/5d1e4c13e1af7459d4a22059");
            }
        });

        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.contact_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);
        mAdapter = new RecyclerImageTextAdapter(mMyData);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        FastScroller fastScroller = (FastScroller) view.findViewById(R.id.fastscroll);
        fastScroller.setRecyclerView(mRecyclerView);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addintent = new Intent(getActivity(), AddContact.class);
                startActivityForResult(addintent, ADD_CONTACT);
            }
        });

        mAdapter.setOnItemClickListener(new RecyclerImageTextAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int request_code) {
                removeContact(v, position);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case ADD_CONTACT:
                if(resultCode == Activity.RESULT_OK){
                    Drawable drawable;
                    String name = data.getStringExtra("contact_name");
                    String number = data.getStringExtra("contact_phone");
                    String photo = data.getStringExtra("contact_uri");

                    ContactRecyclerItem contactItem = new ContactRecyclerItem();
                    contactItem.setName(name);
                    contactItem.setPhone(number);

                    Bitmap bm = null;
                    try {
                        if(photo != null) {
                            bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(photo));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(bm == null)
                        drawable = getResources().getDrawable(R.drawable.photo_icon);
                    else {
                        Bitmap resize_bm = resizingBitmap(bm);
                        drawable = new BitmapDrawable(getResources(), resize_bm);
                    }
                    contactItem.setIcon(drawable);
                    mMyData.add(contactItem);
                    //JSON Object으로 만들어서 DB에 올림
                }
                break;
        }
    }

    /*처음으로 화면이 켜질때 전체 리스트를 DB에 올림(ALL_Contacts)*/
    public JSONObject ArrListToJObj(ArrayList<ContactRecyclerItem> arrList, String name){
        JSONObject obj = new JSONObject();
        try{
            JSONArray jArray = new JSONArray();
            for(int i=0; i<arrList.size(); i++){

                ContactRecyclerItem contactItem;
                contactItem = arrList.get(i);

                Drawable temp = contactItem.getIcon();
                Bitmap bitmap = ((BitmapDrawable)temp).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                String str = Base64.encode(bitmapdata);

                JSONObject sObj = new JSONObject();
                sObj.put("name", contactItem.getName());
                sObj.put("phonenumber", contactItem.getPhone());
                sObj.put("bitmapInfo",str);
                //sObj.put("iconID", contactItem.getPhone());
                //sObj.put("pID", contactItem.getPersonID());
                //sObj.put("contactID",contactItem.getContentId());
                //sObj.put("iconDrawable", contactItem.getIcon());
                jArray.put(sObj);
            }
            obj.put("name", name);
            obj.put("phonenumber", jArray);

        }catch (JSONException e){
            e.printStackTrace();
        }
        return obj;
    }


     public ArrayList<ContactRecyclerItem> getContactList(){
         Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
         String[] projection = new String[]{
                 ContactsContract.CommonDataKinds.Phone.NUMBER,
                 ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                 ContactsContract.Contacts.PHOTO_ID,
                 ContactsContract.Contacts._ID,
                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID
         };
         String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
         Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, sortOrder);
         ArrayList<ContactRecyclerItem> contactItems = new ArrayList<>();
         if(cursor.moveToFirst()){
             do{
                 Drawable drawable;
                 long photo_id = cursor.getLong(2);
                 long person_id = cursor.getLong(3);
                 String contact_id = cursor.getString(4);
                 ContactRecyclerItem contactItem = new ContactRecyclerItem();
                 contactItem.setName(cursor.getString(1));
                 contactItem.setPhone(cursor.getString(0));
                 contactItem.setIconID(photo_id);
                 contactItem.setPersonID(person_id);
                 contactItem.setContentId(contact_id);
                 Bitmap bm = loadContactPhoto(getActivity().getContentResolver(), contactItem.getPersonID(), contactItem.getIconID());
                 if(bm == null)
                     drawable = getResources().getDrawable(R.drawable.photo_icon);
                 else {
                     drawable = new BitmapDrawable(getResources(), bm);
                 }
                 contactItem.setIcon(drawable);
                 contactItems.add(contactItem);
             }while (cursor.moveToNext());
         }
         return contactItems;
     }

    public Bitmap loadContactPhoto(ContentResolver cr, long id, long photo_id){
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if(input != null)
            return resizingBitmap(BitmapFactory.decodeStream(input));

        byte[] photoBytes = null;
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photo_id);
        Cursor c = cr.query(photoUri, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null,null);
        try {
            if (c.moveToFirst())
                photoBytes = c.getBlob(0);
        } catch(Exception e){
            e.printStackTrace();
        }finally {
            c.close();
        }

        if(photoBytes != null)
            return resizingBitmap(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length));

        return null;
    }

    public Bitmap resizingBitmap(Bitmap oBitmap){
        if(oBitmap==null)
            return null;
        float width = oBitmap.getWidth();
        float height = oBitmap.getHeight();
        float resizing_size = 200;
        Bitmap rBitmap = null;
        if (width > resizing_size){
            float mWidth = (float)(width/100);
            float fScale = (float)(resizing_size/mWidth);
            width *= (fScale/100);
            height *= (fScale/100);
        }else if (height>resizing_size){
            float mHeight = (float) (height/100);
            float fScale = (float)(resizing_size/mHeight);
            width *= (fScale/100);
            height *= (fScale/100);
        }

        rBitmap = Bitmap.createScaledBitmap(oBitmap, (int)width, (int)height, true);
        return rBitmap;
    }

    public void removeContact(View v, int position) {
        v.getContext().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts.CONTACT_ID + "=" + mMyData.get(position).getContentId(), null);
        mMyData.remove(position);
        mAdapter.notifyItemRemoved(position);
        onResume();
    }

    public class JSONTaskPost extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String urls[]) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = all_contact;

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    //URL url = new URL("http://192.168.25.16:3000/users");
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
                    writer.close();//버퍼를 받아줌

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
            //tvData.setText(result);//서버로 부터 받은 값을 출력해주는 부
        }
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
            test = (ImageView) view.findViewById(R.id.test);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray jsonArray = (JSONArray) jsonObject.get("phonenumber");

            for(int i=0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String value1 = jsonObject1.getString("name");
                String value2 = jsonObject1.getString("phonenumber");
                String value3 = jsonObject1.getString("bitmapInfo");
                byte[] temp = Base64.decode(value3);
                Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
                test.setImageBitmap(bitmap);
                Thread.sleep(3000);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
