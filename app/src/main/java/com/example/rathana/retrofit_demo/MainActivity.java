package com.example.rathana.retrofit_demo;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rathana.retrofit_demo.adapter.AmsAdapter;
import com.example.rathana.retrofit_demo.data.ServiceGenerator;
import com.example.rathana.retrofit_demo.data.service.ArticleService;
import com.example.rathana.retrofit_demo.model.form.Article;
import com.example.rathana.retrofit_demo.model.response.ArticleResponse;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemSpanLookup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
implements AmsAdapter.OnItemClickedCallback {
    ProgressBar progressBar;
    ArticleService service;
    RecyclerView rv;
    AmsAdapter adapter;
    static final int edit_request_code=50;

    int totalPage=10;
    boolean isLoading=true;
    int currentPage=1;

    List<ArticleResponse.DataEntity> articles=new ArrayList<>();
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar=findViewById(R.id.progressBar);
        setupRecyclerView();
        //getArticles();

    }

    private void setupRecyclerView() {
        rv=findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter=new AmsAdapter(articles,this);
        rv.setAdapter(adapter);

        isLoading=true;
        currentPage=1;
        Paginate.with(rv,callback)
                .setLoadingTriggerThreshold(2)
                .addLoadingListItem(true)
                .setLoadingListItemCreator(null)
                .setLoadingListItemSpanSizeLookup(new LoadingListItemSpanLookup() {
                    @Override
                    public int getSpanSize() {
                        return 1;
                    }
                }).build();

        callback.onLoadMore();
    }

    Paginate.Callbacks callback=new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
            if(isLoading){
                getArticles(currentPage,10);
                currentPage++;
                isLoading=false;
            }
        }

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public boolean hasLoadedAllItems() {
            return currentPage==10;
        }
    };


    private void getArticles(int page,int limit){
        progressBar.setVisibility(View.VISIBLE);
        service = ServiceGenerator.createServices(ArticleService.class);

        Call<ArticleResponse> call = service.getArticles(page,limit);
        /*Response<ArticleResponse> responseResponse= call.execute();*/
        call.enqueue(new Callback<ArticleResponse>() {
            @Override
            public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                List<ArticleResponse.DataEntity> dataEntities=
                        response.body().getData();
                //get total page
                totalPage=response.body().getPagination().getTotalPages();
                isLoading=true;
                adapter.setArticles(dataEntities);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onResponse: "+dataEntities.toString());
            }

            @Override
            public void onFailure(Call<ArticleResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: "+t.toString() );
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //callback from adapter

    @Override
    public void onItemSend(ArticleResponse.DataEntity article, int position) {
        Log.e("item",article.toString());
        Intent intent=new Intent(this,EditPostActivity.class);
        Article ams=new Article();
        ams.setId(article.getId());
        ams.setTitle(article.getTitle());
        ams.setImage(article.getImage());
        ams.setAuthor(article.getAuthor().getId());
        ams.setCategoryId(article.getCategory().getId());
        ams.setDescription(article.getDescription());

        Bundle b=new Bundle();
        b.putParcelable("data",ams);
        intent.putExtras(b);
        intent.putExtra("pos",position);
        startActivityForResult(intent,edit_request_code);
    }

    @Override
    public void onRemoveArticle(ArticleResponse.DataEntity article, int position) {
        adapter.removeArticle(article,position);
        removeArticleService(article.getId());
    }

    private void removeArticleService(int id){
        service.removeArticle(id).
                enqueue(new Callback<com.example.rathana.retrofit_demo.model.response.Article>() {
                    @Override
                    public void onResponse(Call<com.example.rathana.retrofit_demo.model.response.Article> call, Response<com.example.rathana.retrofit_demo.model.response.Article> response) {
                        Toast.makeText(MainActivity.this, "remove success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<com.example.rathana.retrofit_demo.model.response.Article> call, Throwable t) {

                    }
                });
    }

    @Override
    public void OnItemClicked(ArticleResponse.DataEntity article) {
        Intent intent=new Intent(this,ReadArticleActivity.class);
        Bundle bundle=new Bundle();
        bundle.putParcelable("data",article);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    static  final  int NEW_POST_CODE=100;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.newPost:
                    startActivityForResult(
                            new Intent(this,NewPostActivity.class),NEW_POST_CODE);
                return true;

            default: return  false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==NEW_POST_CODE && resultCode==RESULT_OK){
            //add data to adapter
            Article article= data.getParcelableExtra("data");
            ArticleResponse.DataEntity amsArticle=new ArticleResponse.DataEntity();
            ArticleResponse.AuthorEntity author=new ArticleResponse().new AuthorEntity();
            author.setId(article.getAuthor());
            amsArticle.setAuthor(author);
            amsArticle.setDescription(article.getDescription());
            amsArticle.setTitle(article.getTitle());
            amsArticle.setImage(article.getImage());
            adapter.setArticle(amsArticle);
            //upload post to server

            uploadNewPost(article);

        }else if(requestCode==edit_request_code && resultCode==RESULT_OK){
            
            Article article= data.getParcelableExtra("data");
            ArticleResponse.DataEntity amsArticle=new ArticleResponse.DataEntity();
            ArticleResponse.AuthorEntity author=new ArticleResponse().new AuthorEntity();
            author.setId(article.getAuthor());
            amsArticle.setAuthor(author);
            amsArticle.setDescription(article.getDescription());
            amsArticle.setTitle(article.getTitle());
            amsArticle.setImage(article.getImage());
            int pos=data.getIntExtra("pos",0);
            adapter.updateArticle(amsArticle,pos);
            
            //update data on server
            // TODO: 1/3/19
            editArticle(article.getId(),article);

        }
    }

    private void editArticle(int id, Article article) {
        service.editArticle(id,article)
        .enqueue(new Callback<com.example.rathana.retrofit_demo.model.response.Article>() {
            @Override
            public void onResponse(Call<com.example.rathana.retrofit_demo.model.response.Article> call, Response<com.example.rathana.retrofit_demo.model.response.Article> response) {
                Log.e("edit",response.body().getMessage());
            }

            @Override
            public void onFailure(Call<com.example.rathana.retrofit_demo.model.response.Article> call, Throwable t) {
                Log.e("onFailure",t.toString());
            }
        });
    }

    private void uploadNewPost(Article article){
        service.addArticle(article).
                enqueue(new Callback<com.example.rathana.retrofit_demo.model.response.Article>() {
                    @Override
                    public void onResponse(Call<com.example.rathana.retrofit_demo.model.response.Article> call, Response<com.example.rathana.retrofit_demo.model.response.Article> response) {
                        Log.e(TAG, "onResponse: create new article success" );
                    }

                    @Override
                    public void onFailure(Call<com.example.rathana.retrofit_demo.model.response.Article> call, Throwable t) {

                    }
                });
    }
}











