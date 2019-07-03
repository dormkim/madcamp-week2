package com.example.second;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;

public class RecyclerImageTextAdapter extends RecyclerView
        .Adapter<RecyclerImageTextAdapter.ViewHolder> implements SectionTitleProvider {
    private ArrayList<ContactRecyclerItem> mData = null ;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon ;
        ImageView call ;
        ImageView msg ;
        TextView name ;
        TextView phone ;
        View mView;

        ViewHolder(View itemView) {
            super(itemView) ;
            // 뷰 객체에 대한 참조. (hold strong reference)
            mView = itemView;
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name) ;
            phone = itemView.findViewById(R.id.phone) ;
            call = itemView.findViewById(R.id.call);
            msg = itemView.findViewById(R.id.msg);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    RecyclerImageTextAdapter(ArrayList<ContactRecyclerItem> list) {
        mData = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.contact_recyclerview_item, parent, false) ;
        ViewHolder vh = new ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(ViewHolder holder,final int position) {

        final ContactRecyclerItem item = mData.get(position) ;

        holder.icon.setImageDrawable(item.getIcon()) ;
        holder.name.setText(item.getName()) ;
        holder.phone.setText(item.getPhone()) ;

        holder.mView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tel = "tel:"+item.getPhone();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(tel));
                v.getContext().startActivity(intent);
            }
        });

        holder.call.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tel = "tel:"+item.getPhone();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(tel));
                v.getContext().startActivity(intent);
            }
        });

        holder.msg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tel = "smsto:"+item.getPhone();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(tel));
                v.getContext().startActivity(intent);
            }
        });

    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

    @Override
    public String getSectionTitle(int position) {
        //this String will be shown in a bubble for specified position
        return mData.get(position).getName().substring(0, 1);
    }
}