package com.example.xyzreader2.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader2.R;
import com.example.xyzreader2.data.ArticleLoader;
import com.example.xyzreader2.data.ItemsContract;
import com.example.xyzreader2.data.UpdaterService;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ArticleListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar) protected CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view) protected RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) protected SwipeRefreshLayout swipeRefreshLayout;

    private static final int LOADER_ID = 5150;
    private static final String TAG = ArticleListActivity.class.getSimpleName();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.app_name));

        swipeRefreshLayout.setOnRefreshListener(this);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (savedInstanceState == null) {
            refresh();
        }

    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArticleOverviewAdapter adapter = new ArticleOverviewAdapter(this, data);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "SwipeRefresh event handled.");
        ((ArticleOverviewAdapter)recyclerView.getAdapter()).clearForRefresh();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    private class ArticleOverviewAdapter extends RecyclerView.Adapter<ArticleOverviewViewHolder> {
        private Cursor articleCursor;
        private final Context parentContext;

        public ArticleOverviewAdapter(Context context, Cursor cursor) {
            parentContext = context;
            articleCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            articleCursor.moveToPosition(position);
            return articleCursor.getLong(ArticleLoader.Query._ID);
        }

        @NonNull
        @Override
        public ArticleOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ArticleOverviewViewHolder vh = new ArticleOverviewViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        void clearForRefresh() {
            if (articleCursor != null && !articleCursor.isClosed()) {
                Log.d(TAG, "closing the cursor on refresh");
                articleCursor.close();
            }
        }

        private Date parsePublishedDate() {
            try {
                String date = articleCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                return dateFormat.parse(date);
            } catch (ParseException ex) {
                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");
                return new Date();
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ArticleOverviewViewHolder holder, int position) {
            articleCursor.moveToPosition(position);
            holder.titleView.setText(articleCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                holder.subtitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + articleCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                holder.subtitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + articleCursor.getString(ArticleLoader.Query.AUTHOR)));
            }
            Picasso.with(parentContext)
                    .load(articleCursor.getString(ArticleLoader.Query.THUMB_URL))
                    .into(holder.thumbnailView);

            holder.thumbnailView.setAspectRatio(articleCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        @Override
        public int getItemCount() {
            return articleCursor.getCount();
        }
    }

    public static class ArticleOverviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumbnail) protected DynamicHeightImageView thumbnailView;
        @BindView(R.id.article_title) protected TextView titleView;
        @BindView(R.id.article_subtitle) protected TextView subtitleView;

        public ArticleOverviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
