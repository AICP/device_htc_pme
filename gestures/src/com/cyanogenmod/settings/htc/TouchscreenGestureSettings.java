/*
 * Copyright (C) 2016 The CyanogenMod Project
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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.util.Log;

public class TouchscreenGestureSettings extends PreferenceActivity {

    private static final boolean DEBUG = false;
    public static final String TAG = "GestureSsttings";

    private ListPreference mSwipeUp;
    private ListPreference mSwipeDown;
    private ListPreference mSwipeLeft;
    private ListPreference mSwipeRight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gesture_panel);
        PreferenceScreen prefSet = getPreferenceScreen();
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
        loadPreferences(sharedPrefs);

        mSwipeUp = (ListPreference) prefSet.findPreference(Constants.KEY_SWIPE_UP);
        mSwipeUp.setSummary(mSwipeUp.getEntry());

        mSwipeDown = (ListPreference) prefSet.findPreference(Constants.KEY_SWIPE_DOWN);
        mSwipeDown.setSummary(mSwipeDown.getEntry());

        mSwipeLeft = (ListPreference) prefSet.findPreference(Constants.KEY_SWIPE_LEFT);
        mSwipeLeft.setSummary(mSwipeLeft.getEntry());

        mSwipeRight = (ListPreference) prefSet.findPreference(Constants.KEY_SWIPE_RIGHT);
        mSwipeRight.setSummary(mSwipeRight.getEntry());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getListView().setPadding(0, 0, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPreferences(SharedPreferences sharedPreferences) {
        try {
            Constants.mSwipeUpAction = Integer.parseInt(sharedPreferences.getString(
                    Constants.KEY_SWIPE_UP, Integer.toString(Constants.ACTION_NONE)));
            Constants.mSwipeDownAction = Integer.parseInt(sharedPreferences.getString(
                    Constants.KEY_SWIPE_DOWN, Integer.toString(Constants.ACTION_NONE)));
            Constants.mSwipeLeftAction = Integer.parseInt(sharedPreferences.getString(
                    Constants.KEY_SWIPE_LEFT, Integer.toString(Constants.ACTION_NONE)));
            Constants.mSwipeRightAction = Integer.parseInt(sharedPreferences.getString(
                    Constants.KEY_SWIPE_RIGHT, Integer.toString(Constants.ACTION_NONE)));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error loading preferences");
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            try {
                if (Constants.KEY_SWIPE_UP.equals(key)) {
                    Constants.mSwipeUpAction = Integer.parseInt(sharedPreferences.getString(
                            Constants.KEY_SWIPE_UP, Integer.toString(Constants.ACTION_NONE)));
                    mSwipeUp.setSummary(mSwipeUp.getEntry());
                } else if (Constants.KEY_SWIPE_DOWN.equals(key)) {
                    Constants.mSwipeDownAction = Integer.parseInt(sharedPreferences.getString(
                            Constants.KEY_SWIPE_DOWN, Integer.toString(Constants.ACTION_NONE)));
                    mSwipeDown.setSummary(mSwipeDown.getEntry());
                } else if (Constants.KEY_SWIPE_LEFT.equals(key)) {
                    Constants.mSwipeLeftAction = Integer.parseInt(sharedPreferences.getString(
                            Constants.KEY_SWIPE_LEFT, Integer.toString(Constants.ACTION_NONE)));
                    mSwipeLeft.setSummary(mSwipeLeft.getEntry());
                } else if (Constants.KEY_SWIPE_RIGHT.equals(key)) {
                    Constants.mSwipeRightAction = Integer.parseInt(sharedPreferences.getString(
                            Constants.KEY_SWIPE_RIGHT, Integer.toString(Constants.ACTION_NONE)));
                    mSwipeRight.setSummary(mSwipeRight.getEntry());
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error loading preferences");
            }
        }
    };
}
