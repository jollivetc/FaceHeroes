package com.apside.faceheroes;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MailActivity extends AppCompatActivity {

    private EditText mLastNameField;
    private EditText mFirstNameField;
    private EditText mMailField;


    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        mLastNameField = (EditText) findViewById(R.id.lastNameField);
        mFirstNameField = (EditText) findViewById(R.id.firstNameField);
        mMailField = (EditText) findViewById(R.id.mailField);

        Button sendButton = (Button) findViewById(R.id.sendMailButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = mFirstNameField.getText().toString();
                String lastName = mLastNameField.getText().toString();
                String mail = mMailField.getText().toString();
                Log.i("FaceHeroes", "Prepare mail to " + mail);
            }
        });
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
}
