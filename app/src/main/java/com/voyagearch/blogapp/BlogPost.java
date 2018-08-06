package com.voyagearch.blogapp;


import java.util.Date;

public class BlogPost extends BlogPostId {

    private String imageUrl,userId,desc;
    private Date timestamp;


    public BlogPost() {}

    public BlogPost(String imageUrl, String userId, String desc,Date timestamp) {
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
