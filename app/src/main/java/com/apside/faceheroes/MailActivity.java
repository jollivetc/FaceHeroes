package com.apside.faceheroes;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MailActivity extends AppCompatActivity {

    private EditText mLastNameField;
    private EditText mFirstNameField;
    private EditText mMailField;
    private ImageView mImageView;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        mImageView = (ImageView) findViewById(R.id.imageView);

        final File f = findLastFile("/storage/emulated/0/Pictures");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        mImageView.setImageBitmap(bitmap);

        mLastNameField = (EditText) findViewById(R.id.lastNameField);
        mFirstNameField = (EditText) findViewById(R.id.firstNameField);
        mMailField = (EditText) findViewById(R.id.mailField);

        addValidators();

        Button sendButton = (Button) findViewById(R.id.sendMailButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FaceHeroes", "you will send");
               /* if(validate()) {
                    String firstName = mFirstNameField.getText().toString();
                    String lastName = mLastNameField.getText().toString();
                    String mail = mMailField.getText().toString();
                    Log.i("FaceHeroes", "Prepare mail to " + mail);
                    //TODO upload and send mail
                }*/
                try {
                    upload(f.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private File findLastFile(String directory){

        File fl = new File(directory);
        File[] files = fl.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;

    }

    private boolean validate(){
        return StringUtils.isBlank(mLastNameField.getError().toString()) &&
                StringUtils.isBlank(mFirstNameField.getError().toString()) &&
                StringUtils.isBlank(mMailField.getError().toString());

    }

    private void addValidators() {
        MailFormValidator lastNameValidator = new MailFormValidator(mLastNameField);
        mLastNameField.addTextChangedListener(lastNameValidator);
        mLastNameField.setOnFocusChangeListener(lastNameValidator);

        MailFormValidator firstNameValidator = new MailFormValidator(mFirstNameField);
        mFirstNameField.addTextChangedListener(firstNameValidator);
        mFirstNameField.setOnFocusChangeListener(firstNameValidator);

        MailFormValidator mailValidator = new MailFormValidator(mMailField);
        mMailField.addTextChangedListener(mailValidator);
        mMailField.setOnFocusChangeListener(mailValidator);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public String upload(String filename) throws IOException {
        PictureUploader pictureUploader = new PictureUploader();
        pictureUploader.execute(filename);
        return "";
    }

    private class PictureUploader extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... fileName) {
            Log.i("FaceHeroes", "prepare mail");
            String url = "https://apside-devfest.cappuccinoo.fr/api/pics/save-picture";
            final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/*");

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("picture", fileName[0], RequestBody.create(MEDIA_TYPE_JPG, fileName[0]))
                    .addFormDataPart("firstName", "Bob")
                    .addFormDataPart("lastName", "Dupont")
                    .addFormDataPart("email", "bob@apside.fr")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            String res = null;
            try {
                okhttp3.Response response = client.newCall(request).execute();
                res = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("FaceHeroes", res);
            Log.e("FaceHeroes", "Response : " + res);
            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {

        }
    }

}
