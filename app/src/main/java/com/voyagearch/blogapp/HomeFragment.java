package com.voyagearch.blogapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogRecyclerView;
    private BlogRecyclerViewHolder blogRecyclerViewHolder;

    private List<BlogPost> myBlogList;

    private FirebaseFirestore firebaseFirestore;

    //define the DataSnapshot to retrieve the last post so that we can retrieve the another three post from the firebase to implement the pagination in out blogpost
    private DocumentSnapshot lastVisible;

    //to check whether any new posted or not
    //app wouldn't crash anymore while running app
    //because when another user posted the new post ,recyclerview set the new post at the end of the of the last post and then if will rewind the all the post
    //this is more important part in Pagination
    private Boolean setNewPostAtFirstPosition=true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);

        //if you are getting
        //java.lang.NullPointerException: Attempt to invoke virtual method
        // 'void android.support.v7.widget.RecyclerView.setLayoutManager(android.support.v7.widget.RecyclerView$LayoutManager)'
        // on a null object reference
        //to avoid this exception we need to use the view to get the id of the recyclerview ratherthan any other context
        blogRecyclerView=view.findViewById(R.id.home_recyclerId);

        myBlogList=new ArrayList<>();

        blogRecyclerViewHolder=new BlogRecyclerViewHolder(myBlogList,view.getContext());

        firebaseFirestore=FirebaseFirestore.getInstance();

        blogRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        blogRecyclerView.setAdapter(blogRecyclerViewHolder);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

            //check if RecylerView is reached at bottom or not
            //if reached bottom then we need to fireup the next 3 Posts
            blogRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    //checking the reachedLast value
                    Boolean reachedLast=!blogRecyclerView.canScrollVertically(1);

                    //if recyclerView reached last then we need to load the next posts
                    if(reachedLast){
                        String desc=lastVisible.getString("desc");
                        Toast.makeText(getActivity().getApplicationContext(), "RechedLast:"+desc, Toast.LENGTH_SHORT).show();

                        //to load the another post in the recyclerView
                        loadMorePost();

                    }
                }
            });
            //for Pagination we need to use the firebase firestore query to arrange the post by using the date
            //to load the 3 post at a time we need to invoke the limit method
            Query firstQuery=firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            //we want real time data from the database so we need to invoke the addSnapshotListener
            //getActivity  is used to prevent for crashing
            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                    //if new blog posted then lastVisible don't change
                    if(setNewPostAtFirstPosition)
                    {
                        //to get the last post to implement the pagination
                        lastVisible=documentSnapshots.getDocuments().get(documentSnapshots.size() -1);
                    }

                    for(DocumentChange  doc: documentSnapshots.getDocumentChanges()){

                        if(doc.getType()==DocumentChange.Type.ADDED){

                            //to  set the likebtn we need the blogPostId therefore we need to pass the id into the RecyclerView Adapter
                            //withId is the method of BlogPostId class that will help to send the id
                            //for getting the blogPostId
                            String blogPostId=doc.getDocument().getId();
                            BlogPost blogPost=doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            //to set the New Post at the Fist Position so that Pagination wil work in a right way
                            if(setNewPostAtFirstPosition){

                                myBlogList.add(blogPost);

                            }else{

                                //so if new blog posted via another device then if will set the new Post at the First Position
                                myBlogList.add(0,blogPost);
                            }

                            blogRecyclerViewHolder.notifyDataSetChanged();

                        }
                    }

                    //Important part for the Pagination
                    setNewPostAtFirstPosition=false;
                }
            });
        }

        return view;

    }

    public void loadMorePost(){

        Query nextQuery=firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)       //to start the next post down after last post
                .limit(3);

        //we want real time data from the database so we need to invoke the addSnapshotListener
        nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                //check condition so that when there is no post ,it shouldn't be execute the query for loading the post
                //if will help to don't let our app crash
                if(!documentSnapshots.isEmpty()){

                    //we need to set the value of the last visible so that we can fireup rest of the posts into the recyclerView
                    lastVisible =documentSnapshots.getDocuments().get(documentSnapshots.size() -1);

                    for(DocumentChange  doc: documentSnapshots.getDocumentChanges()){

                        if(doc.getType()==DocumentChange.Type.ADDED){

                            //to  set the likebtn we need the blogPostId therefore we need to pass the id into the RecyclerView Adapter
                            //withId is the method of BlogPostId class that will help to send the id
                            String blogPostId=doc.getDocument().getId();
                            BlogPost blogPost=doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            myBlogList.add(blogPost);

                            blogRecyclerViewHolder.notifyDataSetChanged();

                        }
                    }
                }

            }
        });

    }

}
