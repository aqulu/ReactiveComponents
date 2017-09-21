package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class LoadingFloatingActionButton extends RelativeLayout {

    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;

    public LoadingFloatingActionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.loading_floating_action_button, this, true);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mProgressBar = (ProgressBar) view.findViewById(R.id.fab_progress);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.LoadingFloatingActionButton, 0, 0);
        try {

            if (a.hasValue(R.styleable.LoadingFloatingActionButton_backgroundColor)) {
                int background = a.getColor(R.styleable.LoadingFloatingActionButton_backgroundColor,
                        getPrimaryDarkColor(context));

                mFab.setBackgroundTintList(ColorStateList.valueOf(background));
            }

            Drawable drawable;
            if (a.hasValue(R.styleable.LoadingFloatingActionButton_src)) {
                drawable = a.getDrawable(R.styleable.LoadingFloatingActionButton_src);
                mFab.setImageDrawable(drawable);
            }
        } finally {
            a.recycle();
        }

        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callOnClick();
            }
        });
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
