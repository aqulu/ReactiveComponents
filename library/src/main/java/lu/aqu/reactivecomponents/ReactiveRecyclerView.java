package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * RecyclerView supporting loading and empty view state
 * the list is considered to be loading, if no adapter has been set yet
 * <br>
 * if an adapter has been set the view will either display the emptyView (if set)
 * or the list itself, if itemCount > 0
 */
public class ReactiveRecyclerView extends RecyclerView implements ReactiveComponent {

    private SingleViewAdapter mEmptyAdapter;
    private SingleViewAdapter mProgressAdapter;

    private boolean mAutoShowProgress;
    private boolean mAutoHideProgress;

    private Adapter mItemAdapter;

    @Nullable
    private ArrayList<ItemDecoration> mItemDecorationCache;
    @Nullable
    private ArrayList<OnScrollListener> mScrollListenerCache;
    @Nullable
    private ArrayList<OnItemTouchListener> mItemOnTouchListenerCache;

    public ReactiveRecyclerView(Context context) {
        this(context, null);
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ReactiveRecyclerView, 0, 0);

        TextView emptyView = new TextView(getContext());
        try {
            emptyView.setText(a.getString(R.styleable.ReactiveRecyclerView_emptyText));
            if (a.hasValue(R.styleable.ReactiveRecyclerView_emptyTextSize)) {
                int textSize = a.getDimensionPixelSize(R.styleable.ReactiveRecyclerView_emptyTextSize,
                        (int) emptyView.getTextSize());
                emptyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            mAutoShowProgress = a.getBoolean(R.styleable.ReactiveRecyclerView_autoShowProgress, true);
            mAutoHideProgress = a.getBoolean(R.styleable.ReactiveRecyclerView_autoHideProgress, true);
        } finally {
            a.recycle();
        }

        setProgressView(new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge));
        setEmptyView(emptyView);

        if (mAutoShowProgress) {
            showProgressView();
        }
    }

    @Override
    public Adapter getAdapter() {
        return mItemAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mItemAdapter = adapter;

        if (mItemAdapter != null) {
            mItemAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    showItemView();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    showItemView();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    showItemView();
                }
            });
        }

        if (mAutoHideProgress) {
            showItemView();
        }
    }

    /**
     * set the view to display as progress (when no adapter has been set yet)
     *
     * @param progressView view to be displayed when no adapter has been asigned yet
     */
    public void setProgressView(@NonNull View progressView) {
        boolean redrawRequired = isLoading();
        mProgressAdapter = new SingleViewAdapter(progressView);

        if (redrawRequired) {
            showProgressView();
        }
    }

    /**
     * @return true, if user has set an adapter, false otherwise
     */
    public boolean hasAdapter() {
        return mItemAdapter != null;
    }

    /**
     * set the view to display when set item adapter does not contain any items
     *
     * @param emptyView view to be displayed when adapter is empty
     */
    public void setEmptyView(@NonNull View emptyView) {
        boolean redrawRequired = isEmptyViewShowing();
        mEmptyAdapter = new SingleViewAdapter(emptyView);
        if (redrawRequired) {
            super.setAdapter(mEmptyAdapter);
        }
    }

    private void showItemView() {
        if (mItemAdapter != null && mItemAdapter.getItemCount() > 0) {
            if (super.getAdapter() != mItemAdapter) {
                showItemDecorations();
                attachOnScrollListeners();
                super.setAdapter(mItemAdapter);
            }
        } else {
            hideItemDecorations();
            detachOnScrollListeners();
            super.setAdapter(mEmptyAdapter);
        }
    }

    private void showProgressView() {
        hideItemDecorations();
        detachOnScrollListeners();
        super.setAdapter(mProgressAdapter);
    }

    @Override
    public void onLoadingStart() {
        if (mAutoShowProgress) {
            setIsLoading(true);
        }
    }

    @Override
    public void onLoadingFinished() {
        if (mAutoHideProgress) {
            setIsLoading(false);
        }
    }

    @Override
    public void setIsLoading(boolean loading) {
        if (loading) {
            showProgressView();
        } else {
            showItemView();
        }
    }

    @Override
    public boolean isLoading() {
        return super.getAdapter() == mProgressAdapter;
    }

    /**
     * @return true if the empty view is showing, false otherwise
     */
    public boolean isEmptyViewShowing() {
        return super.getAdapter() == mEmptyAdapter;
    }

    private boolean isItemViewShowing() {
        return mItemAdapter != null && mItemAdapter == super.getAdapter();
    }

    /**
     * set whether the progress view shall be automatically shown after <code>onLoadingStarted</code>
     * has been invoked
     *
     * @param autoShowProgress
     */
    public void setAutoShowProgress(boolean autoShowProgress) {
        mAutoShowProgress = autoShowProgress;
    }

    /**
     * set whether the progress view shall be hidden after <code>onLoadingFinished</code> has been
     * invoked or an adapter has been set for this view
     *
     * @param autoHideProgress
     */
    public void setAutoHideProgress(boolean autoHideProgress) {
        mAutoHideProgress = autoHideProgress;
    }

    private static class SingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private View view;

        private SingleViewAdapter(View view) {
            this.view = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            // nest inside container to not change the received layout
            final RelativeLayout container = new RelativeLayout(parent.getContext());
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
                ((ViewGroup) view.getParent()).removeView(view);
            }

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            container.addView(view, layoutParams);

            return new RecyclerView.ViewHolder(container) {
            };
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    @Override
    public void addItemDecoration(ItemDecoration decor, int index) {
        if (mItemDecorationCache == null) {
            mItemDecorationCache = new ArrayList<>();
        }

        // cache itemdecorations to hide them while single views are displayed
        if (index < 0) {
            mItemDecorationCache.add(decor);
        } else {
            mItemDecorationCache.add(index, decor);
        }

        if (isItemViewShowing()) {
            // directly add to parent, if itemview is already showing
            super.addItemDecoration(decor, index);
        }
    }

    @Override
    public void removeItemDecoration(ItemDecoration decor) {
        if (mItemDecorationCache != null) {
            mItemDecorationCache.remove(decor);
        }
        super.removeItemDecoration(decor);
    }

    private void showItemDecorations() {
        if (mItemDecorationCache != null) {
            for (ItemDecoration decoration : mItemDecorationCache) {
                super.addItemDecoration(decoration, -1);
            }
        }
    }

    private void hideItemDecorations() {
        if (mItemDecorationCache != null) {
            for (ItemDecoration decoration : mItemDecorationCache) {
                super.removeItemDecoration(decoration);
            }
        }
    }

    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        if (mScrollListenerCache == null) {
            mScrollListenerCache = new ArrayList<>();
        }
        mScrollListenerCache.add(listener);

        if (isItemViewShowing()) {
            // directly add to parent, if itemview is already showing
            super.addOnScrollListener(listener);
        }
    }

    @Override
    public void clearOnScrollListeners() {
        if (mScrollListenerCache != null) {
            mScrollListenerCache.clear();
        }
        super.clearOnScrollListeners();
    }

    @Override
    public void removeOnScrollListener(OnScrollListener listener) {
        if (mScrollListenerCache != null) {
            mScrollListenerCache.remove(listener);
        }
        super.removeOnScrollListener(listener);
    }

    private void attachOnScrollListeners() {
        if (mScrollListenerCache != null) {
            for (OnScrollListener listener : mScrollListenerCache) {
                super.addOnScrollListener(listener);
            }
        }
    }

    private void detachOnScrollListeners() {
        super.clearOnScrollListeners();
    }

    @Override
    public void addOnItemTouchListener(OnItemTouchListener listener) {
        if (mItemOnTouchListenerCache == null) {
            mItemOnTouchListenerCache = new ArrayList<>();
        }
        mItemOnTouchListenerCache.add(listener);

        if (isItemViewShowing()) {
            // directly add to parent, if itemview is already showing
            super.addOnItemTouchListener(listener);
        }
    }

    @Override
    public void removeOnItemTouchListener(OnItemTouchListener listener) {
        if (mItemOnTouchListenerCache != null) {
            mItemOnTouchListenerCache.remove(listener);
        }
        super.removeOnItemTouchListener(listener);
    }

    private void attachOnItemTouchListeners() {
        if (mItemOnTouchListenerCache != null) {
            for (OnItemTouchListener listener : mItemOnTouchListenerCache) {
                super.addOnItemTouchListener(listener);
            }
        }
    }

    private void detachOnItemTouchListeners() {
        if (mItemOnTouchListenerCache != null) {
            for (OnItemTouchListener listener : mItemOnTouchListenerCache) {
                super.removeOnItemTouchListener(listener);
            }
        }
    }
}