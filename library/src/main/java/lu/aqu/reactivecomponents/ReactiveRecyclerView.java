package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.TypedArray;
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

    private Adapter mItemAdapter;
    private SingleViewAdapter mEmptyAdapter;

    public ReactiveRecyclerView(Context context) {
        this(context, null);
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReactiveRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ReactiveRecyclerView, 0, 0);

        String emptyString;
        try {
            emptyString = a.getString(R.styleable.ReactiveRecyclerView_emptyText);
        } finally {
            a.recycle();
        }

        TextView emptyView = new TextView(getContext());
        emptyView.setText(emptyString);
        mEmptyAdapter = new SingleViewAdapter(emptyView);

        ProgressBar progressBar = new ProgressBar(getContext(), null,
                android.R.attr.progressBarStyleLarge);
        super.setAdapter(new SingleViewAdapter(progressBar));
    }

    private void toggleAdapters() {
        if (mItemAdapter != null && mItemAdapter.getItemCount() > 0) {
            super.setAdapter(mItemAdapter);
        } else {
            super.setAdapter(mEmptyAdapter);
        }
    }

    @Override
    public void setAdapter(final Adapter adapter) {
        this.mItemAdapter = adapter;

        if (adapter != null) {
            adapter.registerAdapterDataObserver(new AdapterDataObserver() {
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