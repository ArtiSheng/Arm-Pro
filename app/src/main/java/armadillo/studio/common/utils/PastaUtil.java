/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import armadillo.studio.CloudApp;

public class PastaUtil {

    public static String paste() {
        ClipboardManager manager = (ClipboardManager) CloudApp.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                String addedTextString = String.valueOf(addedText);
                if (!TextUtils.isEmpty(addedTextString)) {
                    return addedTextString;
                }
            }
        }
        return null;
    }

    public static void clear() {
        ClipboardManager manager = (ClipboardManager) CloudApp.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
