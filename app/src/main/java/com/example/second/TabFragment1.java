package com.example.second;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TabFragment1 extends Fragment{
    private RecyclerView mRecyclerView;
    private RecyclerImageTextAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ContactRecyclerItem> mMyData;
    private View view;
    private static final int ADD_CONTACT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_1, container, false);
        mMyData = getContactList();
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
}
