package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

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
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);

        if (preference == null) return;

        Resources res = mContext.getResources();
        String maintainer = SystemProperties.get("ro.maintainer.name", "").trim();
        String[] officialMaintainers = res.getStringArray(R.array.official_maintainers);

        if (maintainer.isEmpty()) {
            // Property not set
            preference.setSummary(res.getString(R.string.maintainer_unknown));
            return;
        }

        // Check if maintainer is official
        boolean isOfficial = false;
        for (String m : officialMaintainers) {
            if (m.equalsIgnoreCase(maintainer)) {
                isOfficial = true;
                break;
            }
        }

        // Choose icon based on official/unofficial
        int iconRes = isOfficial ? R.drawable.ic_verified : R.drawable.ic_unverified;
        Drawable icon = mContext.getDrawable(iconRes);

        if (icon != null) {
            // Scale icon to 20dp for consistent display
            int size = (int) (res.getDisplayMetrics().density * 20);
            icon.setBounds(0, 0, size, size);
        }

        // Build summary with inline icon
        String label = maintainer + " (" + (isOfficial ? "OFFICIAL" : "UNOFFICIAL") + ") ";
        SpannableStringBuilder builder = new SpannableStringBuilder(label);

        if (icon != null) {
            ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        preference.setSummary(builder);
    }
}
