package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LoadingFloatingActionButton extends RelativeLayout {

    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;

    public static final int SIZE_MINI = FloatingActionButton.SIZE_MINI;
    public static final int SIZE_NORMAL = FloatingActionButton.SIZE_NORMAL;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SIZE_MINI, SIZE_NORMAL})
    public @interface Size {
    }

    public LoadingFloatingActionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);
        LayoutInflater inflater = LayoutInflater.from(context);

        mFab = new FloatingActionButton(context);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.LoadingFloatingActionButton, 0, 0);
        try {

            if (a.hasValue(R.styleable.LoadingFloatingActionButton_backgroundColor)) {
                int background = a.getColor(R.styleable.LoadingFloatingActionButton_backgroundColor,
                        getPrimaryDarkColor(context));
                mFab.setBackgroundTintList(ColorStateList.valueOf(background));
            }

            if (a.hasValue(R.styleable.LoadingFloatingActionButton_src)) {
                Drawable drawable = a.getDrawable(R.styleable.LoadingFloatingActionButton_src);
                mFab.setImageDrawable(drawable);
            }

            int size = a.getInt(R.styleable.LoadingFloatingActionButton_loadingFabSize, FloatingActionButton.SIZE_NORMAL);
            if (size == SIZE_MINI) {
                mProgressBar = (ProgressBar) inflater.inflate(R.layout.progress_bar_mini, null);
            } else {
                mProgressBar = (ProgressBar) inflater.inflate(R.layout.progress_bar_normal, null);
            }
            mFab.setSize(size);
        } finally {
            a.recycle();
        }

        mProgressBar.setVisibility(INVISIBLE);
        addViewCentered(mFab);
        addViewCentered(mProgressBar);

        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callOnClick();
            }
        });
    }

    private void addViewCentered(View view) {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(view, layoutParams);
    }

    private int getPrimaryDarkColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mFab.setEnabled(enabled);
        mFab.setAlpha((enabled) ? 1f : 0.65f);
    }

    @Override
    public void setClickable(boolean enabled) {
        super.setClickable(enabled);
        mFab.setClickable(enabled);
    }

    public void showProgress() {
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(INVISIBLE);
    }
}
