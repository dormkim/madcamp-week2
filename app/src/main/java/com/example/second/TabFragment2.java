package com.example.second;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.bson.internal.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static android.view.View.VISIBLE;

public class TabFragment2 extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerImageAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ArrayList<AlbumRecyclerItem> mMyData = new ArrayList<>();
    private View view;
    private ImageView imageView;
    private Button btn_back;
    private Button btn_delete;
    private Button btn_check;
    private String ip = "13.124.13.185:8080";

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int SELECT_GALLERY = 3;

    private JSONArray all_images;
    private JSONObject add_image;

    private String mCurrentPath;
    private Uri photoURI;

    private int mCount = 0;
    private long now1;
    private long now7;

    private String Tag = "All";
    private String user_email;

    private ArrayList<String> dbList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_2, container, false);
        user_email = ((MainActivity)getActivity()).user_email;
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"/Camera");
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"/madcamp");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.actionbar_actions, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_logout).setVisible(true);
        menu.findItem(R.id.action_addcontact).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(true);
        menu.findItem(R.id.action_camera).setVisible(true);
        menu.findItem(R.id.action_album).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.action_logout:
                Intent logoutintent = new Intent(getActivity(), LoginActivity.class);
                startActivity(logoutintent);
                getActivity().finish();
                break;
            case R.id.action_camera:
                takePhoto();
                break;
            case R.id.action_album:
                goToAlbum();
                break;
            case R.id.action_refresh:
                mCount++;
                if(mCount == 1){
                    now1 = System.currentTimeMillis();
                }
                if(mCount > 6){
                    now7 = System.currentTimeMillis();
                    if((now7 - now1)/1000.0 < 5) {
                        Intent intent = new Intent(getActivity(), SelectTag.class);
                        intent.putExtra("mode","gallery");
                        intent.putExtra("tagName", Tag);
                        intent.putExtra("dbList", dbList);
                        intent.putExtra("user_email",user_email);
                        startActivityForResult(intent, SELECT_GALLERY);
                    }
                    else{
                        mCount = 0;
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        //갤러리의 사진을 갤러리 아이템에 저장 이 리스트를 array로 만들어 Tag를 붙여 보냄.
        initDataset();
        //dbList에 없을 때 서버에 보냄.
        if(!dbList.contains(Tag)){
            dbList.add(Tag);
            try {
                all_images = ArrListToJArr(mMyData, Tag);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                new JSONTaskPostArr().execute("http://" + ip + "/images/initialize").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        mRecyclerView = view.findViewById(R.id.album_recycler);
        mRecyclerView.setHasFixedSize(true);
        int numofCol = 4;
        mGridLayoutManager = new GridLayoutManager(getActivity(), numofCol);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.scrollToPosition(0);
        mAdapter = new RecyclerImageAdapter(mMyData);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mCount = 0;

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if(child!=null && gestureDetector.onTouchEvent(e)){
                    /*터치하고 뗐을때*/
                    int position = mRecyclerView.getChildLayoutPosition(child);
                    getInfo(position);
                }
                return false;
            }
            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        imageView = view.findViewById(R.id.select_photo);
        btn_back = view.findViewById(R.id.btn_back);
        btn_delete = view.findViewById(R.id.btn_delete);
        btn_check = view.findViewById(R.id.btn_check);
        imageView.setVisibility(View.GONE);
        btn_back.setVisibility(View.GONE);
        btn_delete.setVisibility(View.GONE);
        btn_check.setVisibility(View.GONE);
    }

    public void initDataset() {
        mMyData.clear();
        String GalleryDir = getDirectoryPath();
        File fileDir = new File(GalleryDir);
        String[] imageFileNameArr = fileDir.list();

        if(imageFileNameArr != null) {
            for (int i = 0; i < imageFileNameArr.length; i++) {
                AlbumRecyclerItem item = new AlbumRecyclerItem(GalleryDir + imageFileNameArr[i], imageFileNameArr[i]);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(new File(GalleryDir + imageFileNameArr[i])));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(GalleryDir + imageFileNameArr[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int exifOrientation;
                int exifDegree = 0;

                if (exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegrees(exifOrientation);
                }

                Bitmap rotate_bitmap = rotate(bitmap, exifDegree);
                File savepath = new File(GalleryDir + imageFileNameArr[i]);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(savepath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                rotate_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMyData.add(item);
            }
        }
    }

    private String getDirectoryPath(){
        //return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/";
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/madcamp/";
    }

    //새로운 파일 이름 만들기
    private File createImageFile() {
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(mDate);
        String imageFileName = "madcamp_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/madcamp");
        //File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/Camera");
        imageFile = new File(storageDir, imageFileName);
        mCurrentPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    //각 경우별로 함수 어떻게 돌릴지
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            String path = null;
            if (requestCode == PICK_FROM_ALBUM) {
                if (data == null) {
                    return;
                }
                photoURI = data.getData();
                if (photoURI != null) {
                    // Uri - bitmap 변환
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoURI);
                        if (bitmap != null) {
                            String[] proj = {MediaStore.Images.Media.DATA};

                            Cursor cursor = getContext().getContentResolver().query(photoURI, proj, null, null, null);
                            cursor.moveToNext();
                            path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                            cursor.close();
                            ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            int exifOrientation;
                            int exifDegree = 0;

                            if (exif != null) {
                                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                exifDegree = exifOrientationToDegrees(exifOrientation);
                            }

                            String[] fileName = path.split("/");
                            Bitmap rotate_bitmap = rotate(bitmap, exifDegree);
                            File savepath = new File(getDirectoryPath() + fileName[fileName.length - 1]);
                            FileOutputStream fos = new FileOutputStream(savepath);
                            rotate_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();

                            gallery_update(true, getDirectoryPath() + fileName[fileName.length - 1]);
                            mAdapter.notifyDataSetChanged();
                        }
                    }catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == PICK_FROM_CAMERA) {
                path = mCurrentPath;

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(new File(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int exifOrientation;
                int exifDegree = 0;

                if (exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegrees(exifOrientation);
                }

                Bitmap rotate_bitmap = rotate(bitmap, exifDegree);
                File savepath = new File(path);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(savepath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                rotate_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    gallery_update(true, path);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
            }
            else if(requestCode == SELECT_GALLERY) {
                String dbTag = data.getStringExtra("Tagname");
                ArrayList<String> deleteList = data.getStringArrayListExtra("deleteList");
                if (deleteList != null) {
                    for (int i = 0; i < deleteList.size(); i++) {
                        if (dbList.contains(deleteList.get(i))) {
                            dbList.remove(deleteList.get(i));
                        }
                    }
                }
                if (!Tag.equals(dbTag)) {
                    if (dbList.contains(dbTag)) {
                        try {
                            resetGallery();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            new JSONTaskGet().execute("http://" + ip + "/images/tag/" + dbTag + "/" +user_email).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Tag = dbTag;
            }
        }
    }

    //갤러리에 사진 추가시 해서 목록을 띄워줌(갤러리에)
    private void gallery_update(boolean addphoto, String filePath) throws ExecutionException, InterruptedException {
        Intent scan_intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri conURI = Uri.fromFile(f);
        scan_intent.setData(conURI);
        getActivity().sendBroadcast(scan_intent);

        if(addphoto) {
            String [] imageName = filePath.split("/");
            AlbumRecyclerItem item = new AlbumRecyclerItem(filePath, imageName[imageName.length - 1]);
            mMyData.add(item);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath,bmOptions);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();
            String str = Base64.encode(bitmapdata);

            JSONObject Obj = new JSONObject();
            try {
                Obj.put("photo", str);
                Obj.put("email",user_email);
                Obj.put("tag",Tag);
                Obj.put("title", item.getPhoto_id());
                add_image = Obj;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new JSONTaskPostObj().execute("http://" + ip + "/images").get();

            if(Tag != "All"){
                JSONObject sObj = new JSONObject();
                try {
                    sObj.put("photo", str);
                    sObj.put("email",user_email);
                    sObj.put("tag","All");
                    sObj.put("title", item.getPhoto_id());
                    add_image = sObj;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new JSONTaskPostObj().execute("http://" + ip + "/images").get();
            }
        }
    }

    //이미지들을 Array 형태로 만들어서 Tag를 추가함
    public JSONArray ArrListToJArr(ArrayList<AlbumRecyclerItem> arrList, String dbTag) throws JSONException, IOException {

        JSONArray jArray = new JSONArray();
        for(int i = 0; i < arrList.size(); i++){

            AlbumRecyclerItem imageItem = arrList.get(i);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(imageItem.getitemPath(),bmOptions);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();
            String str = Base64.encode(bitmapdata);

            JSONObject sObj = new JSONObject();
            sObj.put("photo", str);
            sObj.put("title", mMyData.get(i).getPhoto_id());
            sObj.put("email",user_email);
            sObj.put("tag", dbTag);
            jArray.put(sObj);
        }
        return jArray;
    }

    //갤러리 전체를 Array로 만들어 DB에 추가
    public class JSONTaskPostArr extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                JSONArray jsonObject = all_images;
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

    //하나의 image 추가
    public class JSONTaskPostObj extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String urls[]) {
            try {
                JSONObject jsonObject = add_image;
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

    //하나의 image 삭제
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
                AlbumRecyclerItem item = new AlbumRecyclerItem(null, null);
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String photo = jsonObject.getString("photo");

                //갤러리에 불러온 사진들 다 저장
                byte[] bytes = Base64.decode(photo);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                String filepath = getDirectoryPath() + jsonObject.getString("title");
                FileOutputStream out = new FileOutputStream(filepath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

                item.setItemPath(filepath);
                item.setPhoto_id(jsonObject.getString("title"));

                try {
                    gallery_update(false, filepath);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mMyData.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //그리드에서 사진 뷰를 보여줌
    private void getInfo(final int position){
        final String getPath = mMyData.get(position).getitemPath();

        imageView.setVisibility(VISIBLE);
        btn_back.setVisibility(VISIBLE);

        imageView.setImageURI(Uri.parse((getPath)));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_delete.getVisibility() == VISIBLE){
                    btn_delete.setVisibility(View.GONE);
                }
                else {
                    btn_delete.setVisibility(VISIBLE);
                    btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //DB에서도 삭제
                            try {
                                getDelete(getPath, position);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            imageView.setVisibility(View.GONE);
                            btn_back.setVisibility(View.GONE);
                            btn_delete.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setVisibility(View.GONE);
                btn_back.setVisibility(View.GONE);
                btn_delete.setVisibility(View.GONE);
            }
        });
    }

    //사진 하나 삭제 할 때
    private void getDelete(String getPath, int position) throws ExecutionException, InterruptedException {
        File file = new File(getPath);
        file.delete();
        mCurrentPath = getPath;
        gallery_update(false, getPath);
        new JSONTaskDeleteObj().execute("http://" + ip + "/images/tag/" + Tag + "/title/" + mMyData.get(position).getPhoto_id() + "/" + user_email).get();
        mMyData.remove(position);
        mAdapter.notifyItemRemoved(position);
        Toast.makeText(getContext(),"사진 삭제 완료", Toast.LENGTH_SHORT).show();
    }

    //앨범에서 사진을 고를 때
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    //사진을 찍을때 - content와 file 경로의 차이를 확실히 파악
    private void takePhoto() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;

                photoFile = createImageFile();

                if (photoFile != null) {
                    Uri providerURI = FileProvider.getUriForFile(getContext(), "com.example.second.provider", photoFile);
                    //인텐트에 전달할때는 content로 구성된 uri를 보내야한다
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                }
            }
        } else {
            Toast.makeText(getContext(), "공간 접근 불가.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //갤러리 리셋
    private void resetGallery() throws ExecutionException, InterruptedException {
        File file;
        for (int i = 0; i < mMyData.size(); i++) {
            String filePath = mMyData.get(i).getitemPath();
            file = new File(filePath);
            file.delete();
            gallery_update(false, filePath);
        }
        mMyData.clear();
        mAdapter.notifyDataSetChanged();
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}