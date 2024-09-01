/*
 * Copyright (C) 2024 euclidOS
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class EuclidReleaseTypePreferenceController extends BasePreferenceController {

    private static final String TAG = "EuclidReleaseTypePreferenceController";
    private static final String EUCLID_RELEASE_TYPE = "ro.euclid.releasetype";

    public EuclidReleaseTypePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        // Retrieve the Euclid release type from system properties
        String euclidReleaseType = SystemProperties.get(EUCLID_RELEASE_TYPE,
                mContext.getString(R.string.device_info_default));

        // Set the summary of the preference to the retrieved value
        preference.setSummary(euclidReleaseType);
    }
}
