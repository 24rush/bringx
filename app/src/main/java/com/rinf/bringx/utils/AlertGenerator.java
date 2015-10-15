package com.rinf.bringx.utils;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.Bindings;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.R;

import java.util.concurrent.Callable;

public class AlertGenerator {

    private static View _reasonControl = null;

    public static void ShowOkCancelAlert(Context ctx, int titleId, int msgId, final INotifier<String> onOk, INotifier<Void> onCancel, int layoutId) {
        _reasonControl = null;
        Localization localization = new Localization(ctx);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx)
                .setTitle(localization.getText(titleId))
                .setMessage(localization.getText(msgId))
                .setCancelable(false)
                .setPositiveButton(localization.getText(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String reason = "";

                                    if (_reasonControl != null) {
                                        EditText edt = (EditText) _reasonControl.findViewById(R.id.edt_reason_rejected);
                                        if (edt != null)
                                            reason = edt.getText().toString();
                                    }

                                    onOk.OnValueChanged(reason);
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

        if (layoutId != 0) {
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            _reasonControl = inflater.inflate(layoutId, null);
            alertDialog.setView(_reasonControl);
        }

        alertDialog.show();
    }

    public static void ShowOkAlert(Context ctx, int titleId, String message, final Callable onOk) {
        Localization localization = new Localization(ctx);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx)
                .setTitle(localization.getText(titleId))
                .setMessage(message)
                .setCancelable(false)
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

    public static void ShowOkAlert(Context ctx, int titleId, int msgId, final Callable onOk) {
        ShowOkAlert(ctx, titleId, new Localization(ctx).getText(msgId), onOk);
    }
}
