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
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText login_EmailEditText;
    private EditText login_PasswordEdittext;
    private Button loginButton;
    private Button login_regButton;
    private ProgressBar  login_Progressbar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_EmailEditText=(EditText)findViewById(R.id.log_emailId);
        login_PasswordEdittext=(EditText)findViewById(R.id.log_passId);
        loginButton=(Button)findViewById(R.id.log_loginButtonId);
        login_regButton=(Button)findViewById(R.id.log_reg_buttonId);
        login_Progressbar=(ProgressBar)findViewById(R.id.log_progressId);

        mAuth=FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String myLoginEmail=login_EmailEditText.getText().toString().trim();
                final String myLoginPass=login_PasswordEdittext.getText().toString().trim();


                if(!TextUtils.isEmpty(myLoginEmail) && !TextUtils.isEmpty(myLoginPass))
                {
                    login_Progressbar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(myLoginEmail,myLoginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                goToMain();
                            }
                            else
                            {
                                login_Progressbar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this,"Login Failed!!!",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Kindly Fill the Details!!!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            goToMain();
        }
    }

    void goToMain()
    {
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }

    public void onLogRegister(View view)
    {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }
}
