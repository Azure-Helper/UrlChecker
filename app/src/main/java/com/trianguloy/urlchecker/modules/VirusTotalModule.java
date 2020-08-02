package com.trianguloy.urlchecker.modules;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.utilities.UrlUtilities;
import com.trianguloy.urlchecker.utilities.VirusTotalUtility;

/**
 * This module uses the VirusTotal api (https://developers.virustotal.com/reference) for url reports
 */
public class VirusTotalModule extends BaseModule implements View.OnClickListener, View.OnLongClickListener {

    private static final int RETRY_TIMEOUT = 5000;
    private ImageButton btn_scan;
    private TextView txt_result;

    private boolean scanning = false;
    private VirusTotalUtility.InternalReponse result = null;

    public VirusTotalModule(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_virustotal;
    }

    @Override
    public void onInitialize(View views) {
        btn_scan = views.findViewById(R.id.scan);
        txt_result = views.findViewById(R.id.result);

        btn_scan.setOnClickListener(this);
        txt_result.setOnClickListener(this);
        txt_result.setOnLongClickListener(this);
    }

    @Override
    public void onNewUrl(String url) {
        scanning = false;
        result = null;
        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                scanOrCancel();
                break;
            case R.id.result:
                showInfo(false);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.result:
                showInfo(true);
                return true;
        }
        return false;
    }


    /**
     * Performs a scan of the current url, in background.
     * Or cancels current scan
     */
    private void scanOrCancel() {
        if (scanning) {
            // already scanning? cancel
            scanning = false;
        } else {
            // start scan
            scanning = true;
            new Thread(new Runnable() {
                public void run() {
                    _scanUrl();
                }
            }).start();
        }
        updateUI();
    }

    /**
     * Manages the scanning in the background
     */
    private void _scanUrl() {
        VirusTotalUtility.InternalReponse response;
        while (scanning) {
            // asks for the report
            response = VirusTotalUtility.scanUrl(getUrl());

            // check valid report
            if (response.detectionsTotal > 0) {
                result = response;
                scanning = false;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
                return;
            }

            // retry if still no report
            try {
                Thread.sleep(RETRY_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Updates the ui
     */
    private void updateUI() {
        if (scanning) {
            // scanning in progress, show cancel
            btn_scan.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            setResult("Scanning...", 0);
            btn_scan.setEnabled(true);
        } else {
            // not a scanning in progress
            btn_scan.setImageResource(android.R.drawable.ic_menu_search);
            if (result == null) {
                // no result available, new url
                setResult("Press to scan", 0);
                btn_scan.setEnabled(true);
            } else {
                // result available
                if (result.detectionsTotal <= 0) {
                    // this should never happen...
                    setResult("no detections? strange", R.color.warning);
                } else if (result.detectionsPositive > 2) {
                    // more thst two bad detection, bad url
                    setResult("Uh oh, " + result.detectionsPositive + "/" + result.detectionsTotal + " engines detected the url (as of date " + result.date + ")", R.color.bad);
                } else if (result.detectionsPositive > 0) {
                    // 1 or 2 bad ddetectings, warning
                    setResult("Uh oh, " + result.detectionsPositive + "/" + result.detectionsTotal + " engines detected the url (as of date " + result.date + ")", R.color.warning);
                } else {
                    // no detections, good
                    setResult("None of the " + result.detectionsTotal + " engines detected the site (as of date " + result.date + ")", R.color.good);
                }
                btn_scan.setEnabled(false);
            }
        }

    }

    /**
     * Utility to update the ui
     *
     * @param message with this message
     * @param color   and this background color
     */
    private void setResult(String message, int color) {
        txt_result.setText(message);
        txt_result.setBackgroundColor(color == 0 ? Color.TRANSPARENT : getActivity().getResources().getColor(color));
    }

    /**
     * Shows the report results
     *
     * @param details if true, the virustotal page is opened, if false just a basic dialog with the json
     */
    private void showInfo(boolean details) {
        if (result == null) return;

        if (details) {
            UrlUtilities.openUrlRemoveThis(result.scanUrl, getActivity());
        } else {
            new AlertDialog.Builder(getActivity())
                    .setMessage(result.info)
                    .show();
        }
    }
}