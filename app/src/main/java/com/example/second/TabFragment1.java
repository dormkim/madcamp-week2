package com.example.second;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class TabFragment1 extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerImageTextAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ContactRecyclerItem> mMyData;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_1, container, false);
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        initDataset();
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
                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
            }
        });
    }

    public void initDataset() {
        mMyData = getContactList();
        ContactRecyclerItem contactItem;

        for (int i=0; i<mMyData.size(); i++){
            contactItem = mMyData.get(i);
            Drawable drawable;
            Bitmap bm = loadContactPhoto(getActivity().getContentResolver(), contactItem.getPersonID(), contactItem.getIconID());
            if(bm == null)
                drawable = getResources().getDrawable(R.drawable.basic_icon);
            else {
                drawable = new BitmapDrawable(getResources(), bm);
            }
            contactItem.setIcon(drawable);
        }
    }

    public ArrayList<ContactRecyclerItem> getContactList(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.Contacts._ID
        };
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, sortOrder);
        ArrayList<ContactRecyclerItem> contactItems = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                long photo_id = cursor.getLong(2);
                long person_id = cursor.getLong(3);
                ContactRecyclerItem contactItem = new ContactRecyclerItem();
                contactItem.setName(cursor.getString(1));
                contactItem.setPhone(cursor.getString(0));
                contactItem.setIconID(photo_id);
                contactItem.setPersonID(person_id);

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

    public JSONObject ArrListToJObj(ArrayList<ContactRecyclerItem> arrList, String name){
        JSONObject obj = new JSONObject();
        try{
            JSONArray jArray = new JSONArray();
            for(int i=0; i<arrList.size(); i++){

                ContactRecyclerItem contactItem;
                contactItem = arrList.get(i);

                JSONObject sObj = new JSONObject();
                sObj.put("name", contactItem.getName());
                sObj.put("phonenumber", contactItem.getPhone());
                sObj.put("iconID", contactItem.getPhone());
                sObj.put("pID", contactItem.getPersonID());
                sObj.put("iconDrawable", contactItem.getIcon());
                jArray.put(sObj);
            }
            obj.put("filename", name);
            obj.put("ContactData", jArray);

        }catch (JSONException e){
            e.printStackTrace();
        }
        return obj;
    }

     /*   public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("title", "androidTest");
                jsonObject.accumulate("author", "yun");
                jsonObject.accumulate("published_date", "2019-03-15");
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
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
                    String line = null;

                    //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String… urls) 니까
                    return buffer.toString();
                    //아래는 예외처리 부분이다.
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    //종료가 되면 disconnect메소드를 호출한다.

                    if(con != null){
                        con.disconnect();
                    }

                    try {
                        //버퍼를 닫아준다.
                        if(reader != null){
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

        //doInBackground메소드가 끝나면 여기로 와서 텍스트뷰의 값을 바꿔준다.

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            tvData.setText(result);
        }
    }*/
}
