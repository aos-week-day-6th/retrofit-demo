package com.example.rathana.retrofit_demo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.rathana.retrofit_demo.data.ServiceGenerator;
import com.example.rathana.retrofit_demo.data.service.ArticleService;
import com.example.rathana.retrofit_demo.model.form.Article;
import com.example.rathana.retrofit_demo.model.response.ImageResponse;
import com.example.rathana.retrofit_demo.util.RuntimePermissionHelper;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    EditText title,desc,authorId,catId;
    ProgressBar progressBar;
    ImageView img,btnPickImage;
    String articleThumb;
    ArticleService service;
    Article article;
    int posUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        title=findViewById(R.id.title);
        desc=findViewById(R.id.desc);
        authorId=findViewById(R.id.authorId);
        catId=findViewById(R.id.catId);

        img=findViewById(R.id.image);

        btnPickImage=findViewById(R.id.btnPickImage);
        progressBar=findViewById(R.id.progressBar);

        //create service object
        service=ServiceGenerator.createServices(ArticleService.class);

        //check runtime permission
        RuntimePermissionHelper.checkReadExternalStorage(this);

        //get ams from intent
        if(getIntent()!=null){
            article=getIntent().getParcelableExtra("data");
            title.setText(article.getTitle());
            catId.setText(article.getCategoryId()+"");
            authorId.setText(article.getAuthor()+"");
            posUpdate=getIntent().getIntExtra("pos",0);
            if(article.getImage()!=null)
                Picasso.get().load(article.getImage()).into(img);

        }

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

    }

    public static  final  int PICK_IMAGE_CODE=100;
    private void pickImage(){
        Intent intent=new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_IMAGE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_CODE && resultCode==RESULT_OK){
            //get image uri
            Uri uri=data.getData();
            Log.e(TAG, "onActivityResult: pick image");
            try{

                String[] columnInfo ={MediaStore.Images.Media.DATA};
                Cursor cursor=getContentResolver().query(
                        uri,columnInfo,
                        null,
                        null,
                        null
                );

                cursor.moveToFirst();
                int columnIndex=cursor.getColumnIndex(columnInfo[0]);
                String filePath= cursor.getString(columnIndex);
                Log.e(TAG, "onActivityResult: "+filePath );
                //covert image path to bitmap
                Bitmap bitmap=BitmapFactory.decodeFile(filePath);
                img.setImageBitmap(bitmap);

                //upload image to server
                // TODO: 1/2/19
                uploadImage(filePath);

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        
    }

    public void onSaveChange(View view) {
        Article article=new Article();
        article.setTitle(title.getText().toString());
        article.setDescription(desc.getText().toString());
        article.setCategoryId(Integer.parseInt(catId.getText().toString()));
        article.setAuthor(Integer.parseInt(authorId.getText().toString()));
        article.setId(this.article.getId());
        article.setImage(articleThumb==null ? this.article.getImage() : articleThumb);
        Log.e("edit",article.toString());
        Intent intent=new Intent();
        Bundle bundle=new Bundle();
        bundle.putParcelable("data",article);
        intent.putExtras(bundle);
        intent.putExtra("pos",posUpdate);
        setResult(RESULT_OK,intent);
        finish();
    }

    private  void uploadImage(String fileName){
        progressBar.setVisibility(View.VISIBLE);
        service.uploadImage(createMultipartBody(fileName))
                .enqueue(new Callback<ImageResponse>() {
                    @Override
                    public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                        ImageResponse image= response.body();
                        articleThumb=image.getImage();
                        Log.e(TAG, "uploadImage: "+ response.toString());
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<ImageResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: "+t.toString() );
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    //covert file to multipart.part
    private MultipartBody.Part createMultipartBody(String fileName){
        Uri uri=Uri.parse(fileName);
        File file = new File(uri.getPath());
        RequestBody body=RequestBody.create(MediaType.parse("multipart/form-data"),file);

        return  MultipartBody.Part.createFormData(
          "FILE",file.getName(),body
        );
    }
}
