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
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private JSONObject add_Contact;
    private JSONArray all_contact;
    private int mCount = 0;
    private long now1;
    private long now7;
    private ArrayList<String> dbList = new ArrayList<>();
    private ArrayList<Integer> selectList = new ArrayList<>();
    private static final int ADD_CONTACT = 1;
    private static final int SELECT_CONTACT = 2;
    private String Tag = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_1, container, false);
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        mMyData = getContactList();
        if (!dbList.contains(Tag)) {
            dbList.add(Tag);
            try {
                all_contact = ArrListToJArr(mMyData, Tag);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new JSONTaskPostArr().execute("http://143.248.38.46:8080/api/contacts/initialize");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


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

        mCount = 0;
        FloatingActionButton btn_change = view.findViewById(R.id.btn_change);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCount++;
                if(mCount == 1){
                    now1 = System.currentTimeMillis();
                }
                if(mCount > 6){
                    now7 = System.currentTimeMillis();
                    if((now7 - now1)/1000.0 < 5) {
                        Intent intent = new Intent(getActivity(), SelectContact.class);
                        intent.putExtra("tagName", Tag);
                        intent.putExtra("dbList", dbList);
                        startActivityForResult(intent, SELECT_CONTACT);
                    }
                    else{
                        mCount = 0;
                    }
                }
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
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode) {
            //연락처에 저장하는 클래스로 넘어가서 전화번호 정보를 받아와 mMyData에 추가하고 DB에 추가
            case ADD_CONTACT:
                Drawable drawable;
                String name = data.getStringExtra("contact_name");
                String number = data.getStringExtra("contact_phone");
                String photo = data.getStringExtra("contact_uri");

                ContactRecyclerItem contactItem = new ContactRecyclerItem();
                contactItem.setName(name);
                contactItem.setPhone(number);

                Bitmap bm = null;
                try {
                    if (photo != null) {
                        bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(photo));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bm == null)
                    drawable = getResources().getDrawable(R.drawable.photo_icon);
                else {
                    Bitmap resize_bm = resizingBitmap(bm);
                    drawable = new BitmapDrawable(getResources(), resize_bm);
                }
                contactItem.setIcon(drawable);
                mMyData.add(contactItem);
                try {
                    addContact(contactItem);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case SELECT_CONTACT:
                String dbTag = data.getStringExtra("Tagname");
                Log.i("Success","string : "+dbTag);
                if(!Tag.equals(dbTag)) {
                    Tag=dbTag;
                }
            }
        }
    }

    /*Add Contact 하고 DB에 올림*/
    private void addContact(ContactRecyclerItem contactItem) throws JSONException {
        Drawable temp = contactItem.getIcon();
        Bitmap bitmap = ((BitmapDrawable)temp).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        String str = Base64.encode(bitmapdata);

        JSONObject sObj = new JSONObject();
        sObj.put("name", contactItem.getName());
        sObj.put("phonenumber", contactItem.getPhone());
        sObj.put("icon",str);
        sObj.put("contact_id",contactItem.getContactId());
        sObj.put("tag", Tag);
        add_Contact = sObj;
        new JSONTaskPostObj().execute("http://143.248.38.46:8080/api/contacts");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!Tag.equals("All")){

            JSONObject Obj = new JSONObject();
            Obj.put("name", contactItem.getName());
            Obj.put("phonenumber", contactItem.getPhone());
            Obj.put("icon",str);
            Obj.put("contact_id",contactItem.getContactId());
            Obj.put("tag", "All");
            add_Contact = Obj;
            new JSONTaskPostObj().execute("http://143.248.38.46:8080/api/contacts");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Delete Contact 하고 DB에서 지움
    public void removeContact(View v, int position) {
        new JSONTaskDeleteObj().execute("http://143.248.38.46:8080/api/contacts/tag/"+Tag+"/phonenumber/"+mMyData.get(position).getPhone());
        v.getContext().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts.CONTACT_ID + "=" + mMyData.get(position).getContactId(), null);
        mMyData.remove(position);
        mAdapter.notifyItemRemoved(position);
        onResume();
    }

    //전체 연락처들의 Array를 만들어 DB에 추가
    public class JSONTaskPostArr extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                JSONArray jsonObject = all_contact;
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

    //하나의 contact 추가
    public class JSONTaskPostObj extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                JSONObject jsonObject = add_Contact;
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

    //하나의 contact 삭제
    public class JSONTaskDeleteObj extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("DELETE");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

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

    //연락처를 Array 형태로 만들어서 Tag를 추가함
    public JSONArray ArrListToJArr(ArrayList<ContactRecyclerItem> arrList, String dbTag) throws JSONException {

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
            sObj.put("icon",str);
            sObj.put("contact_id",contactItem.getContactId());
            sObj.put("tag", dbTag);
            jArray.put(sObj);
        }
        return jArray;
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
                contactItem.setContactId(contact_id);
                contactItem.setIconID(photo_id);
                contactItem.setPersonID(person_id);
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
}
