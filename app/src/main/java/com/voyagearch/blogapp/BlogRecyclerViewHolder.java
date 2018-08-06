package com.voyagearch.blogapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerViewHolder  extends RecyclerView.Adapter<BlogRecyclerViewHolder.MyHolder>{

    List<BlogPost> myBlogPostList;
    Context context;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public BlogRecyclerViewHolder(List<BlogPost> myBlogPostList, Context context) {
        this.myBlogPostList = myBlogPostList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        //to to remove the noticable delay while recycling the posts
        holder.setIsRecyclable(false);
        //getting the blogPostId
        final String blogPostId=myBlogPostList.get(position).BlogPostId;

        final String currentUserId=firebaseAuth.getCurrentUser().getUid();

        holder.desc.setText(myBlogPostList.get(position).getDesc());

        String image_url=myBlogPostList.get(position).getImageUrl();
        holder.onSetBlogImage(image_url);

        //to set the date and time into the post we can see the time and date of the post
        long miliseconds=myBlogPostList.get(position).getTimestamp().getTime();
        Date date = new Date(miliseconds);
        SimpleDateFormat dateformat=new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String dateString=dateformat.format(date).toString();
        holder.date.setText(dateString);

        String userId=myBlogPostList.get(position).getUserId();

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String username=task.getResult().getString("username");
                    String imageUrl=task.getResult().getString("profile");

                    //to set the image and the username
                    holder.onProfileInfo(username,imageUrl);

                }else{
                       //error handling
                }
            }
        });

        //to count the likes of the post
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    //to count the likes
                    int count=documentSnapshots.size();
                    holder.updateLikesCount(count);

                }else{
                    holder.updateLikesCount(0);
                }
            }
        });
        //setting the image if liked or not
        //and we want the likes in real time
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.likesBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_red_24dp));
                }else{
                    holder.likesBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_gray_24dp));
                }
            }
        });
        //setting the like button
        holder.likesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            //store the timestamp to know when the likes button clicked
                            Map<String,Object> likesMap=new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).set(likesMap);

                        }else{
                            //if likes timestamp already exits in firebase firestore then we need to delete so that we can
                            //implement the dislike thing
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).delete();
                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return myBlogPostList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        TextView desc;
        TextView date;
        ImageView myImageView;

        TextView myUsername;
        CircleImageView profileImg;

        ImageView likesBtn;
        TextView  likesCount;
        public MyHolder(View itemView) {
            super(itemView);

            desc=itemView.findViewById(R.id.blog_descriptionId);
            myImageView=itemView.findViewById(R.id.blog_post_imgId);
            date=itemView.findViewById(R.id.blog_data_of_postId);

            myUsername=itemView.findViewById(R.id.blog_usernameId);
            profileImg=itemView.findViewById(R.id.blog_profile_imgId);

            likesBtn=itemView.findViewById(R.id.blog_likesBtnId);
            likesCount=itemView.findViewById(R.id.blog_likesId);

        }

        public void onSetBlogImage(String image_url){

            RequestOptions placeholderForPostImage=new RequestOptions();
            placeholderForPostImage.placeholder(R.mipmap.blog_image_thumb);

            Glide.with(context).applyDefaultRequestOptions(placeholderForPostImage).load(image_url).into(myImageView);

        }

        //to set the username and the profile image into the post
        public void onProfileInfo(String username,String imageUrl){

            myUsername.setText(username);

            RequestOptions profileImagePlaceHodler=new RequestOptions();
            profileImagePlaceHodler.placeholder(R.mipmap.profile_tuhumb);

            Glide.with(context).applyDefaultRequestOptions(profileImagePlaceHodler).load(imageUrl).into(profileImg);

        }
       public void updateLikesCount(int count){

            likesCount.setText(count+"Likes");
       }
    }
}
