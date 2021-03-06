package com.graduationproject.invoforultimate.model.impl;

import android.os.Bundle;
import android.os.Looper;

import com.amap.api.maps.model.LatLng;
import com.amap.api.track.AMapTrackClient;
import com.amap.api.track.query.entity.DriveMode;
import com.amap.api.track.query.entity.Point;
import com.amap.api.track.query.entity.Track;
import com.amap.api.track.query.model.QueryTerminalRequest;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.amap.api.track.query.model.QueryTrackRequest;
import com.amap.api.track.query.model.QueryTrackResponse;
import com.graduationproject.invoforultimate.listener.OnTrackListenerImpl;
import com.graduationproject.invoforultimate.model.TrackReplay;
import com.graduationproject.invoforultimate.presenter.ReplayBuilderPresenter;
import com.graduationproject.invoforultimate.presenter.TrackPresenter;
import com.graduationproject.invoforultimate.service.TrackThread;
import com.graduationproject.invoforultimate.utils.TerminalUtil;
import com.graduationproject.invoforultimate.entity.bean.TrackIntentParcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.graduationproject.invoforultimate.app.TrackApplication.getContext;
import static com.graduationproject.invoforultimate.entity.constants.HttpUrlConstants.SERVICE_ID;
import static com.graduationproject.invoforultimate.entity.constants.TrackReplayConstants.*;

/**
 * Created by INvo
 * on 2020-02-10.
 */
public class TrackReplayImpl implements TrackReplay {

    private AMapTrackClient aMapTrackClient;
    private Bundle bundle;
    //    private TrackReplay trackReplayModel;
    private ArrayList<LatLng> latLngArrayList = new ArrayList<>();
    private boolean canReplay;
    private int replayCounts;
    private LatLng replayLatLng;
    private ReplayBuilderPresenter replayBuilderPresenter;

    public TrackReplayImpl(Bundle bundle, TrackPresenter trackPresenter) {
        this.bundle = bundle;
        this.replayBuilderPresenter = (ReplayBuilderPresenter) trackPresenter;
        trackReplay();
    }

    public void markerReplay() {
        canReplay = true;
        replayCounts = 0;
        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                while (canReplay) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (replayCounts < latLngArrayList.size()) {
                        replayLatLng = latLngArrayList.get(replayCounts);
                        replayBuilderPresenter.onMarkerReplayCallback(replayLatLng);
                        replayCounts++;
                    } else {
                        canReplay = false;
                    }
                }
            }
        };
        thread.start();
        Looper.loop();
    }

    public void trackReplay() {
        TrackIntentParcelable trackIntentParcelable = bundle.getParcelable("track_history");

        int trackID = trackIntentParcelable.getTrackID();
        long startUnix = trackIntentParcelable.getStartUnix();
        long endUnix = trackIntentParcelable.getEndUnix();
        aMapTrackClient = new AMapTrackClient(getContext());
        aMapTrackClient.queryTerminal(new QueryTerminalRequest(SERVICE_ID, TerminalUtil.getTerminalName()), new OnTrackListenerImpl() {
            @Override
            public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
                if (queryTerminalResponse.isSuccess()) {
                    if (queryTerminalResponse.isTerminalExist()) {
                        QueryTrackRequest queryTrackRequest = new QueryTrackRequest(
                                SERVICE_ID,
                                TerminalUtil.getTerminal(),
                                trackID,     // 轨迹ID
                                startUnix,  //开始时间戳
                                endUnix,    //结束时间戳
                                0,      // 不启用去噪
                                0,   // 启用绑路
                                0,      // 不进行精度过滤
                                DriveMode.DRIVING,  // 当前仅支持驾车模式
                                0,     // 距离补偿
                                5000,   // 距离补偿，只有超过5km的点才启用距离补偿
                                1,  // 结果应该包含轨迹点信息
                                1,  // 返回1：1数据
                                100    // 一页不超过100条
                        );
                        aMapTrackClient.queryTerminalTrack(queryTrackRequest, new OnTrackListenerImpl() {
                            @Override
                            public void onQueryTrackCallback(QueryTrackResponse queryTrackResponse) {
                                if (queryTrackResponse.isSuccess()) {
                                    List<Track> tracks = queryTrackResponse.getTracks();
                                    if (tracks != null && !tracks.isEmpty()) {
                                        boolean allEmpty = true;
                                        for (Track track : tracks) {
                                            List<Point> points = track.getPoints();
                                            if (points != null && points.size() > 0) {
                                                allEmpty = false;
                                                /**
                                                 *将采取的坐标点绘制在MapView上
                                                 */
                                                replayBuilderPresenter.onTrackPointsCallback(points);
                                            }
                                        }
                                        if (allEmpty) {
                                            replayBuilderPresenter.onTrackPointsResultCallback(ALL_EMPTY);
                                        } else {
                                            StringBuilder stringBuilder = new StringBuilder();
                                            stringBuilder.append(TRACK_REPLAY_SUCCESS);
                                            for (Track track : tracks) {
                                                stringBuilder.append(track.getDistance()).append("m");
                                            }
                                            replayBuilderPresenter.onTrackPointsResultCallback(stringBuilder.toString());
                                        }
                                    } else {
                                        replayBuilderPresenter.onTrackPointsResultCallback(TRACKS_EMPTY);
                                    }
                                } else {
                                    replayBuilderPresenter.onTrackPointsResultCallback(TRACK_NOT_RESPONSE);
                                }
                            }
                        });
                    } else {
                        replayBuilderPresenter.onTrackPointsResultCallback(TERMINAL_NOT_EXIST);
                    }
                } else {
                    replayBuilderPresenter.onTrackPointsResultCallback(NOT_NETWORK + queryTerminalResponse.getErrorMsg());
                }
            }
        });

        Thread thread = new TrackThread(TRACK_COORDINATE, trackID, this);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetListTem(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result).getJSONObject("data").getJSONArray("tracks").getJSONObject(0);
            JSONArray jsonArray = jsonObject.getJSONArray("points");
            int count = Integer.parseInt(jsonObject.getString("counts"));
            for (int i = 0; i < count; i++) {
                String latLng = jsonArray.getJSONObject(i).getString("location");
                latLngArrayList.add(new LatLng(Double.valueOf(latLng.split(",")[1]), Double.valueOf(latLng.split(",")[0])));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
