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

    // Always available, preference is never hidden
    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        Resources res = mContext.getResources();
        String maintainer = SystemProperties.get("ro.maintainer.name", "").trim();
        String[] officialMaintainers = res.getStringArray(R.array.official_maintainers);

        if (maintainer.isEmpty()) {
            // Property not set at all
            preference.setSummary(res.getString(R.string.maintainer_unknown));
            return;
        }

        boolean isOfficial = false;
        for (String m : officialMaintainers) {
            if (m.equalsIgnoreCase(maintainer)) {
                isOfficial = true;
                break;
            }
        }

        if (isOfficial) {
            preference.setSummary(maintainer + " (" + res.getString(R.string.maintainer_official) + ")");
        } else {
            preference.setSummary(res.getString(R.string.maintainer_unofficial));
        }
    }
}
