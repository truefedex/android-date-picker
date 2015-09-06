# android-date-picker
Simple lightweight Calendar Picker view and Calendar Numbers view for Android. Supports API levels >= 10.

CalendarPickerView:

<img src="/images/img.png" alt="Demo" width="300px" />

CalendarNumbersView (with custom colors):

<img src="/images/img2.png" alt="Demo" width="300px" />

[![Build Status](https://travis-ci.org/truefedex/android-date-picker.svg?branch=master)](https://travis-ci.org/truefedex/android-date-picker)

Usage
-----

1. Add `compile 'com.phlox.widget:android-date-picker:0.2.1'` to your gradle dependencies.
2. Add `CalendarPickerView` into your layouts or view hierarchy.
3. Set a `DateSelectionListener` and wait for events.

Example:

```xml
<com.phlox.datepick.CalendarPickerView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:id="@+id/view" />
```
or
```xml
<com.phlox.datepick.CalendarNumbersView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:id="@+id/view"
    android:background="#000"
    app:fontSize="20sp"
    app:textColor="#fff"
    app:cellBackgroundColor="#0"
    app:cellSelectionBackgroundColor="#958"
    app:selectionTextColor="#000"
    app:cellDayNamesCellTextColor="#fff"
    app:cellDayNamesCellBackgroundColor="#111"
    app:cellPadding="5dp"
    app:inactiveTextColor="#888"/>
```
