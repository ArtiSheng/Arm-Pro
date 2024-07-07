/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import armadillo.studio.accessibility.TopAccessibility;


public class Accessibility {
    public static boolean isAccessibilitySettingsOn(@NotNull Context mContext) {
        int accessibilityEnabled = 0;
        String service = mContext.getPackageName() + "/" + TopAccessibility.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
