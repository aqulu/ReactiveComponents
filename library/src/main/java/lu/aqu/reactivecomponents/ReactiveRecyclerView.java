package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * RecyclerView supporting loading and empty view state
 * the list is considered as loading, if no adapter has been set yet.
 * <p>
 * if an adapter has been set the view will either display the emptyView (if set) or the list itself, if itemCount > 0
 */
public class ReactiveRecyclerView extends RecyclerView implements ReactiveComponent {

    private SingleViewAdapter mEmptyAdapter;
    private SingleViewAdapter mProgressAdapter;

    private boolean mAutoShowProgress;
    private boolean mAutoHideProgress;

    private Adapter mItemAdapter;

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
        mProgressAdapter = new SingleViewAdapter(progressView);
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
        mEmptyAdapter = new SingleViewAdapter(emptyView);
        if (hasAdapter()) {
            showItemView();
        }
    }

    /**
     * shows items from user-set adapter (if adapater was set and count > 0) or
     * otherwise the emptyview associated with this instance
     */
    private void showItemView() {
        if (mItemAdapter != null && mItemAdapter.getItemCount() > 0) {
            super.setAdapter(mItemAdapter);
        } else {
            super.setAdapter(mEmptyAdapter);
        }
    }

    /**
     * shows the progressview (if LayoutManager has been set for this RecyclerView instance)
     */
    private void showProgressView() {
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

    public void setIsLoading(boolean loading) {
        if (loading) {
            showProgressView();
        } else {
            showItemView();
        }
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
            container.setMinimumHeight(parent.getHeight());
            container.setMinimumWidth(parent.getWidth());

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
}