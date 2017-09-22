Reactive Components (WIP)
=========================

A collection of components featuring a loading / in progress state. Includes RxJava lifecycle tie-in.

Example usage
-------------

__RecyclerView:__

```xml

<lu.aqu.reactivecomponents.ReactiveRecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/my_recycler_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:scrollbars="vertical"
    app:emptyText="Wow, this looks empty!"
    app:emptyTextSize="24sp"
    app:layoutManager="android.support.v7.widget.LinearLayoutManager" />

```

Tied into Retrofit / RxJava request:

```java

final ReactiveRecyclerView myRecyclerView = (ReactiveRecyclerView) findViewById(R.id.my_recycler_view);
service.getHouses()
	.compose(ReactiveTransformer.<List<House>>bind(myRecyclerView))
	.subscribe(new Consumer<List<House>>() {
	    @Override
	    public void accept(@NonNull List<House> houses) throws Exception {
		Log.i(LOG_TAG, "request succeeded!");
		myRecyclerView.setAdapter(new HouseCardAdapter(houses));
	    }
	}, new Consumer<Throwable>() {
	    @Override
	    public void accept(Throwable throwable) throws Exception {
		Log.e(LOG_TAG, "request failed");
	    }
	});

```

Per default, the recyclerview will switch from loadingstate to displaying the user supplied adapter (or the empty view) as soon as "setAdapter" is invoked.
This behavior can be controlled by setting the XML attribute "app:autoShowProgress" to true. The data can be shown by manually invoking "myRecyclerView.setIsLoading(false)" in this case.

__FloatingActionButton:__


```xml

<lu.aqu.reactivecomponents.ReactiveFloatingActionButton
    android:id="@+id/fab"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:backgroundColor="@color/colorPrimaryDark"
    app:src="@drawable/ic_arrow_forward_white_24dp" />

<lu.aqu.reactivecomponents.ReactiveFloatingActionButton
    android:id="@+id/mini_fab"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:backgroundColor="@color/colorPrimaryDark"
    app:loadingFabSize="mini"
    app:src="@drawable/ic_arrow_forward_white_24dp" />

```

The floating action button's loading state can either manually be controlled by invoking "fab.setIsLoading(true)" or tied into an RxJava / Retrofit request.

Whether or not the button shall be clickable during the loading state can be adjusted with the XML tag "app:clickableWhileLoading". The default value of this settings is "false" (button is not clickable while loading). 


Download
--------

add jitpack to your project's repositories (project level build.gradle file):

```groovy

allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}

```


and add the following to your app level's build.gradle dependencies:

```groovy

dependencies {
  compile 'com.github.aqulu:reactive-components:0.0.2'
}

```

