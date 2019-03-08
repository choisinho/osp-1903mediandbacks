package app.bqlab.mediandbacks;

import android.view.View;
import android.view.ViewGroup;

class ChildrenEnable {
    static void set(boolean enable, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                set(enable, (ViewGroup) child);
            }
        }
    }
}
