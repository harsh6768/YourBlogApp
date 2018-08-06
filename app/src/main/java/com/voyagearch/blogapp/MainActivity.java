package com.voyagearch.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private FloatingActionButton addPostBtn;

    private  FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    private BottomNavigationView mBottomNavigationView;
    private  HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("onCreate","MainActivity Create");
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();


        //if user not logged in then only we see the home page
        //so that after log out from the account when we will open the app again our app shouldn't be crash

        if(mAuth.getCurrentUser()!=null){

            Log.i("onCreate","Checking condition");

            myToolbar=findViewById(R.id.main_toolbarId);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setTitle("Photo Blog");

            mBottomNavigationView=findViewById(R.id.bottom_navId);
            frameLayout=findViewById(R.id.main_frameId);
            homeFragment=new HomeFragment();
            notificationFragment=new NotificationFragment();
            accountFragment=new AccountFragment();

            //to set the default fragment
            setFragment(homeFragment);

            mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.bottom_homeId:
                            setFragment(homeFragment);
                            return true;
                        case R.id.bottom_notifId:
                            setFragment(notificationFragment);
                            return true;
                        case R.id.bottom_accountId:
                            setFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            addPostBtn=findViewById(R.id.floating_postId);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent newPostIntent=new Intent(MainActivity.this,NewPostActivity.class);
                    startActivity(newPostIntent);

                }
            });

        }

    }

    //to set the fragment when we click the bottom navigation button
    private void setFragment(Fragment fragment)
    {
        //FragmentTransaction sync the frament with bottom navigation bar
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frameId,fragment);
        fragmentTransaction.commit();

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.i("onStart","MainActivity Starts");

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser==null)
        {
            Log.i("onStart  ","onStart Condition Checked");
            //goto the mainActivity
            onMain();
        }
        else{
            Log.i("onCreate","Else Statement");
           //check if user logged in but profile still haven't updated then so we need to send the user to setup activity

            firebaseFirestore.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        //if username and profile image haven't saved yet in firebase firestore then send user to setup activity
                        if(!task.getResult().exists()){
                            Intent setUpIntent=new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setUpIntent);
                            finish();
                        }
                    }else{
                        String error=task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error"+error,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //for menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_logoutId:
                 onLogout();
                return true;
            case R.id.menu_accountSettings:
                Intent intent=new Intent(MainActivity.this,SetupActivity.class);
                startActivity(intent);
                return true;
                default:
                    return false;

        }
    }

    private void onLogout() {

        mAuth.signOut();
        if(mAuth.getCurrentUser()==null){
            onMain();
        }

    }

    private void onMain() {

        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }

}
