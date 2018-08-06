package com.voyagearch.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private EditText mUserName;
    private Button saveSettingsBt;
    private ProgressBar setupProgressbar;
    private Toolbar myToolBar;

    private CircleImageView circleImageView;

    //for requestcode
    private final static int GALLERY=100;

    Uri imageUri=null;

    StorageReference myStorageReferece ;
    FirebaseFirestore myFireStore;
    FirebaseAuth mAuth;

    private String user_Id;

    // for getting the path of the image so that we can download the image
    Uri downloadPath=null;

    //this will used to check if profile image is changed or not
    private  boolean profileImageChanged=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //to get the userId

        //for the toolbar
        myToolBar=findViewById(R.id.account_settingsId);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setTitle("Account Settings");

        circleImageView=findViewById(R.id.setup_imgId);
        mUserName=findViewById(R.id.setup_userNameId);
        saveSettingsBt=findViewById(R.id.setup_settingsBtId);
        setupProgressbar=findViewById(R.id.setup_progressBarId);

        myStorageReferece=FirebaseStorage.getInstance().getReference();
        //we are going to save the data into the FirebaseFireStore
        myFireStore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        //to get the uersId of the current user sothat we can retrieve the data from the FirebaseFireStore
        user_Id=mAuth.getCurrentUser().getUid();


        //to Retrieve the username and the Image from the FirebaseFirestore and set to the profile so that when we go the account activity after storing the image and the username
        //we can see the username and the profile Image into the circleImageView
        setupProgressbar.setVisibility(View.VISIBLE);
        saveSettingsBt.setEnabled(false);

        myFireStore.collection("Users").document(user_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                   if(task.isSuccessful()){
                    //to check whether data is stored i mean username and the imageview of the that user
                    //if exist retrieve the username  and the profile image and set to the in account settings
                      if(task.getResult().exists()){

                           String name=task.getResult().getString("username");
                           String profile_image=task.getResult().getString("profile");

                        //so if user only wants to change the username then it app won't crash
                          imageUri=Uri.parse(profile_image);
                        //username is set
                        mUserName.setText(name);
                        //now we need to set the image for this we use the glide library

                        //RequestOptions used to fill the Gap between the time taken to set the image
                        //it will set the default image so that it shows the default image until profile images set
                        RequestOptions requestPlaceHolder=new RequestOptions();
                        requestPlaceHolder.placeholder(R.drawable.circle_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(requestPlaceHolder).load(profile_image).into(circleImageView);

                    }else {
                        Toast.makeText(SetupActivity.this,"Data doesn't exist!!!",Toast.LENGTH_LONG).show();
                    }

                    setupProgressbar.setVisibility(View.INVISIBLE);
                    saveSettingsBt.setEnabled(true);

                }else{
                    setupProgressbar.setVisibility(View.INVISIBLE);
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FireStore Retrieve  error"+error,Toast.LENGTH_LONG).show();
                }
            }
        });

        //this will upload the image into the FirebaseStorage when we click into the circleImageView
        saveSettingsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if profile image changed then store the image to the FirebaseStorage otherwise just store the username and the profile image into the Firebase FireStore
                final String username = mUserName.getText().toString().trim();
                if (!TextUtils.isEmpty(username) && imageUri != null) {

                    if (profileImageChanged) {

                        setupProgressbar.setVisibility(View.VISIBLE);

                        //set the path of the image
                        StorageReference imagePath = myStorageReferece.child("profile_images").child(user_Id + ".jpg");

                        //to store the  image into the Firebase Storage
                        imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    //method is called to remove the confliction of storing the image into the FirebaseStorage
                                    //because when we change the username and want to store the image and data into the FirebaseFireStore
                                    saveDataOnFirebaseStorage(task, username);

                                } else {
                                    setupProgressbar.setVisibility(View.INVISIBLE);
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image error" + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        //if profile Image haven't changed but username is changed then store the username and update the data
                        saveDataOnFirebaseStorage(null, username);
                    }
               }else {
                    setupProgressbar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetupActivity.this, "Please Fill the Information!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        //when into the circleImage
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //for checking the dangerous permission from the user to use the external storage
                //it will only work from Mars mallow

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    //check if permission granted or not
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this,"Permission Denied!!!",Toast.LENGTH_LONG).show();
                        //request from the use to give the permission so that user can use the extenal storage to set the profile image
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                    }
                    else{
                        //if permission granted then we need to set the image
                        setProfileImage();
                    }
                }else{
                    //android version is less than 6.0
                     setProfileImage();
                }
            }
        });

    }

    //to save the image to FirebaseStorage and then store into the FirebaseFireStore
    private void saveDataOnFirebaseStorage(Task<UploadTask.TaskSnapshot> task, String username) {

        //check profile Image changed or not
        if(profileImageChanged) {
            //to get the path of the images so that we can download that images
            downloadPath=task.getResult().getDownloadUrl();
        }else{

            //if profile didn't changed then do get the downloadPath of the image and set to the uri
            downloadPath=imageUri;
        }

        user_Id=mAuth.getCurrentUser().getUid();

        //we are going to store the data into the FirebaseFireStore
        HashMap<String,String> users=new HashMap<String,String>();
        users.put("username",username);
        users.put("profile",downloadPath.toString());

        //if image stored in the FirebaseStorage now we will store the username and the profile in the FirebaseFireStore
        myFireStore.collection("Users").document(user_Id).set(users).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(SetupActivity.this,"Settings Updated!!!",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SetupActivity.this,MainActivity.class));
                    finish();

                }else{
                    setupProgressbar.setVisibility(View.INVISIBLE);
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FireStore error"+error,Toast.LENGTH_LONG).show();
                }
                setupProgressbar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setProfileImage() {
        Intent setImage=new Intent(Intent.ACTION_PICK);
        setImage.setType("image/*");
        startActivityForResult(setImage,GALLERY);

    }

    //set the image to the circleImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY){

            imageUri=data.getData();
            circleImageView.setImageURI(imageUri);
            //image is changed so weed to changed the boolean value
            profileImageChanged=true;
        }
    }
}
