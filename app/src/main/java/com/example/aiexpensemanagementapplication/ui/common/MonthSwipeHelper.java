
package com.example.aiexpensemanagementapplication.ui.common;

import android.view.MotionEvent;
import android.view.View;

public final class MonthSwipeHelper {

    private MonthSwipeHelper() {}

    public static void attach(
            View card,
            Runnable previous,
            Runnable next) {

        card.setOnTouchListener(new View.OnTouchListener() {

            private float startX, startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        float dx = event.getX() - startX;
                        float dy = event.getY() - startY;

                        if (Math.abs(dx) > 60 &&
                                Math.abs(dx) > Math.abs(dy)) {

                            if (dx > 0) {
                                previous.run();
                            } else {
                                next.run();
                            }

                        } else {
                            v.performClick();
                        }

                        return true;
                }

                return true;
            }
        });
    }
}
