package com.example.second;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.second.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static androidx.constraintlayout.widget.Constraints.TAG;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;

public class TabFragment3 extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 10;
    ImageView imageView1, imageView2 , imageView3, imageView4;
    Button button1, button2, button3, button4;
    Bitmap backgroundImage, secretImage, encryptedImage1, encryptedImage2;
    boolean bgInitial = false;
    boolean scInitial = false;
    boolean bgJustNow = false;
    ProgressBar simpleProgressBar;

    public TabFragment3() {
// Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_3, container, false);

        setHasOptionsMenu(true);

        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        imageView1 = view.findViewById(R.id.imageView1);
        imageView2 = view.findViewById(R.id.imageView2);
        imageView3 = view.findViewById(R.id.imageView3);
        imageView4 = view.findViewById(R.id.imageView4);
        simpleProgressBar = (ProgressBar) view.findViewById(R.id.simpleProgressBar);
        simpleProgressBar.setVisibility(View.INVISIBLE);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
                bgInitial = true;
                bgJustNow = true;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                pickFromGallery();
                scInitial = true;
                bgJustNow = false;
            }
        });

        button3.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick (View view) {

                if (bgInitial && scInitial) {

                    if (backgroundImage.getHeight() != secretImage.getHeight() || backgroundImage.getWidth() != secretImage.getWidth()){
                        Toast.makeText(getContext(), "Cannot encrypt! Images have different sizes!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getContext(), "Encryption In Progress", Toast.LENGTH_SHORT).show();
                        new EncryptTask().execute(backgroundImage, secretImage);
                    }
                }
                else {
                    Snackbar.make(view, "Please Choose Both Background and Secret Images", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        button4.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/DCIM/Encryption");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname1 = "Image-" + n + ".jpg";
                n = generator.nextInt(n);
                String fname2 = "Image-" + n + ".jpg";
                File file1 = new File(myDir, fname1);
                File file2 = new File(myDir, fname2);
                Log.i(TAG, "" + file1);
                Log.i(TAG, "" + file2);
                if (file1.exists())
                    file1.delete();
                if (file2.exists())
                    file2.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file1);
                    encryptedImage1.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    scanFile( myDir + fname1);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file1));

                    getActivity().sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream out = new FileOutputStream(file2);
                    encryptedImage2.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    scanFile( myDir + fname2);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file2));

                    getActivity().sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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
        menu.findItem(R.id.action_camera).setVisible(false);
        menu.findItem(R.id.action_album).setVisible(false);

    }

    private void scanFile(String path) {
        MediaScannerConnection.scanFile(getContext(), new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.d("Tag", "Scan finished. You can view the image in the gallery now.");
            }
        });
    }

    private void setProgressValue (final int progress) { simpleProgressBar.setProgress(progress);}

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                        if (bgJustNow) {
                            imageView1.setImageURI(selectedImage);
                            backgroundImage = bitmap;
                        }
                        else {
                            secretImage = bitmap;
                            imageView2.setImageURI(selectedImage);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
            }
    }

    private class EncryptTask extends AsyncTask<Bitmap, Integer, ArrayList<Bitmap>> {
        protected void onPreExecute(){
            simpleProgressBar.setVisibility(View.VISIBLE);
            setProgressValue(0);
            imageView3.setImageResource(R.drawable.ic_launcher_background);
            imageView4.setImageResource(R.drawable.ic_launcher_background);
        }

        protected ArrayList<Bitmap> doInBackground(Bitmap... imgs) {
            encryptedImage1 = createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), ARGB_8888);
            encryptedImage2 = createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), ARGB_8888);
            ArrayList<Bitmap> result = new ArrayList<Bitmap>();
            int p, a1, a2, r1, r2, g1, g2, b1, b2, a, r, g, b;

            if (backgroundImage.getHeight() != secretImage.getHeight() || backgroundImage.getWidth() != secretImage.getWidth())
            {
                result.add(backgroundImage);
                result.add(secretImage);
                return result;
            }

            for (int i = 0; i < backgroundImage.getHeight(); i++) {
                for (int j = 0; j < backgroundImage.getWidth(); j++) {
                    p = backgroundImage.getPixel(j, i);
                    a = (p >> 24) & 0xf0;
                    r = (p >> 16) & 0xf0;
                    g = (p >> 8) & 0xf0;
                    b = p & 0xf0;
                    p = secretImage.getPixel(j, i);
                    a2 = (p >> 24) & 0xff;
                    r2 = (p >> 16) & 0xff;
                    g2 = (p >> 8) & 0xff;
                    b2 = p & 0xff;
                    a1 = (a + (a2 / 16)) << 24;
                    r1 = (r + (r2 / 16)) << 16;
                    g1 = (g + (g2 / 16)) << 8;
                    b1 = (b + (b2 / 16));
                    a2 = (a + (a2 % 16)) << 24;
                    r2 = (r + (r2 % 16)) << 16;
                    g2 = (g + (g2 % 16)) << 8;
                    b2 = (b + (b2 % 16));

                    try {
                        encryptedImage1.setPixel(j, i, a1 + r1 + g1 + b1);
                        encryptedImage2.setPixel(j, i, a2 + r2 + g2 + b2);
                    } catch (IllegalStateException z) {
                        z.printStackTrace();
                    }
                }

                if (i % 100 == 0) {
                    publishProgress((int)((i * 100.0 / backgroundImage.getHeight())));
                }
            }
            result.add(encryptedImage1);
            result.add(encryptedImage2);
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
            setProgressValue(progress[0]);
        }

        protected void onPostExecute(ArrayList<Bitmap> result) {
            Toast.makeText(getContext(), "Encryption complete", Toast.LENGTH_SHORT).show();
            simpleProgressBar.setVisibility(View.INVISIBLE);
            imageView3.setImageBitmap(result.get(0));
            imageView4.setImageBitmap(result.get(1));
        }
    }
}
