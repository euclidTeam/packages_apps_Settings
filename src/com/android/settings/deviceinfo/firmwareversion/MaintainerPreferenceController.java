package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class MaintainerPreferenceController extends BasePreferenceController {

    private static final String KEY_MAINTAINER = "maintainer_info";

    public MaintainerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        String maintainer = SystemProperties.get("ro.maintainer.name", "").trim();
        if (maintainer.isEmpty()) return UNSUPPORTED_ON_DEVICE;

        Resources res = mContext.getResources();
        String[] officialMaintainers = res.getStringArray(R.array.official_maintainers);

        for (String m : officialMaintainers) {
            if (m.equalsIgnoreCase(maintainer)) {
                return AVAILABLE; // Show preference if in whitelist
            }
        }
        return UNSUPPORTED_ON_DEVICE; // Hide if not in whitelist
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        String maintainer = SystemProperties.get("ro.maintainer.name", "").trim();
        if (!maintainer.isEmpty()) {
            preference.setSummary(maintainer + " (Official)");
        } else {
            preference.setSummary(mContext.getString(R.string.maintainer_unknown));
        }
    }
}
