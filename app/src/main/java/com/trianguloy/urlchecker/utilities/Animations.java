package com.trianguloy.urlchecker.utilities;

import android.animation.LayoutTransition;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

public class Animations {
    public static void enableAnimations(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
        if (view instanceof ViewGroup) {
            final LayoutTransition lt = ((ViewGroup) view).getLayoutTransition();
            if (lt != null) {
                lt.enableTransitionType(LayoutTransition.CHANGING);
            } else {
                AndroidUtils.assertError(view + " doesn't have a LayoutTransition");
            }
        } else {
            AndroidUtils.assertError(view + " isn't a ViewGroup");
        }
    }
}
