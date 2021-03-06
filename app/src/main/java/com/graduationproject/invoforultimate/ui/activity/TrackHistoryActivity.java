package com.graduationproject.invoforultimate.ui.activity;

import androidx.annotation.CheckResult;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import butterknife.BindView;

import android.os.Parcelable;
import android.transition.Explode;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;

import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.graduationproject.invoforultimate.BaseActivity;
import com.graduationproject.invoforultimate.R;
import com.graduationproject.invoforultimate.adapter.TrackHistoryAdapter;
import com.graduationproject.invoforultimate.utils.TerminalUtil;
import com.graduationproject.invoforultimate.entity.bean.TrackHistoryInfo;
import com.graduationproject.invoforultimate.entity.constants.TrackHistoryConstants;
import com.graduationproject.invoforultimate.listener.OnTrackAdapterListener;
import com.graduationproject.invoforultimate.presenter.impl.HistoryBuilderImpl;
import com.graduationproject.invoforultimate.ui.view.impl.HistoryViewCallback;
import com.graduationproject.invoforultimate.utils.Decoration;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import static com.graduationproject.invoforultimate.R2.id.swipe_refresh;
import static com.graduationproject.invoforultimate.R2.id.track_history;
import static com.graduationproject.invoforultimate.entity.constants.TrackHistoryConstants.*;

public class TrackHistoryActivity extends BaseActivity<HistoryViewCallback, HistoryBuilderImpl, TextureMapView> implements OnTrackAdapterListener, HistoryViewCallback {
    public static final String TAG = TrackHistoryConstants.TAG;
    @BindView(track_history)
    RecyclerView recyclerView;
    @BindView(swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

    @Override
    protected void initControls(Bundle savedInstanceState) {
        Explode explode = new Explode();
        explode.excludeTarget(android.R.id.statusBarBackground,true);
        explode.setDuration(700L);
        Window window = this.getWindow();
        window.setEnterTransition(explode);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorBlack, R.color.colorGreen, R.color.colorBlue, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getP().getTrackHistoryInfo(null);
            swipeRefreshLayout.setRefreshing(true);
        });


        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new Decoration());
        getP().getTrackHistoryInfo(this);
    }

    @MainThread
    @Override
    public void onGetTrackHistoryResult(@Nullable TrackHistoryInfo trackHistoryInfo) {
        runOnUiThread(() -> {
            recyclerView.setAdapter(new TrackHistoryAdapter(trackHistoryInfo, this));
            if (swipeRefreshLayout.isRefreshing()) {
                ToastText(UPGRADE_STATUS);
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onSwitch(Object var) {
        getP().getTrackHistoryIntent(var);
    }

    @Override
    public void onDelete(Object var) {
        swipeRefreshLayout.setRefreshing(true);
        getP().deleteTrack(var);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_track_history;
    }

    @CheckResult
    @Override
    protected TextureMapView loadMap(Bundle savedInstanceState) {
        return null;
    }

    @CheckResult
    @Override
    protected HistoryBuilderImpl loadP() {
        return new HistoryBuilderImpl();
    }

    @CheckResult
    @Override
    protected HistoryViewCallback loadV() {
        return this;
    }

    @Override
    public void onGetIntentResult(@Nullable Parcelable parcelable) {
        runOnUiThread(() -> TrackHistoryActivity.this.startActivity(new Intent().setClass(TrackHistoryActivity.this, TrackReplayActivity.class).putExtra("track_history", parcelable)));
    }

    @Override
    public void onDeleteResult() {
        runOnUiThread(() -> {
            getP().getTrackHistoryInfo(null);
            ToastText(DELETE_STATUS);
            swipeRefreshLayout.setRefreshing(false);
        });
    }
}
