package lu.aqu.reactivecomponents;

public interface ReactiveComponent {

    void onLoadingStart();

    void onLoadingFinished();

    void setIsLoading(boolean loading);
}
