package lu.aqu.reactivecomponents;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

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
public class ReactiveFloatingActionButtonTest {

    @Test
    public void testLoadingState() throws Exception {
        Context appContext = getContext();

        ReactiveFloatingActionButton fab = new ReactiveFloatingActionButton(appContext);
        assertFalse(fab.isLoading());

        fab.setIsLoading(true);
        assertTrue(fab.isLoading());
        assertFalse(fab.isClickable());

        fab.setIsLoading(false);
        assertFalse(fab.isLoading());
        assertTrue(fab.isClickable());
    }

    @Test
    @UiThreadTest
    public void testDisabledWhileLoading() {
        Context appContext = getContext();

        final ReactiveFloatingActionButton fab = new ReactiveFloatingActionButton(appContext);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                fab.setDisabledWhileLoading(true);
                assertTrue(fab.isEnabled());

                fab.setIsLoading(true);
                assertTrue(fab.isLoading());
                assertFalse(fab.isEnabled());
            }
        });
    }

    @Test
    public void testClickableWhileLoading() {
        Context appContext = getContext();

        ReactiveFloatingActionButton fab = new ReactiveFloatingActionButton(appContext);
        fab.setClickableWhileLoading(true);

        fab.setIsLoading(true);
        assertTrue(fab.isLoading());
        assertTrue(fab.isClickable());
    }

    @Test
    public void testRxSuccess() throws Exception {
        Context appContext = getContext();

        final ReactiveFloatingActionButton fab = new ReactiveFloatingActionButton(appContext);

        Observable.just(Arrays.asList("The", "cake", "is", "a", "lie"))
                .compose(ReactiveTransformer.<List<String>>bind(fab))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        assertFalse(fab.isLoading());
                    }
                })
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        assertTrue(fab.isLoading());
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
        Context appContext = getContext();

        final ReactiveFloatingActionButton fab = new ReactiveFloatingActionButton(appContext);
        Observable.just(Arrays.asList("The", "cake", "is", "a", "lie"))
                .compose(ReactiveTransformer.<List<String>>bind(fab))
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        assertFalse(fab.isLoading());
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
                        assertTrue(fab.isLoading());
                    }
                });
    }

    private Context getContext() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.setTheme(R.style.Theme_AppCompat);
        return appContext;
    }
}
