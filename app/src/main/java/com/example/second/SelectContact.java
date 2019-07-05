package com.example.second;

import android.content.Intent;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class SelectContact extends AppCompatActivity {

    private String Tagname = null;
    private TextView selected_item_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectcontact_item);

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
                            list.remove(position);
                            listview.clearChoices();
                            Tagname = null;
                            selected_item_textview.setText("아래 목록에서 선택하세요!");
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
                if(list.contains(name) == false) {
                    list.add(name);
                    adapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(getApplicationContext(),"중복된 항목은 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //버튼 확인체크
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Tagname != null){
                    Intent intent = new Intent();
                    intent.putExtra("Tagname", Tagname);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "항목을 선택하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
