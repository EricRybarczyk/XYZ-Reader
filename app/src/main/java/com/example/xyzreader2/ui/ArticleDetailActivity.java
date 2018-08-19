package com.example.xyzreader2.ui;

import android.content.Loader;
import android.app.LoaderManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.xyzreader2.R;
import com.example.xyzreader2.data.ArticleLoader;
import com.example.xyzreader2.data.ItemsContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleDetailActivity extends AppCompatActivity
                        implements LoaderManager.LoaderCallbacks<Cursor>,
                                    ArticleDetailFragment.OnFragmentInteractionListener {

    // android.support.v4.app.FragmentStatePagerAdapter
    //    https://developer.android.com/reference/android/support/v4/app/FragmentStatePagerAdapter

    @BindView(R.id.pager) protected ViewPager viewPager;
    private ArticlePagerAdapter pagerAdapter;
    private Cursor articleCursor;
    private long startId;
    private static final String TAG = ArticleDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        getLoaderManager().initLoader(0, null, this);

        pagerAdapter = new ArticlePagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //super.onPageSelected(position);
                if (articleCursor != null) {
                    articleCursor.moveToPosition(position);
                }
                //selectedItemId = articleCursor.getLong(ArticleLoader.Query._ID);
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
                //selectedItemId = startId;
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        articleCursor = data;
        pagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (startId > 0) {
            articleCursor.moveToFirst();
            // TODO: optimize
            while (!articleCursor.isAfterLast()) {
                if (articleCursor.getLong(ArticleLoader.Query._ID) == startId) {
                    final int position = articleCursor.getPosition();
                    viewPager.setCurrentItem(position, false);
                    break;
                }
                articleCursor.moveToNext();
            }
            startId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        articleCursor = null;
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO - something to do here?
        Log.i(TAG, "onFragmentInteraction: " + uri.toString());
    }


    private class ArticlePagerAdapter extends FragmentStatePagerAdapter {

        public ArticlePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            articleCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(articleCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (articleCursor != null) ? articleCursor.getCount() : 0;
        }
    }

}
