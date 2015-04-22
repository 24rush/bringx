package com.rinf.bringx.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.rinf.bringx.R;

import java.util.concurrent.Callable;

public class AlertGenerator {

    public static void ShowOkCancelAlert(Context ctx, int titleId, int msgId, final Callable onOk, Callable onCancel) {
        Localization localization = new Localization(ctx);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx)
                .setTitle(localization.getText(titleId))
                .setMessage(localization.getText(msgId))
                .setPositiveButton(localization.getText(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    onOk.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(localization.getText(R.string.btn_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        alertDialog.show();
    }

    public static void ShowOkAlert(Context ctx, int titleId, int msgId, final Callable onOk) {
        Localization localization = new Localization(ctx);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx)
                .setTitle(localization.getText(titleId))
                .setMessage(localization.getText(msgId))
                .setPositiveButton(localization.getText(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if (onOk != null)
                                        onOk.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

        alertDialog.show();
    }
}
