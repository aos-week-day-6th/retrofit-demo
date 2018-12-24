package com.example.rathana.retrofit_demo.model.form;

import com.google.gson.annotations.SerializedName;

public abstract class Article {

    @SerializedName("IMAGE")
    public String image;
    @SerializedName("STATUS")
    public String status;
    @SerializedName("CATEGORY_ID")
    public int categoryId;
    @SerializedName("AUTHOR")
    public int author;
    @SerializedName("DESCRIPTION")
    public String description;
    @SerializedName("TITLE")
    public String title;


    @Override
    public String toString() {
        return "Article{" +
                "image='" + image + '\'' +
                ", status='" + status + '\'' +
                ", categoryId=" + categoryId +
                ", author=" + author +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
