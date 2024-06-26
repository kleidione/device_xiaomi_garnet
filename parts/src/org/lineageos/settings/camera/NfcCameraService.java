/*
 * Copyright (C) 2023 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.camera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import java.util.Set;

public class NfcCameraService extends Service {

    private static final String TAG = "NfcCameraService";
    private static final String SYSPROP = "persist.nfc.camera.pause_polling";

    private static final int MAX_POLLING_PAUSE_TIMEOUT = 40000;
    private static final String FRONT_CAMERA_ID = "1";

    private static final Set<String> IGNORED_PACKAGES = Set.of(
        "co.aospa.sense", // face unlock
        "com.google.android.as" // auto rotate, screen attention etc
    );

    private NfcAdapter mNfcAdapter;
    private CameraManager mCameraManager;
    private boolean mIsFrontCamInUse = false;

    private final Handler mHandler = new Handler();

    private final CameraManager.AvailabilityCallback mCameraCallback =
            new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraOpened(String cameraId, String packageId) {
            dlog("onCameraOpened id=" + cameraId + " package=" + packageId);
            if (cameraId.equals(FRONT_CAMERA_ID) && !IGNORED_PACKAGES.contains(packageId)) {
                mIsFrontCamInUse = true;
                updateNfcPollingState();
            }
        }

        @Override
        public void onCameraClosed(String cameraId) {
            dlog("onCameraClosed id=" + cameraId);
            if (cameraId.equals(FRONT_CAMERA_ID) && mIsFrontCamInUse) {
                mIsFrontCamInUse = false;
                updateNfcPollingState();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        dlog("onCreate");
        mCameraManager = getSystemService(CameraManager.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dlog("onStartCommand");
        mCameraManager.registerAvailabilityCallback(mCameraCallback, mHandler);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        dlog("onDestroy");
        mCameraManager.unregisterAvailabilityCallback(mCameraCallback);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startService(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            Log.i(TAG, "No nfc on this device");
        } else if (SystemProperties.getBoolean(SYSPROP, false)) {
            Log.i(TAG, "Disabled via system prop");
        } else {
            context.startServiceAsUser(new Intent(context, NfcCameraService.class), UserHandle.CURRENT);
        }
    }

    private NfcAdapter getNfcAdapter() {
        if (mNfcAdapter == null) {
            dlog("getNfcAdapter: mNfcAdapter=null");
            try {
                mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            } catch (Exception e) {
                Log.e(TAG, "getNfcAdapter failed!", e);
            }
        }
        return mNfcAdapter;
    }

    private void updateNfcPollingState() {
        final NfcAdapter adapter = getNfcAdapter();
        if (adapter == null) {
            Log.e(TAG, "updateNfcPollingState: NfcAdapter is null!");
            return;
        }
        if (!adapter.isEnabled()) {
            dlog("updateNfcPollingState: nfc is disabled");
            return;
        }
        if (mIsFrontCamInUse) {
            Log.i(TAG, "Front cam in use, pause polling");
            pausePolling(adapter);
        } else {
            Log.i(TAG, "Front cam not in use, resume polling");
            mHandler.removeCallbacksAndMessages(null);           
            adapter.resumePolling();
        }
    }

    private void pausePolling(NfcAdapter adapter) {
        adapter.pausePolling(MAX_POLLING_PAUSE_TIMEOUT);
        mHandler.postDelayed(() -> {
            if (adapter.isEnabled()) {
                Log.i(TAG, "Front cam still in use, polling pause timed out, pausing again");
                pausePolling(adapter);
            } else {
                dlog("pausePolling: nfc is disabled");
            }
        }, MAX_POLLING_PAUSE_TIMEOUT + 100);
    }

    private static void dlog(String msg) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, msg);
        }
    }
}
