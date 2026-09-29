package com.touchv.game;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * ViewGroup that places every child by fractional bounds of the plate
 * (0..1 of width/height). Overlay tap targets therefore track the full-screen
 * plate at any screen size, and the toolkit marks can be expressed once as
 * fractions of the 720x1600 design plate instead of pixels.
 */
public final class PlateLayout extends ViewGroup {

    /** Fractional layout params: fx/fy = top-left, fw/fh = size, all 0..1. */
    public static final class LP extends ViewGroup.LayoutParams {
        public final float fx, fy, fw, fh;

        public LP(float fx, float fy, float fw, float fh) {
            super(0, 0);
            this.fx = fx;
            this.fy = fy;
            this.fw = fw;
            this.fh = fh;
        }
    }

    public PlateLayout(Context context) {
        super(context);
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void add(View child, float fx, float fy, float fw, float fh) {
        addView(child, new LP(fx, fy, fw, fh));
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int w = MeasureSpec.getSize(widthSpec);
        int h = MeasureSpec.getSize(heightSpec);
        setMeasuredDimension(w, h);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            ViewGroup.LayoutParams p = child.getLayoutParams();
            if (p instanceof LP) {
                LP lp = (LP) p;
                int cw = lp.fw > 0 ? Math.round(lp.fw * w) : w;
                int ch = lp.fh > 0 ? Math.round(lp.fh * h) : h;
                child.measure(MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(ch, MeasureSpec.EXACTLY));
            } else {
                child.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            ViewGroup.LayoutParams p = child.getLayoutParams();
            if (p instanceof LP) {
                LP lp = (LP) p;
                int x = Math.round(lp.fx * w);
                int y = Math.round(lp.fy * h);
                int cw = lp.fw > 0 ? Math.round(lp.fw * w) : child.getMeasuredWidth();
                int ch = lp.fh > 0 ? Math.round(lp.fh * h) : child.getMeasuredHeight();
                child.layout(x, y, x + cw, y + ch);
            } else {
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LP(0f, 0f, 1f, 1f);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(android.util.AttributeSet attrs) {
        return new LP(0f, 0f, 1f, 1f);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LP;
    }
}
