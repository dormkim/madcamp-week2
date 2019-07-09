package com.example.second;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;

public class RecyclerImageAdapter extends RecyclerView.Adapter<RecyclerImageAdapter.ViewHolder> {
    private ArrayList<AlbumRecyclerItem> mData = null ;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photo ;
        ViewHolder(View itemView) {
            super(itemView) ;
            // 뷰 객체에 대한 참조. (hold strong reference)
            photo = itemView.findViewById(R.id.photo);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    RecyclerImageAdapter(ArrayList<AlbumRecyclerItem> list) {
        mData = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.album_recyclerview_item, parent, false) ;
        ViewHolder vh = new ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AlbumRecyclerItem item = mData.get(position);
        BitmapFactory.Options bo = new BitmapFactory.Options();
        bo.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(item.getitemPath(), bo);
        if (bitmap != null) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(item.getitemPath());
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
            holder.photo.setImageBitmap(rotate_bitmap);
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
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