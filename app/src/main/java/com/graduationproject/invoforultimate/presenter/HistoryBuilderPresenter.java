package com.graduationproject.invoforultimate.presenter;

import android.os.Parcelable;

import com.graduationproject.invoforultimate.entity.bean.TrackHistoryInfo;

/**
 * Created by INvo
 * on 2020-02-12.
 */
public interface HistoryBuilderPresenter extends TrackPresenter {
    void onGetTrackHistoryCallback(TrackHistoryInfo trackHistoryInfo);

    void onGetIntentCallback(Parcelable parcelable);

    void onDeleteCallback();
}
