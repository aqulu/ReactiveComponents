package lu.aqu.reactivecomponents;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lu.aqu.reactivecomponents.transformers.ReactiveTransformer;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReactiveRecyclerViewTest {

    @Test
    public void testInitialLoadingState() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));

        assertNull(recyclerView.getAdapter());
        assertTrue(recyclerView.isLoading());
        assertFalse(recyclerView.isEmptyViewShowing());
    }

    @Test
    public void testItemView() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(createAdapter(appContext, 15));

        assertNotNull(recyclerView.getAdapter());
        assertFalse(recyclerView.isLoading());
        assertEquals(15, recyclerView.getAdapter().getItemCount());
    }

    @Test
    public void testEmptyView() throws Exception {
        final Context appContext = InstrumentationRegistry.getTargetContext();

        ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(createAdapter(appContext, 0));

        assertNotNull(recyclerView.getAdapter());
        assertFalse(recyclerView.isLoading());
        assertTrue(recyclerView.isEmptyViewShowing());
    }

    @Test
    public void testToggleLoadingState() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));

        assertTrue(recyclerView.isLoading());
        recyclerView.setIsLoading(false);
        assertFalse(recyclerView.isLoading());
        recyclerView.setIsLoading(true);
        assertTrue(recyclerView.isLoading());
    }

    @Test
    public void testRxSuccess() throws Exception {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));

        Observable.just(Arrays.asList("The", "cake", "is", "a", "lie"))
                .compose(ReactiveTransformer.<List<String>>bind(recyclerView))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        assertFalse(recyclerView.isLoading());
                        assertTrue(recyclerView.isEmptyViewShowing());
                    }
                })
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        assertTrue(recyclerView.isLoading());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        fail();
                    }
                });
    }

    @Test
    public void testRxError() throws Exception {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));

        Observable.<List<String>>error(new RuntimeException("pew pew"))
                .compose(ReactiveTransformer.<List<String>>bind(recyclerView))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        assertFalse(recyclerView.isLoading());
                        assertTrue(recyclerView.isEmptyViewShowing());
                    }
                })
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        fail();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        assertTrue(recyclerView.isLoading());
                    }
                });
    }

    @Test
    public void testRxSuccessAutoHideProgressDisabled() throws Exception {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        final ReactiveRecyclerView recyclerView = new ReactiveRecyclerView(appContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAutoHideProgress(false);

        assertTrue(recyclerView.isLoading());
        assertFalse(recyclerView.isEmptyViewShowing());

        Observable.just(Arrays.asList("The", "cake", "is", "a", "lie"))
                .compose(ReactiveTransformer.<List<String>>bind(recyclerView))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        assertTrue(recyclerView.isLoading());
                        assertFalse(recyclerView.isEmptyViewShowing());
                    }
                })
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception { /* do nothing */ }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception { /* do nothing */ }
                });
    }

    private RecyclerView.Adapter createAdapter(final Context context, final int itemCount) {
        return new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new TextView(context)) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(String.valueOf(position));
            }

            @Override
            public int getItemCount() {
                return itemCount;
            }
        };
    }

}
