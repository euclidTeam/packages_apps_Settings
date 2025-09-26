package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;

public class EuclidVersionPreferenceController extends BasePreferenceController {

    @VisibleForTesting
    static final String EUCLID_VERSION_PROPERTY = "org.euclid.version";
    static final String EUCLID_CODENAME_PROPERTY = "ro.euclid.codename";

    public EuclidVersionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return !TextUtils.isEmpty(SystemProperties.get(EUCLID_VERSION_PROPERTY)) && !TextUtils.isEmpty(SystemProperties.get(EUCLID_CODENAME_PROPERTY))
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        String euclidVersion = SystemProperties.get(EUCLID_VERSION_PROPERTY);
        String euclidCodename = SystemProperties.get(EUCLID_CODENAME_PROPERTY);
        if (!euclidVersion.isEmpty() && !euclidCodename.isEmpty()) {
            return euclidVersion + " | " + euclidCodename;
        } else {
            return
                mContext.getString(R.string.device_info_default);
        }
    }
}
