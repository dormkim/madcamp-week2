package com.example.second;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.Random;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class TabFragment4 extends Fragment {
    private static final int GALLERY_REQUEST_CODE = 10;
    ImageView imageView1, imageView2, imageView3;
    Button button1, button2, button3, button4;
    Bitmap encryptedImage1, encryptedImage2, decryptedImage;
    ProgressBar simpleProgressBar;
    int count = 0;
    boolean hasImage1 = false;
    boolean hasImage2 = false;
    boolean img1JustNow = false;

    public TabFragment4() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_4, container, false);

        setHasOptionsMenu(true);

        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        imageView1 = view.findViewById(R.id.imageView1);
        imageView2 = view.findViewById(R.id.imageView2);
        imageView3 = view.findViewById(R.id.imageView3);
        simpleProgressBar = (ProgressBar) view.findViewById(R.id.simpleProgressBar);
        simpleProgressBar.setVisibility(View.INVISIBLE);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
                hasImage1 = true;
                img1JustNow = true;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
                hasImage2 = true;
                img1JustNow = false;
            }
        });

        button3.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (hasImage1 && hasImage2) {
                    if (encryptedImage1.getHeight() == encryptedImage2.getHeight() && encryptedImage1.getWidth() == encryptedImage2.getWidth()) {
                        Toast.makeText(getContext(), "Decryption In Progress", Toast.LENGTH_SHORT).show();
                        new TabFragment4.DecryptTask().execute(encryptedImage1, encryptedImage2);
                    }
                    else {
                        Toast.makeText(getContext(), "Cannot decrypt! Image have different sizes!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Snackbar.make(view, "Please Choose Image to Decrypt", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/DCIM/Decryption");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File file = new File(myDir, fname);
                Log.i(TAG, "" + file);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    decryptedImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    scanFile( myDir + "Image-" + n + ".jpg");
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file));

                    getActivity().sendBroadcast(intent);
                } catch(Exception e) {
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

    private void setProgressValue (final int progress) { simpleProgressBar.setProgress(progress);}
    private void scanFile(String path){
        MediaScannerConnection.scanFile(getContext(), new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.d("Tag", "Scan finished. You can view the image in the gallery now.");
            }
        });
    }

    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    //count ++;

                    Uri selectedImage = data.getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedImage);
                        if (img1JustNow){
                            imageView1.setImageURI(selectedImage);
                            encryptedImage1 = bitmap;
                        }
                        else {
                            imageView2.setImageURI(selectedImage);
                            encryptedImage2 = bitmap;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
            }

    }

    private class DecryptTask extends AsyncTask<Bitmap, Integer, Bitmap> {
        // Do the long-running work in here

        protected void onPreExecute(){
            simpleProgressBar.setVisibility(View.VISIBLE);
            setProgressValue(0);
            imageView3.setImageResource(R.drawable.ic_launcher_background);

//            if (backgroundImage.getHeight() != secretImage.getHeight() || backgroundImage.getWidth() != secretImage.getWidth()){
//                Toast.makeText(getContext(), "Encryption In Progress", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Toast.makeText(getContext(), "Cannot encrypt! Images have different sizes!", Toast.LENGTH_SHORT).show();
//            }

        }
        protected Bitmap doInBackground(Bitmap... imgs) {
            //encryptedImage = imageEncryption.Encrypt(backgroundImage, secretImage);

            decryptedImage = createBitmap(encryptedImage1.getWidth(), encryptedImage1.getHeight(), ARGB_8888);
            int p, a1, r1, g1, b1, a2, r2, g2, b2;

            for (int i = 0; i < encryptedImage1.getHeight(); i++)
            {
                for (int j = 0; j < encryptedImage1.getWidth(); j++)
                {
                    p = encryptedImage1.getPixel(j, i);
                    a1 = (p >> 24) & 0xf;
                    r1 = (p >> 16) & 0xf;
                    g1 = (p >> 8) & 0xf;
                    b1 = p & 0xf;
                    p = encryptedImage2.getPixel(j, i);
                    a2 = ((p >> 24) & 0xf)+ (a1 * 16);
                    r2 = ((p >> 16) & 0xf) + (r1 * 16);
                    g2 = ((p >> 8) & 0xf) + (g1 * 16);
                    b2 = (p & 0xf) + (b1 * 16);
                    decryptedImage.setPixel(j, i, (a2 << 24) + (r2 << 16) + (g2 << 8) + b2);
                }
                if (i % 100 == 0) {
                    publishProgress((int)((i*100.0 / encryptedImage1.getHeight())));
                }
            }

            return decryptedImage;
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
            setProgressValue(progress[0]);
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Bitmap result) {
            Toast.makeText(getContext(), "Decryption complete", Toast.LENGTH_SHORT).show();
            //showNotification("Processing Complete");
            simpleProgressBar.setVisibility(View.INVISIBLE);
            imageView3.setImageBitmap(result);
        }
    }
}
