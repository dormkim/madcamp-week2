package com.example.second;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.example.second.R;
import com.example.second.TAB1.SelectContactOnDB;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SelectTag extends AppCompatActivity {

    private ArrayList<String> addList = new ArrayList<>();
    private ArrayList<String> deleteList = new ArrayList<>();
    private String original_Tagname = null;
    private String Tagname = null;
    private TextView selected_item_textview;
    private int SELECT_CONTACT_ON_DB = 4;
    private int SELECT_GALLERY_ON_DB = 5;

    private String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_tag);

        final ListView listview = findViewById(R.id.listview);
        selected_item_textview = findViewById(R.id.selected_item_textview);

        Button btn_check = findViewById(R.id.btn_check);
        FloatingActionButton btn_insert = findViewById(R.id.btn_insert);
        final FloatingActionButton btn_delete = findViewById(R.id.btn_delete);
        final EditText saveName = findViewById(R.id.save_name);

        Intent intent = getIntent();

        //데이터를 저장하게 되는 리스트
        final List<String> list;
        list = intent.getStringArrayListExtra("dbList");
        original_Tagname = intent.getStringExtra("tagName");
        user_email = intent.getStringExtra("user_email");

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);

        //리스트뷰의 아이템을 클릭시 해당 아이템의 문자열을 가져오기 위한 처리
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                //클릭한 아이템의 문자열을 가져옴
                Tagname = (String)adapterView.getItemAtPosition(position);
                //텍스트뷰에 출력
                selected_item_textview.setText(Tagname);
                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(position > 0 && position < list.size()) {
                            deleteList.add(Tagname);
                            list.remove(position);
                            listview.clearChoices();
                            selected_item_textview.setText("아래 목록에서 선택하세요!");
                            tagDelete();
                            adapter.notifyDataSetChanged();
                        }
                        else if(position == 0){
                            Toast.makeText(getApplicationContext(), "All은 삭제 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //항목 추가
        btn_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = saveName.getText().toString();
                if(!list.contains(name) && !name.equals("")) {
                    list.add(name);
                    addList.add(name);
                    adapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(getApplicationContext(),"중복된 항목이나 빈 항목은 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //버튼 확인체크
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!addList.contains(Tagname) || original_Tagname.equals(Tagname)){
                    Intent intent = new Intent();
                    intent.putExtra("Tagname", Tagname);
                    intent.putExtra("deleteList", deleteList);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else if(Tagname != null){
                    String mode = getIntent().getStringExtra("mode");
                    if(mode.equals("contact")){
                        Intent selectIntent = new Intent(getApplicationContext(), SelectContactOnDB.class);
                        selectIntent.putExtra("user_email", getIntent().getStringExtra("user_email"));
                        startActivityForResult(selectIntent, SELECT_CONTACT_ON_DB);
                    }
                    else if(mode.equals("gallery")){
                        Intent selectIntent = new Intent(getApplicationContext(), SelectGalleryOnDB.class);
                        selectIntent.putExtra("user_email", getIntent().getStringExtra("user_email"));
                        startActivityForResult(selectIntent, SELECT_GALLERY_ON_DB);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "항목을 선택하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra("Tagname", Tagname);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void tagDelete(){
        try {
            new JSONTaskDeleteObj().execute("http://143.248.38.46:8080/contacts/tag/" + Tagname + "/" + user_email).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Tagname = null;
    }

    //Tag 전체 삭제
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
}
