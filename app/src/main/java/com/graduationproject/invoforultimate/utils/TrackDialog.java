package com.graduationproject.invoforultimate.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.graduationproject.invoforultimate.R;

import static com.graduationproject.invoforultimate.entity.constants.MainConstants.*;

/**
 * Created by INvo
 * on 2020-02-09.
 */
public class TrackDialog extends AlertDialog.Builder {


    public TrackDialog(Context context, int type) {
        super(context);
        if (DIALOG_START_TRACK == type) {
            setTitle(BOTTOM_NAVIGATION_TITLE)
                    .setMessage(BOTTOM_NAVIGATION_MESSAGE)
                    .setIcon(R.drawable.sport)
                    .setCancelable(true);
        }
        if (DIALOG_TRACK_NOT_UPLOAD == type) {
            setTitle(DIALOG_CHECK_TITLE)
                    .setMessage(DIALOG_CHECK_MESSAGE)
                    .setCancelable(true);
        }
        if (DIALOG_CREATE_TERMINAL == type) {
            setTitle(ALTER_DIALOG_TITLE)
                    .setMessage(ALTER_DIALOG_MESSAGE)
                    .setCancelable(false);
        }
        if (DIALOG_EXIT_APP == type) {
            setTitle(DIALOG_EXIT_TITLE)
                    .setCancelable(true);
        }
        if (DIALOG_STOP_TRACK == type) {
            setTitle(DIALOG_EXIT_TRACK)
                    .setCancelable(true);
        }
    }
}
