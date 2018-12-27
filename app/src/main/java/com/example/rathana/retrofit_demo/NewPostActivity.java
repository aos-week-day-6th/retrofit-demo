package com.example.rathana.retrofit_demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.rathana.retrofit_demo.model.form.Article;

public class NewPostActivity extends AppCompatActivity {

    EditText title,desc,authorId,catId;
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        title=findViewById(R.id.title);
        desc=findViewById(R.id.desc);
        authorId=findViewById(R.id.authorId);
        catId=findViewById(R.id.catId);

    }

    public void onPost(View view) {
        Article article=new Article();
        article.setTitle(title.getText().toString());
        article.setDescription(desc.getText().toString());
        article.setCategoryId(Integer.parseInt(catId.getText().toString()));
        article.setAuthor(Integer.parseInt(authorId.getText().toString()));

        Intent intent=new Intent();
        Bundle bundle=new Bundle();
        bundle.putParcelable("data",article);
        setResult(RESULT_OK,intent);
        finish();
    }
}
