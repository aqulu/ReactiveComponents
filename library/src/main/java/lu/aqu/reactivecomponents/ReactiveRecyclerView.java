package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * RecyclerView supporting loading and empty view state
 * the list is considered as loading, if no adapter has been set yet.
 * <p>
 * if an adapter has been set the view will either display the emptyView (if set) or the list itself, if itemCount > 0
 */
public class ReactiveRecyclerView extends RecyclerView {

    private View mEmptyView;
    private View mProgressView;

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

        mProgressView = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);

        TextView emptyView = new TextView(getContext());
        try {
            if (a.getBoolean(R.styleable.ReactiveRecyclerView_autoShowProgress, true)) {
                showProgressView();
            }

            emptyView.setText(a.getString(R.styleable.ReactiveRecyclerView_emptyText));
            if (a.hasValue(R.styleable.ReactiveRecyclerView_emptyTextSize)) {
                int textSize = a.getDimensionPixelSize(R.styleable.ReactiveRecyclerView_emptyTextSize,
                        (int) emptyView.getTextSize());
                emptyView.setTextSize(textSize);
            }
        } finally {
            a.recycle();
        }

        mEmptyView = emptyView;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mItemAdapter = adapter;

        if (mItemAdapter != null) {
            mItemAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    toggleAdapters();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    toggleAdapters();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    toggleAdapters();
                }
            });

            toggleAdapters();
        }
    }

    /**
     * shows the progressview (if LayoutManager has been set for this RecyclerView instance)
     */
    public void showProgressView() {
        super.setAdapter(new SingleViewAdapter(mProgressView));
    }

    /**
     * set the view to display as progress (when no adapter has been set yet)
     *
     * @param progressView view to be displayed when no adapter has been asigned yet
     */
    public void setProgressView(@NonNull View progressView) {
        mProgressView = progressView;
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
        mEmptyView = emptyView;
        if (hasAdapter()) {
            toggleAdapters();
        }
    }

    private void toggleAdapters() {
        if (mItemAdapter != null && mItemAdapter.getItemCount() > 0) {
            super.setAdapter(mItemAdapter);
        } else {
            super.setAdapter(new SingleViewAdapter(mEmptyView));
        }
    }

    private static class SingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private View view;

        private SingleViewAdapter(View view) {
            this.view = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    // set padding as soon as view can be measured to center inside recyclerview
                    int paddingVertical = (parent.getHeight() - view.getHeight()) / 2;
                    int paddingHorizontal = (parent.getWidth() - view.getWidth()) / 2;
                    view.setPadding(paddingHorizontal, paddingVertical,
                            paddingHorizontal, paddingVertical);
                }
            });
            return new RecyclerView.ViewHolder(view) {
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