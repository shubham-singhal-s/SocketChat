package com.bennyhawk.socketchat;


import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    private Dialog userNameRequestDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameRequestDialog= new Dialog(this,R.style.Theme_Dialog);
        userNameRequestDialog.setContentView(R.layout.dialog_request_username);
        userNameRequestDialog.setCancelable(false);
        userNameRequestDialog.setCanceledOnTouchOutside(false);
        userNameRequestDialog.show();
    }

    public void setUsername(View v){
        EditText textInputLayout = (EditText) userNameRequestDialog.findViewById(R.id.user_name_input);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.chat_container, ChatMainFragment.newInstance(textInputLayout.getText().toString()))
                .commit();
        userNameRequestDialog.dismiss();


    }
}
