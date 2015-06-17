# Scrollable

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Scrollable-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1687)

---

![](https://raw.githubusercontent.com/noties/Scrollable/master/sample.gif)

This library encapsulates scrolling logic when implementing *scrolling tabs* (hot topic nowadays in Android development). It saves us from unpleasant routine aka `addHeader`, `addFooter` for every scrolling content which appears in tabs and synchronising it with our *real* headers. It also works good for a single scrolling widget, for e.g. RecyclerView which has it's own issues with headers (at least *parallaxing* them). Also check out the `ScrollableDialog` in sample application it mimics new Android Lollipop Intent chooser behavior.

[![Maven Central](https://img.shields.io/maven-central/v/ru.noties/scrollable.svg)](http://search.maven.org/#search|ga|1|g%3A%22ru.noties%22%20AND%20a%3A%22scrollable%22)
```groovy
compile 'ru.noties:scrollable:x.x.x'
```


## What's new (1.1.2)
* Bundled with own Scroller implementation to be independent of device's Scroller


### What's new (1.1.1)
* Using ScrollableLayout with Toolbar fix


### What's new (1.1.0)
* Improved interception of ghost touches when `ScrollableLayout.getScrollY()` > 0 && < max scroll y
* Added `Close-up logic` (turned off by default)


## Howto
Simply wrap your views in `ru.noties.scrollable.ScrollableLayout`. The final xml might be looking something like that (don't copy, it's not valid):

```xml
<ru.noties.scrollable.ScrollableLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/scrollable_layout"
    app:scrollable_maxScroll="@dimen/header_height"> --!(1)

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="@dimen/header_height" --!(2)
            android:background="@color/header_background"
            android:textColor="@color/white"
            android:textSize="30sp"
            android:text="Header"
            android:id="@+id/header"
            android:gravity="center"/>

        <ru.noties.scrollable.sample.TabsLayout
            android:layout_width="match_parent"
            android:layout_height="@dimen/tabs_height" --!(3)
            android:background="@color/tabs_background"
            android:id="@+id/tabs" />

    </LinearLayout>

    <android.support.v4.view.ViewPager
        android:layout_width="match_parent"
        android:layout_height="match_parent" --!(4)
        android:layout_marginTop="@dimen/tabs_height" --!(5)
        android:id="@+id/view_pager" />

</ru.noties.scrollable.ScrollableLayout>

```

The most imprortant things here are:

1. `app:scrollable_maxScroll="@dimen/header_height"` - it's the value of header height aka the max scroll distance for a ScrollableLayout
2. It's what will be a header
3. It you wish to create *sticky* behavior to a certain View, you should indicate it's heigth here & in step **5**
4. Out main content should have `match_parent` as it's layout_height attribute
5. Crucial for a *sticky* behavior


Then we should init out logic in code (copied from sample application):
```java
final View header = findViewById(R.id.header);
final TabsLayout tabs = findView(this, R.id.tabs);

mScrollableLayout = findView(this, R.id.scrollable_layout);
mScrollableLayout.setDraggableView(tabs);
final ViewPager viewPager = findView(this, R.id.view_pager);
final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getResources(), getFragments());
viewPager.setAdapter(adapter);

tabs.setViewPager(viewPager);

// Note this bit, it's very important
mScrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
    @Override
    public boolean canScrollVertically(int direction) {
    	return adapter.canScrollVertically(viewPager.getCurrentItem(), direction);
    }
});

mScrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
  @Override
  public void onScrollChanged(int y, int oldY, int maxY) {

	// Sticky behavior
    final float tabsTranslationY;
    if (y < maxY) {
    	tabsTranslationY = .0F;
    } else {
    	tabsTranslationY = y - maxY;
    }

    tabs.setTranslationY(tabsTranslationY);

    header.setTranslationY(y / 2);
  }
});
```

In order to start rolling we should pass to our ScrollableLayout a `ru.noties.scrollable.CanScrollVerticallyDelegate`. For e.g. PagerAdapter implements it what way:

```java
boolean canScrollVertically(int position, int direction) {
	return getItem(position).canScrollVertically(direction);
}
```

And fragments implements it that way:

```java
@Override
public boolean canScrollVertically(int direction) {
	return mView != null && mView.canScrollVertically(direction);
}
```

Additionally you could provide a ScrollableLayout with a `ru.noties.scrollable.OnScrollChangedListener` and implement where your own logic. It's the place to implement parallax and all other possible stuff.

## Close-up logic
Since `1.1.0` it's possible to evaluate custom close-up logic. For a simple close-up (with only two states of ScrollableLayout - collapsed & expanded) use `ru.noties.scrollable.DefaultCloseUpAlgorithm`. For more fancy stuff create your own by implementing `ru.noties.scrollable.CloseUpAlgorithm`.

#### CloseUpAlgorithm
Might be set with `ScrollableLayout.setCloseUpAlgorithm()` or (if default implementation is desired) via xml definition `app:scrollable_defaultCloseUp="true"`

#### CloseUpIdleAnimationTime
`ru.noties.scrollable.CloseUpIdleAnimationTime` is used to compute the duration of the close-up animation. For a simple case when duration for all close-ups is constant `ru.noties.scrollable.SimpleCloseUpIdleAnimationTime` might be used via `ScrollableLayout.setCloseUpIdleAnimationTime()` or via xml definition `app:scrollable_closeUpAnimationMillis="200"`.
Default value is `200` ms.

#### CloseUpConsiderIdleMillis
Value in milliseconds after which scroll state of a ScrollableLayout is considered idle and thus close-up logic is evaluated. Might be set via `ScrollableLayout.setConsiderIdleMillis()` or via xml definition `app:scrollable_considerIdleMillis="100"`.
Default value is `100` ms.

#### CloseUpAnimatorConfigurator
`ru.noties.scrollable.CloseUpAnimatorConfigurator` is used to configure an `ObjectAnimator` of a pending close-up animation. If the only configurable value is an `Interpolator` then `ru.noties.scrollable.InterpolatorCloseUpAnimatorConfigurator` might be used via `ScrollableLayout.setCloseUpAnimatorConfigurator()` or via xml definition `app:scrollable_closeUpAnimatorInterpolator="@android:anim/bounce_interpolator"`.
Default value is `null`

## XML attributes
```xml
    app:scrollable_maxScroll="@dimen/header_height"
    app:scrollable_considerIdleMillis="100"
    app:scrollable_friction="0.075"
    app:scrollable_closeUpAnimationMillis="200"
    app:scrollable_defaultCloseUp="true"
    app:scrollable_scrollerFlywheel="false"
    app:scrollable_closeUpAnimatorInterpolator="@android:anim/bounce_interpolator"
```


## License

```
  Copyright 2015 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```
