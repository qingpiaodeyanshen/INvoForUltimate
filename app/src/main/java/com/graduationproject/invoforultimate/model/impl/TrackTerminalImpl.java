package com.graduationproject.invoforultimate.model.impl;

import android.os.Message;
import android.util.Log;

import com.graduationproject.invoforultimate.presenter.MainBuilderPresenter;
import com.graduationproject.invoforultimate.presenter.TrackPresenter;
import com.graduationproject.invoforultimate.utils.TerminalUtil;
import com.graduationproject.invoforultimate.model.TrackTerminal;
import com.graduationproject.invoforultimate.service.TrackThread;

import org.json.JSONException;
import org.json.JSONObject;

import static com.graduationproject.invoforultimate.entity.constants.TerminalModuleConstants.*;

/**
 * Created by INvo
 * on 2020-02-07.
 */
public class TrackTerminalImpl implements TrackTerminal {

    private TrackThread trackThread;
    //    private TerminalTrackModel terminalModel;
    private MainBuilderPresenter mainBuilderPresenter;
    private String terminalName;

    public TrackTerminalImpl(TrackPresenter trackPresenter) {
        this.mainBuilderPresenter = (MainBuilderPresenter) trackPresenter;
    }

    public void createTerminal(String s) {
        this.terminalName = s;
        trackThread = new TrackThread(CREATE_TERMINAL, s, this);
        trackThread.start();
        try {
            trackThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void checkTerminal() {
        Long l = TerminalUtil.getTerminal();
        if (l == 0) {
            mainBuilderPresenter.checkTerminalCallback(false);
        } else {
            mainBuilderPresenter.checkTerminalCallback(true);
        }
    }

    @Override
    public void createTrackCallback(Message message) {
        String tid = null;
        if (MSG_TERMINAL_SUCCESS == message.what) {
            try {
                JSONObject jsonObject = new JSONObject(message.obj.toString());
                tid = jsonObject.getJSONObject("data").getString("tid");
                Log.d("my", tid);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                TerminalUtil.setTerminal(tid, terminalName);
                new TrackThread(CREATE_TRACK_COUNT, tid, null).start();
                mainBuilderPresenter.createTerminalCallback(RESULT_TERMINAL_MSG_SUCCESS);
            }
        }
        if (MSG_TERMINAL_INVALID_ERROR == message.what) {
            mainBuilderPresenter.createTerminalCallback(RESULT_TERMINAL_MSG_INVALID_PARAMS);
        }
        if (MSG_TERMINAL_EXIST_ERROR == message.what) {
            mainBuilderPresenter.createTerminalCallback(RESULT_TERMINAL_MSG_EXISTING_ELEMENT);
        }
    }
}
