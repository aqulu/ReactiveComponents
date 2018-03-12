package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private boolean mHasFixedSize;

    @Nullable
    private ArrayList<ItemDecoration> mItemDecorationCache;
    @Nullable
    private ArrayList<OnScrollListener> mScrollListenerCache;
    @Nullable
    private ArrayList<OnItemTouchListener> mItemOnTouchListenerCache;

    private final AdapterDataObserver mAdapterObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            finishLoading();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            finishLoading();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            finishLoading();
        }
    };

    public ReactiveRecyclerView(Context context) {
        this(context, null);
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ReactiveRecyclerView, 0, 0);

        final String emptyText;
        final Drawable emptyDrawable;
        Integer textSize = null;
        try {
            emptyText = a.getString(R.styleable.ReactiveRecyclerView_emptyText);
            if (a.hasValue(R.styleable.ReactiveRecyclerView_emptyTextSize)) {
                textSize = a.getDimensionPixelSize(R.styleable.ReactiveRecyclerView_emptyTextSize, -1);
            }
            emptyDrawable = a.getDrawable(R.styleable.ReactiveRecyclerView_emptyDrawable);

            mAutoShowProgress = a.getBoolean(R.styleable.ReactiveRecyclerView_autoShowProgress, true);
            mAutoHideProgress = a.getBoolean(R.styleable.ReactiveRecyclerView_autoHideProgress, true);
        } finally {
            a.recycle();
        }

        setProgressView(new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge));
        setEmptyView(buildEmptyView(emptyDrawable, emptyText, textSize));

        if (mAutoShowProgress) {
            showProgressView();
        }
    }

    @Override
    public boolean hasFixedSize() {
        return mHasFixedSize;
    }

    @Override
    public void setHasFixedSize(boolean hasFixedSize) {
        mHasFixedSize = hasFixedSize;
        if (isItemViewShowing()) {
            super.setHasFixedSize(hasFixedSize);
        }
    }

    @Override
    public Adapter getAdapter() {
        return mItemAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        unregisterAdapterObserver();

        mItemAdapter = adapter;
        registerAdapterObserver();

        if (mAutoHideProgress) {
            finishLoading();
        }
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        boolean itemViewShowing = isItemViewShowing();
        unregisterAdapterObserver();

        mItemAdapter = adapter;
        registerAdapterObserver();

        if (itemViewShowing) {
            super.swapAdapter(mItemAdapter, removeAndRecycleExistingViews);
        } else if (mAutoHideProgress) {
            finishLoading();
        }
    }

    /**
     * registers adapter observer to current mItemAdapter
     */
    private void registerAdapterObserver() {
        if (mItemAdapter != null) {
            mItemAdapter.registerAdapterDataObserver(mAdapterObserver);
        }
    }

    /**
     * unregisters adapter observer from current mItemAdapter
     */
    private void unregisterAdapterObserver() {
        if (mItemAdapter != null) {
            mItemAdapter.unregisterAdapterDataObserver(mAdapterObserver);
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

    private View buildEmptyView(@Nullable Drawable drawable, @Nullable String text, @Nullable Integer textSize) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if (drawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageDrawable(drawable);

            linearLayout.addView(imageView);
        }

        if (text != null) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            if (textSize != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            linearLayout.addView(textView);
        }

        return linearLayout;
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

    private void finishLoading() {
        if (mItemAdapter != null && mItemAdapter.getItemCount() > 0) {
            showItemView();
        } else {
            showEmptyView();
        }
    }

    private void showItemView() {
        if (!isItemViewShowing()) {
            showItemDecorations();
            attachOnScrollListeners();
            attachOnItemTouchListeners();
            super.setHasFixedSize(mHasFixedSize);

            super.setAdapter(mItemAdapter);
        }
    }

    private void showEmptyView() {
        showSingleView(mEmptyAdapter);
    }

    private void showProgressView() {
        showSingleView(mProgressAdapter);
    }

    /**
     * hides all item view specific listeners and decorations and sets adapter
     *
     * @param adapter to show
     */
    private void showSingleView(Adapter adapter) {
        hideItemDecorations();
        detachOnScrollListeners();
        detachOnItemTouchListeners();
        super.setHasFixedSize(false);
        super.setAdapter(adapter);
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
            finishLoading();
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

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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