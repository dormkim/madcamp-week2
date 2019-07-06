package com.example.second;

import android.app.Activity;
import android.graphics.Matrix;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

    FloatingActionButton btnCamera;
    FloatingActionButton btnAlbum;
    FloatingActionButton btnReset;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private String mCurrentPath;
    private Uri photoURI, albumURI, imageURI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_2, container, false);

        initDataset();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.album_recycler);
        mRecyclerView.setHasFixedSize(true);
        int numofCol = 4;
        mGridLayoutManager = new GridLayoutManager(getActivity(), numofCol);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.scrollToPosition(0);
        mAdapter = new RecyclerImageAdapter(mMyData);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        btnCamera = view.findViewById(R.id.btn_camera);
        btnAlbum = view.findViewById(R.id.btn_album);
        btnReset = view.findViewById(R.id.btn_reset);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAlbum();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_check.getVisibility() == VISIBLE){
                    btn_check.setVisibility(View.GONE);
                    btn_check.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btn_check.setVisibility(View.GONE);
                            resetGallery();
                        }
                    });
                }
                else{
                    btn_check.setVisibility(VISIBLE);
                    btn_check.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btn_check.setVisibility(View.GONE);
                            resetGallery();
                        }
                    });
                }
            }
        });


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
                    btnAlbum.hide();
                    btnCamera.hide();
                    btnReset.hide();
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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 갤러리에 폴더 추가  */
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/madcamp");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
    }

    public void initDataset() {
        mMyData.clear();
        String GalleryDir = getDirectoryPath();
        File fileDir = new File(GalleryDir);
        String[] imageFileNameArr = fileDir.list();

        for(int i = 0; i < imageFileNameArr.length; i++){
            AlbumRecyclerItem item = new AlbumRecyclerItem(GalleryDir + imageFileNameArr[i]);
            mMyData.add(item);
        }
    }

    private String getDirectoryPath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/madcamp/";
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
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    Uri providerURI = FileProvider.getUriForFile(getContext(), "com.example.tabbed_activity.provider", photoFile);
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

    //새로운 파일 이름 만들기
    private File createImageFile() throws IOException {
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(mDate);
        String imageFileName = "madcamp_" + timeStamp + ".PNG";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/madcamp");

        //이미 madcamp 폴더는 존재하므로 예외처리는 따로 안함
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
            if (requestCode == PICK_FROM_ALBUM) {
                if (data == null) {
                    return;
                }
                try {
                    File albumFile = null;
                    albumFile = createImageFile();
                    photoURI = data.getData();
                    albumURI = Uri.fromFile(albumFile);
                    cropImage();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "앨범에서 부르기가 실패되었습니다.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else if (requestCode == PICK_FROM_CAMERA) {
                File file = new File(mCurrentPath);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplication().getContentResolver(), Uri.fromFile(file));
                    if (bitmap != null) {
                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(mCurrentPath);
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
                        File savepath = new File(mCurrentPath);
                        FileOutputStream fos = new FileOutputStream(savepath);
                        rotate_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gallery_update(true);

            } else if (requestCode == CROP_FROM_CAMERA) {
                gallery_update(true);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    //갤러리에 사진 추가시 해서 목록을 띄워줌(갤러리에)
    private void gallery_update(boolean addphoto) {
        Intent scan_intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPath);
        Uri conURI = Uri.fromFile(f);
        scan_intent.setData(conURI);
        getActivity().sendBroadcast(scan_intent);
        if(addphoto) {
            AlbumRecyclerItem item = new AlbumRecyclerItem(mCurrentPath);
            mMyData.add(item);
        }
    }

    //앨범에서 사진을 불러와 crop할때 자르기.
    public void cropImage() {
        Intent crop_intent = new Intent("com.android.camera.action.CROP");
        crop_intent.setDataAndType(photoURI, "image/*");
        crop_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        crop_intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Toast.makeText(getContext(), "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

        crop_intent.putExtra("crop", "true");
        crop_intent.putExtra("aspectX", 1);
        crop_intent.putExtra("aspectY", 1);
        crop_intent.putExtra("scale", true);
        crop_intent.putExtra("return-data", true);
        crop_intent.putExtra("output", albumURI);

        startActivityForResult(crop_intent, CROP_FROM_CAMERA);
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

    private void getInfo(final int position){
        final String getPath = mMyData.get(position).getitemPath();

        imageView.setVisibility(VISIBLE);
        //textView.setVisibility(VISIBLE);
        btn_back.setVisibility(VISIBLE);

        imageView.setImageURI(Uri.parse((getPath)));
        //textView.setText(getName(getPath));

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
                            getDelete(getPath, position);
                            imageView.setVisibility(View.GONE);
                            btn_back.setVisibility(View.GONE);
                            btn_delete.setVisibility(View.GONE);
                            btnAlbum.show();
                            btnCamera.show();
                            btnReset.show();
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
                btnAlbum.show();
                btnCamera.show();
                btnReset.show();
            }
        });
    }

    private void getDelete(String getPath, int position){
        File file = new File(getPath);
        file.delete();
        mCurrentPath = getPath;
        gallery_update(false);
        mMyData.remove(position);
        mAdapter.notifyItemRemoved(position);
        Toast.makeText(getContext(),"사진 삭제 완료", Toast.LENGTH_SHORT).show();
    }

    private void resetGallery(){
        File file;
        for(int i = 0; i < mMyData.size(); i++){
            String filePath = mMyData.get(i).getitemPath();
            file = new File(filePath);
            file.delete();
            mCurrentPath = filePath;
            gallery_update(false);
        }
        mMyData.clear();
        mAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(),"갤러리 초기화", Toast.LENGTH_SHORT).show();
    }
}