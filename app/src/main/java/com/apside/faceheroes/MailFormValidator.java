package com.apside.faceheroes;


import android.view.View;
import android.widget.TextView;

public class MailFormValidator extends TextValidator implements View.OnFocusChangeListener {

    public MailFormValidator(TextView textView) {
        super(textView);
    }

    @Override
    public boolean validate(TextView textView) {
//        if (StringUtils.isBlank(textView.getText().toString())) {
            textView.setError(textView.getHint() + " is required!");
//            return false;
//        }else{
//            textView.setError(null);
            return true;
  //      }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            validate(textView);
        }
    }
}
