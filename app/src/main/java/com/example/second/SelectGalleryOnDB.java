package com.example.second;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.concurrent.ExecutionException;

public class SelectGalleryOnDB extends AppCompatActivity {

    private TextView selected_item_textview;
    private ArrayList<AlbumRecyclerItem> select_Data = new ArrayList<>();
    private ArrayList<Integer> select_num = new ArrayList<>();
    private ArrayList<Select_Image> select_images = new ArrayList<>();
    private String user_email;
    private String ip = "13.124.13.185:8080";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectcontact_item_on_db);
        user_email = getIntent().getStringExtra("user_email");
        try {
            new JSONTaskGet().execute("http://" + ip + "/images/tag/All/" + user_email).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final ListView listview = findViewById(R.id.listview);
        selected_item_textview = findViewById(R.id.selected_item_textview);
        Button btn_check = findViewById(R.id.btn_check);

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        ListViewAdapter adapter = new ListViewAdapter();

        for (int i = 0; i < select_Data.size(); i++) {
            String icon = select_Data.get(i).getitemPath();
            BitmapFactory.Options bo = new BitmapFactory.Options();
            bo.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(icon, bo);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            adapter.add(drawable, select_Data.get(i).getPhoto_id());
        }

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);

        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!select_num.contains(i)){
                    select_num.add(i);
                }
            }
        });

        //버튼 확인체크
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(select_num);
                for(int i = 0; i < select_num.size(); i++){
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(select_Data.get(select_num.get(i)).getitemPath(), bmOptions);
                    select_images.add(new Select_Image(bitmap, select_Data.get(i).getitemPath()));
                }
                try {
                    setGallery();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }


    public class ListViewAdapter extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<ImageListView> listViewItemList = new ArrayList<>() ;

        // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
        @Override
        public int getCount() {
            return listViewItemList.size() ;
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.selectimage_item, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = convertView.findViewById(R.id.image) ;
            TextView titleTextView = convertView.findViewById(R.id.image_name) ;

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ImageListView listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            iconImageView.setImageDrawable(listViewItem.getIcon());
            titleTextView.setText(listViewItem.getName());

            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position ;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position) ;
        }

        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void add(Drawable icon, String title) {
            ImageListView item = new ImageListView(null, null);

            item.setIcon(icon);
            item.setName(title);

            listViewItemList.add(item);
        }
    }

    private String getDirectoryPath(){
        //return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/";
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/madcamp/";
    }

    //갤러리 리셋 후 저장
    private void setGallery() throws ExecutionException, InterruptedException {
        String GalleryDir = getDirectoryPath();
        File fileDir = new File(GalleryDir);
        String[] files = fileDir.list();
        if(files != null) {
            for(int i = 0; i < files.length; i++){
                File file = new File(GalleryDir + files[i]);
                file.delete();
                gallery_update(GalleryDir + files[i]);
            }
        }
        //selectdata에서 해당하는 놈들을 불러와서 갤러리에 업데이트....를 해야하는데

        for(int i = 0; i < select_images.size(); i++) {
            File fileCacheItem = new File(select_images.get(i).filepath);
            OutputStream out = null;
            try {
                out = new FileOutputStream(fileCacheItem);
                select_images.get(i).bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            gallery_update(select_images.get(i).filepath);
        }
    }

    //리셋 후 다 불러옴
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
                    Log.i("asdfasdf", buffer.toString());
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

    private void parseJsonData(String jsonResponse) throws IOException {
        try
        {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for(int i=0;i<jsonArray.length();i++)
            {
                AlbumRecyclerItem item = new AlbumRecyclerItem(null, null);
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String filepath = getDirectoryPath() + jsonObject.getString("title");
                item.setItemPath(filepath);
                item.setPhoto_id(jsonObject.getString("title"));
                select_Data.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    //갤러리 업데이트
    private void gallery_update(String filePath) {
        Intent scan_intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri conURI = Uri.fromFile(f);
        scan_intent.setData(conURI);
        getApplicationContext().sendBroadcast(scan_intent);
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

    public class Select_Image{
        private Bitmap bitmap;
        private String filepath;

        Select_Image(Bitmap bitmap, String filepath){
            this.bitmap = bitmap;
            this.filepath = filepath;
        }
    }
}
