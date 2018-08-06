package com.voyagearch.blogapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH =50 ;

    private ImageView newPostImage;
    private EditText  newPostDesc;
    private Button   newPostBtn;
    private Toolbar newPostToolbar;
    private ProgressBar newPostProgressbar;

    private final static int NEWPOST_REQUEST_CODE=100;

    private Uri newPostImageUri=null;

    private StorageReference storageReference;
    private FirebaseFirestore newPostFireStore;
    private FirebaseAuth mAuth;

    private String current_user_postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference=FirebaseStorage.getInstance().getReference();
        newPostFireStore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        current_user_postId=mAuth.getCurrentUser().getUid();

        newPostImage=findViewById(R.id.new_postImageId);
        newPostDesc=findViewById(R.id.newPost_textId);
        newPostBtn=findViewById(R.id.newPost_btnId);
        newPostProgressbar=findViewById(R.id.newPost_progressbarId);

        //set the toobar
        newPostToolbar=findViewById(R.id.new_toolbarId);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("New Post");

        //to add the back button we need to add
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //we don't need to check the permission for the ReadExternal Storage
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent postImage=new Intent(Intent.ACTION_PICK);
                postImage.setType("image/*");
                startActivityForResult(postImage,NEWPOST_REQUEST_CODE);

            }
        });

        //to store the image into the firebase Storage  and then store the new post into the Firebase FireStore
        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 final String postDesc=newPostDesc.getText().toString();

                if( !TextUtils.isEmpty(postDesc) && newPostImageUri!=null){

                    newPostProgressbar.setVisibility(View.VISIBLE);

                    //it will generate the random string for id show that it will random string
                    final String randomImagePostName= UUID.randomUUID().toString();

                    StorageReference postImageFilePath=storageReference.child("post_images").child(randomImagePostName+".jpg");

                    postImageFilePath.putFile(newPostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(NewPostActivity.this,"images stored ",Toast.LENGTH_SHORT).show();

                                //to getting the image from the firebase
                                String donwloadPostImageUrl=task.getResult().getDownloadUrl().toString();

                                HashMap<String,Object> postMap=new HashMap<>();
                                postMap.put("imageUrl",donwloadPostImageUrl);
                                postMap.put("desc",postDesc);
                                postMap.put("userId",current_user_postId);
                                postMap.put("timestamp",FieldValue.serverTimestamp());

                                //here we used the add in place of .document().set() to set the random id for data
                               newPostFireStore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                   @Override
                                   public void onComplete(@NonNull Task<DocumentReference> task) {

                                       if(task.isSuccessful()){

                                           Toast.makeText(NewPostActivity.this,"firebase firestore data inserted",Toast.LENGTH_SHORT).show();
                                           Intent newPostIntent=new Intent(NewPostActivity.this,MainActivity.class);
                                           startActivity(newPostIntent);
                                           finish();
                                       }else{
                                           String error=task.getException().getMessage();
                                           Toast.makeText(NewPostActivity.this,"Error:"+error,Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });
                            }else{

                                newPostProgressbar.setVisibility(View.VISIBLE);
                                String error=task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this,"Error"+error,Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }else{
                    newPostProgressbar.setVisibility(View.INVISIBLE);
                    Toast.makeText(NewPostActivity.this, "Add Image or Description", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //to set the image to the
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==NEWPOST_REQUEST_CODE){

            newPostImageUri=data.getData();
            newPostImage.setImageURI(newPostImageUri);

            Toast.makeText(NewPostActivity.this,"Image Setted",Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(NewPostActivity.this,"Error",Toast.LENGTH_SHORT).show();
        }
    }

}
