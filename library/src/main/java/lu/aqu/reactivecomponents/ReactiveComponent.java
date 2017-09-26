package lu.aqu.reactivecomponents;

public interface ReactiveComponent {

    /**
     * called on start of loading action
     */
    void onLoadingStart();

    /**
     * called on end of loading action
     */
    void onLoadingFinished();

    /**
     * changes the component's state accordint to parameter <code>loading</code>
     *
     * @param loading whether the component's loading state should be displayed or not
     */
    void setIsLoading(boolean loading);

    /**
     * @return true if the component is showing a loading state, false otherwise
     */
    boolean isLoading();
}
