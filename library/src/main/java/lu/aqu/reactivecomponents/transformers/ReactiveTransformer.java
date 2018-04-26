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

    /**
     * Binds a reactive component to an RxJava Observables lifecycle.<br>
     * The component will be set to loading in the Observables <code>onSubscribe</code> and
     * will finish loading in <code>doOnTerminate</code>.
     *
     * @param component to be bound to the Observable lifecycle
     * @param <T>       return type of the Observable
     * @return ObservableTransformer, which changes the ReactiveComponents loading state
     */
    public static <T> ObservableTransformer<T, T> bind(@NonNull final ReactiveComponent component) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@io.reactivex.annotations.NonNull Observable<T> upstream) {
                return upstream.doOnSubscribe(new Consumer<Disposable>() {

                    @Override
                    public void accept(@NonNull Disposable disposable) {
                        component.onLoadingStart();
                    }
                }).doOnTerminate(new Action() {

                    @Override
                    public void run() {
                        component.onLoadingFinished();
                    }
                });
            }
        };
    }

}
