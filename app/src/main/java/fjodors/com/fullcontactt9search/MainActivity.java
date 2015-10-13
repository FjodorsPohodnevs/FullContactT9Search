package fjodors.com.fullcontactt9search;


import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    protected static final String TAG = "MainActivity";

    @Bind(R.id.words)
    RecyclerView mRecyclerView;

    RecyclerAdapter mAdapter;

    List<String> words = new ArrayList<>();
    Trie trie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        asyncLoadData();

    }

    public void asyncLoadData() {
        Observable.create(__ -> {
            trie = new Trie();
            trie.loadDictionary(loadWordsToList());
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(__ -> {
                    setRecyclerView();
                    Toast.makeText(getApplicationContext(), "Data loaded", Toast.LENGTH_LONG).show();
                });
    }


    public List<String> loadWordsToList() {

        BufferedReader reader = null;
        try {
            try {
                InputStream in = getResources().openRawResource(R.raw.words);
                reader = new BufferedReader(new InputStreamReader(in));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    words.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (reader != null)
                    reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }


    public void setRecyclerView() {
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerAdapter(trie.getT9Map());
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        setupSearchView(menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void setupSearchView(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName name = getComponentName();
        SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

        searchView.setSearchableInfo(searchInfo);

        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_item_search) {
            onSearchRequested();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {

        List<String> filteredWordList = trie.lookup(query);
        mAdapter.animateTo(filteredWordList, query);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

}
