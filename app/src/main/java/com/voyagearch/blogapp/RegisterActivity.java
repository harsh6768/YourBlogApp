package com.voyagearch.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_Email;
    private EditText reg_Pass;
    private EditText reg_ConfirmPass;


    private ProgressBar myProgressbar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        reg_Email=findViewById(R.id.reg_emailId);
        reg_Pass=findViewById(R.id.reg_passId);
        reg_ConfirmPass=findViewById(R.id.reg_confirm_passId);

        Button reg_RegisterBt = findViewById(R.id.reg_registerId);

        Button reg_LoginBt=findViewById(R.id.reg_loginId);

        myProgressbar=findViewById(R.id.reg_progressId);

        reg_RegisterBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String myEmail=reg_Email.getText().toString().trim();
                final String myPass=reg_Pass.getText().toString().trim();
                final String myConfirmPass=reg_ConfirmPass.getText().toString().trim();

                if(!TextUtils.isEmpty(myEmail) && !TextUtils.isEmpty(myPass) && !TextUtils.isEmpty(myConfirmPass))
                {
                    myProgressbar.setVisibility(View.VISIBLE);

                    if(myPass.equals(myConfirmPass))
                    {
                        mAuth.createUserWithEmailAndPassword(myEmail,myPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()) {
                                   //if user registered but username isn't setup yet therefore we need to goto the setup activity
                                    Intent setupIntent=new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                }
                                else {
                                    myProgressbar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(RegisterActivity.this,"Registration Failed!!!!",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        myProgressbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this,"Please Enter Correct Password!!!!",Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(RegisterActivity.this,"Please Fill The Information!!!!",Toast.LENGTH_LONG).show();
                }
            }
        });

        reg_LoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser!=null) {
            goToMain();
        }
    }

    private void goToMain() {
       Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
       startActivity(intent);
       finish();
    }

}
