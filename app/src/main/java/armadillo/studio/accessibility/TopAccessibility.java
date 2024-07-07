package armadillo.studio.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import org.jetbrains.annotations.NotNull;

import armadillo.studio.R;
import armadillo.studio.common.utils.AppUtils;

public class TopAccessibility extends AccessibilityService {
    private static ActivityResult activityResult;

    public static void setActivityResult(ActivityResult result) {
        activityResult = result;
    }

    public interface ActivityResult {
        void Next(String apk_name, String clz);
    }

    @Override
    public void onAccessibilityEvent(@NotNull AccessibilityEvent accessibilityEvent) {
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == accessibilityEvent.getEventType()) {
            if (activityResult != null && accessibilityEvent.getPackageName() != null)
                activityResult.Next(AppUtils.getApplicationName(accessibilityEvent.getPackageName().toString()),
                        accessibilityEvent.getClassName() == null ? getString(R.string.unknown) : accessibilityEvent.getClassName().toString());
        }
    }

    @Override
    public void onInterrupt() {

    }
}
