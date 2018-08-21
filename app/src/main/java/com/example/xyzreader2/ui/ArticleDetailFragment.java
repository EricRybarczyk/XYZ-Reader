package com.example.xyzreader2.ui;

import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xyzreader2.R;
import com.example.xyzreader2.data.ArticleLoader;
import com.example.xyzreader2.util.AppendTextScrollHandler;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArticleDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArticleDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_ITEM_ID = "item_id";
    private View rootView;
    private long itemId;
    private Cursor articleCursor;
    private OnFragmentInteractionListener fragmentInteractionListener;
    private int priorScrollY;
    private static final String TAG = ArticleDetailFragment.class.getSimpleName();

    @BindView(R.id.photo) protected ImageView articlePhoto;
    //@BindView(R.id.article_title) protected TextView articleTitle;
    @BindView(R.id.article_byline) protected TextView articleByline;
    @BindView(R.id.article_body) protected TextView articleBody;
    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar) protected CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.article_scroll_container) protected NestedScrollView scrollView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    public ArticleDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param itemId ID value for the Article to be loaded.
     * @return A new instance of fragment ArticleDetailFragment.
     */
    public static ArticleDetailFragment newInstance(long itemId) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated : begin");

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
        Log.d(TAG, "onActivityCreated : loader initialized");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        ButterKnife.bind(this, rootView);
        Log.d(TAG, "onCreateView : Butterknife.bind() was called");

        AppCompatActivity parentActivity = ((AppCompatActivity)getActivity());
        ActionBar actionBar = parentActivity.getSupportActionBar();
        parentActivity.setSupportActionBar(toolbar);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> parentActivity.onBackPressed());

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            fragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentInteractionListener = null;
    }

        //        @BindView(R.id.photo) protected ImageView photoView;
        //        @BindView(R.id.article_title) protected TextView articleTitle;
        //        @BindView(R.id.article_byline) protected TextView articleByline;
        //        @BindView(R.id.article_body) protected TextView articleBody;
    private void bindViews() {
        Log.d(TAG, "bindViews : Enter for itemId = " + String.valueOf(itemId));
        if (rootView == null) {
            Log.d(TAG, "bindViews : rootView is null");
            return;
        }
        if (articleCursor == null) {
            Log.d(TAG, "bindViews : articleCursor is null");
            rootView.setVisibility(View.GONE);
            return;
        }
        rootView.setVisibility(View.VISIBLE);
        Log.d(TAG, "bindViews : begin binding");

        Picasso.with(getActivity())
            .load(articleCursor.getString(ArticleLoader.Query.PHOTO_URL))
            .into(articlePhoto);

        String title = articleCursor.getString(ArticleLoader.Query.TITLE);
        //articleTitle.setText(title);
        collapsingToolbar.setTitle(title);
        //toolbar.setTitle(title);


        Date publishedDate = parsePublishedDate(articleCursor.getString(ArticleLoader.Query.PUBLISHED_DATE));
        String author = articleCursor.getString(ArticleLoader.Query.AUTHOR);

        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            articleByline.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by " + author));

        } else {
            // If date is before 1902, just show the string
            articleByline.setText(Html.fromHtml(
                    outputFormat.format(publishedDate) + " by " + author));

        }

        String fullArticle = articleCursor.getString(ArticleLoader.Query.BODY);
        Log.d(TAG, "Article Length: " + String.valueOf(fullArticle.length()) + "for itemId = " + String.valueOf(itemId));
        String introArticle;
        if (fullArticle.length() >= AppendTextScrollHandler.TEXT_SEGMENT_SIZE) {
            introArticle = fullArticle.substring(0, AppendTextScrollHandler.TEXT_SEGMENT_SIZE);
            Log.d(TAG, "calling setOnScrollChangeListener");
            scrollView.setOnScrollChangeListener(new AppendTextScrollHandler(articleBody, fullArticle));
        } else {
            introArticle = fullArticle;
        }
        articleBody.setText(introArticle);
        Log.d(TAG, "Intro Length: " + String.valueOf(introArticle.length()));

        Log.d(TAG, "bindViews : done binding");
    }


    private Date parsePublishedDate(String dateString) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader : entered");
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished : entered");
        if (!isAdded()) {
            if (articleCursor != null) {
                articleCursor.close();
            }
            Log.d(TAG, "onLoadFinished : !isAdded()");
            return;
        }
        articleCursor = data;
        if (articleCursor != null && !articleCursor.moveToFirst()) {
            Log.e(TAG, "onLoadFinished : Error reading item detail cursor");
            articleCursor.close();
            articleCursor = null;
        }
        Log.d(TAG, "onLoadFinished : calling bindViews()");
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        articleCursor = null;
        Log.d(TAG, "onLoaderReset : calling bindViews()");
        bindViews();
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (fragmentInteractionListener != null) {
            fragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
