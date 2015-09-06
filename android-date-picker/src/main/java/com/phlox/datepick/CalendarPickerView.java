package com.phlox.datepick;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarPickerView extends FrameLayout{
    private CalendarNumbersView calendar;
    private TextView tvCalendarCaption;
    private ImageView ivPrevMonth;
    private ImageView ivNextMonth;

    public CalendarPickerView(Context context) {
        super(context);
        init();
    }

    public CalendarPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_date_time_picker, this);
        tvCalendarCaption = (TextView) findViewById(R.id.tvCalendarCaption);
        calendar = (CalendarNumbersView) findViewById(R.id.calendar);
        ivPrevMonth = (ImageView) findViewById(R.id.ivPrevMonth);
        ivNextMonth = (ImageView) findViewById(R.id.ivNextMonth);

        ivPrevMonth.setOnClickListener(onPrevMonthClickListener);
        ivNextMonth.setOnClickListener(onNextMonthClickListener);

        updateCaption();
    }

    public void setListener(CalendarNumbersView.DateSelectionListener listener) {
        calendar.setListener(listener);
    }

    public CalendarNumbersView.DateSelectionListener getListener() {
        return calendar.getListener();
    }

    private void updateCaption() {
        SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvCalendarCaption.setText(format.format(calendar.getShownMonth().getTime()));
    }

    public CalendarNumbersView getCalendar() {
        return calendar;
    }

    private OnClickListener onPrevMonthClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar prevMonth = calendar.getShownMonth();
            prevMonth.add(Calendar.MONTH, -1);
            calendar.setShownMonth(prevMonth);
            updateCaption();
        }
    };

    private OnClickListener onNextMonthClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar nextMonth = calendar.getShownMonth();
            nextMonth.add(Calendar.MONTH, 1);
            calendar.setShownMonth(nextMonth);
            updateCaption();
        }
    };
}
