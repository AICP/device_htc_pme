/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2014 SlimRoms Project
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.htc;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraAccessException;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

public class HtcGestureService extends Service {

    private static final boolean DEBUG = false;
    public static final String TAG = "GestureService";

    private static final String DOZE_INTENT = "com.android.systemui.doze.pulse";

    private static final int SENSOR_WAKELOCK_DURATION = 200;

    private Context mContext;
    private CameraManager mCameraManager;
    private GestureMotionSensor mGestureSensor;
    private PowerManager mPowerManager;
    private WakeLock mSensorWakeLock;

    private String mTorchCameraId;
    private boolean mTorchEnabled = false;

    private GestureMotionSensor.GestureMotionSensorListener mListener =
        new GestureMotionSensor.GestureMotionSensorListener() {
        @Override
        public void onEvent(int type, SensorEvent event) {
            if (DEBUG) Log.d(TAG, "Received event: " + type);
            switch (type) {
                case GestureMotionSensor.SENSOR_GESTURE_DOUBLE_TAP:
                    mPowerManager.wakeUp(SystemClock.uptimeMillis());
                    break;
                case GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP:
                case GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN:
                case GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT:
                case GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT:
                    handleGestureAction(gestureToAction(type));
                    break;
                case GestureMotionSensor.SENSOR_GESTURE_CAMERA:
                    handleCameraActivation();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();
        mContext = this;
        mGestureSensor = GestureMotionSensor.getInstance(mContext);
        mGestureSensor.registerListener(mListener);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mSensorWakeLock = mPowerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "HtcGestureWakeLock");
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mCameraManager.registerTorchCallback(mTorchCallback, null);
        mTorchCameraId = getTorchCameraId();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenStateReceiver, screenStateFilter);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        unregisterReceiver(mScreenStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onDisplayOn() {
        if (DEBUG) Log.d(TAG, "Display on");
        if (isDoubleTapEnabled()) {
            mGestureSensor.disableGesture(GestureMotionSensor.SENSOR_GESTURE_DOUBLE_TAP);
        }
        if (Constants.mSwipeUpAction != Constants.ACTION_NONE) {
            mGestureSensor.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP);
        }
        if (Constants.mSwipeDownAction != Constants.ACTION_NONE) {
            mGestureSensor.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN);
        }
        if (Constants.mSwipeLeftAction != Constants.ACTION_NONE) {
            mGestureSensor.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT);
        }
        if (Constants.mSwipeRightAction != Constants.ACTION_NONE) {
            mGestureSensor.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT);
        }
        mGestureSensor.stopListening();
    }

    private void onDisplayOff() {
        if (DEBUG) Log.d(TAG, "Display off");
        if (isDoubleTapEnabled()) {
            mGestureSensor.enableGesture(GestureMotionSensor.SENSOR_GESTURE_DOUBLE_TAP);
        }
        if (Constants.mSwipeUpAction != Constants.ACTION_NONE) {
            mGestureSensor.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP);
        }
        if (Constants.mSwipeDownAction != Constants.ACTION_NONE) {
            mGestureSensor.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN);
        }
        if (Constants.mSwipeLeftAction != Constants.ACTION_NONE) {
            mGestureSensor.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT);
        }
        if (Constants.mSwipeRightAction != Constants.ACTION_NONE) {
            mGestureSensor.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT);
        }
        mGestureSensor.beginListening();
    }

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onDisplayOff();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onDisplayOn();
            }
        }
    };

    private void handleGestureAction(int action) {
        if (DEBUG) Log.d(TAG, "Performing gesture action: " + action);
        switch (action) {
            case Constants.ACTION_CAMERA:
                handleCameraActivation();
                break;
            case Constants.ACTION_TORCH:
                handleFlashlightActivation();
                onDisplayOn();
                onDisplayOff();
                break;
            case Constants.ACTION_DOZE:
                launchDozePulse();
                onDisplayOn();
                onDisplayOff();
                break;
            case Constants.ACTION_NONE:
            default:
                break;
        }
    }

    private int gestureToAction(int gesture) {
        switch (gesture) {
            case GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP:
                return Constants.mSwipeUpAction;
            case GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN:
                return Constants.mSwipeDownAction;
            case GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT:
                return Constants.mSwipeLeftAction;
            case GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT:
                return Constants.mSwipeRightAction;
            default:
                return -1;
        }
    }

    private boolean isDoubleTapEnabled() {
        return (Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.DOUBLE_TAP_TO_WAKE, 0) != 0);
    }

    private void launchDozePulse() {
        if (DEBUG) Log.d(TAG, "Launch doze pulse");
        mSensorWakeLock.acquire(SENSOR_WAKELOCK_DURATION);
        mContext.sendBroadcastAsUser(new Intent(DOZE_INTENT),
                new UserHandle(UserHandle.USER_CURRENT));
    }

    private void handleCameraActivation() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);
        launchCamera();
    }

    private void handleFlashlightActivation() {
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);
        launchFlashlight();
    }

    private void launchCamera() {
        mSensorWakeLock.acquire(SENSOR_WAKELOCK_DURATION);
        mPowerManager.wakeUp(SystemClock.uptimeMillis());
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            mContext.startActivityAsUser(intent, null, new UserHandle(UserHandle.USER_CURRENT));
        } catch (ActivityNotFoundException e) {
            /* Ignore */
        }
    }

    private void launchFlashlight() {
        mSensorWakeLock.acquire(SENSOR_WAKELOCK_DURATION);
        try {
            mCameraManager.setTorchMode(mTorchCameraId, !mTorchEnabled);
        } catch (CameraAccessException e) {
            // Ignore
        }
    }

    private String getTorchCameraId() {
        try {
            for (final String id : mCameraManager.getCameraIdList()) {
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(id);
                int direction = cc.get(CameraCharacteristics.LENS_FACING);
                if (direction == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            // Ignore
        }

        return null;
    }

    private CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (!cameraId.equals(mTorchCameraId))
                return;
            mTorchEnabled = enabled;
        }

        @Override
        public void onTorchModeUnavailable(String cameraId) {
            if (!cameraId.equals(mTorchCameraId))
                return;
            mTorchEnabled = false;
        }
    };
}
