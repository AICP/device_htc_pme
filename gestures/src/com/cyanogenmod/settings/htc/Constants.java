/*
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

public final class Constants {

    protected static final String KEY_SWIPE_UP = "swipe_up_action_key";
    protected static final String KEY_SWIPE_DOWN = "swipe_down_action_key";
    protected static final String KEY_SWIPE_LEFT = "swipe_left_action_key";
    protected static final String KEY_SWIPE_RIGHT = "swipe_right_action_key";

    protected static final int ACTION_NONE = 0;
    protected static final int ACTION_CAMERA = 1;
    protected static final int ACTION_TORCH = 2;
    protected static final int ACTION_DOZE = 3;

    protected static int mSwipeUpAction;
    protected static int mSwipeDownAction;
    protected static int mSwipeLeftAction;
    protected static int mSwipeRightAction;
}
