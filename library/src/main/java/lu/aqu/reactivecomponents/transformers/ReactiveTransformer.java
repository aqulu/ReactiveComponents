package lu.aqu.reactivecomponents.transformers;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lu.aqu.reactivecomponents.ReactiveComponent;

public class ReactiveTransformer {

    public static <T> ObservableTransformer<T, T> bind(@NonNull final ReactiveComponent component) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@io.reactivex.annotations.NonNull Observable<T> upstream) {
                return upstream.doOnSubscribe(new Consumer<Disposable>() {

                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        component.onLoadingStart();
                    }
                }).doOnTerminate(new Action() {

                    @Override
                    public void run() throws Exception {
                        component.onLoadingFinished();
                    }
                });
            }
        };
    }

}
