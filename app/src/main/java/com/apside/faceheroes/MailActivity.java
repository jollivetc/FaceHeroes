package com.apside.faceheroes;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
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

        File f = findLastFile("/storage/emulated/0/Pictures");
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
                if(validate()) {
                    String firstName = mFirstNameField.getText().toString();
                    String lastName = mLastNameField.getText().toString();
                    String mail = mMailField.getText().toString();
                    Log.i("FaceHeroes", "Prepare mail to " + mail);
                    //TODO upload and send mail
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

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void upload(String filepath) throws IOException
    {
 /*       HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost("url");
        File file = new File(filepath);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file, "image/jpeg");
        mpEntity.addPart("userfile", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
  */      // check the response and do what is required
    }

}
