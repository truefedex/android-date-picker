package com.phlox.datepick;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


public class CalendarNumbersView extends View{
    public static final int MAX_WEEKS_IN_MONTH = 6;
    private float MAX_SELECTION_FINGER_SHIFT_DIST = 5.0f;

    private TextPaint paint;
    private int cellPadding;
    private int textColor;
    private int inactiveTextColor;
    private int selectionTextColor;
    private int cellBackgroundColor;
    private int cellSelectionBackgroundColor;
    private int dayNamesTextColor;
    private int dayNamesBackgroundColor;
    private boolean showDayNames = true;
    private Locale locale = Locale.getDefault();

    private Calendar selectedDate;
    private Calendar shownMonth;

    private DateSelectionListener listener = null;

    //temporary and cache values
    private int _cachedCellSideWidth = 0;
    private int _cachedCellSideHeight = 0;
    private Calendar _calendar = Calendar.getInstance();
    private Rect _rect = new Rect();
    private float _textHeight = 0;
    private float _x;
    private float _y;
    private Typeface _boldTypeface;
    private Typeface _defaultTypeface;

    public interface DateSelectionListener {
        void onDateSelected(Calendar selectedDate);
    }

    public static class CalendarDayCellCoord {
        public int col;
        public int row;
        public CalendarDayCellCoord(int col, int row) {
            this.col = col; this.row = row;
        }
    }

    public CalendarNumbersView(Context context) {
        super(context);
        init(null);
    }

    public CalendarNumbersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CalendarNumbersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

        //setup defaults
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.calendar_default_text_size));
        textColor = getResources().getColor(R.color.calendar_default_text_color);
        inactiveTextColor = getResources().getColor(R.color.calendar_default_inactive_text_color);
        selectionTextColor = getResources().getColor(R.color.calendar_default_selection_text_color);
        cellPadding = getResources().getDimensionPixelSize(R.dimen.calendar_default_cell_padding);
        cellBackgroundColor = getResources().getColor(R.color.calendar_default_cell_background_color);
        cellSelectionBackgroundColor = getResources().getColor(R.color.calendar_default_cell_selection_background_color);
        dayNamesTextColor = getResources().getColor(R.color.calendar_default_day_names_cell_text_color);
        dayNamesBackgroundColor = getResources().getColor(R.color.calendar_default_day_names_cell_background_color);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarNumbersView);
        if (ta != null) {
            paint.setTextSize(ta.getDimensionPixelSize(R.styleable.CalendarNumbersView_fontSize, (int)paint.getTextSize()));
            textColor = ta.getColor(R.styleable.CalendarNumbersView_textColor, textColor);
            inactiveTextColor = ta.getColor(R.styleable.CalendarNumbersView_inactiveTextColor, inactiveTextColor);
            selectionTextColor = ta.getColor(R.styleable.CalendarNumbersView_selectionTextColor, selectionTextColor);
            cellPadding = ta.getDimensionPixelSize(R.styleable.CalendarNumbersView_cellPadding, cellPadding);
            cellBackgroundColor = ta.getColor(R.styleable.CalendarNumbersView_cellBackgroundColor, cellBackgroundColor);
            cellSelectionBackgroundColor = ta.getColor(R.styleable.CalendarNumbersView_cellSelectionBackgroundColor, cellSelectionBackgroundColor);
            dayNamesTextColor = ta.getColor(R.styleable.CalendarNumbersView_cellDayNamesCellTextColor, dayNamesTextColor);
            dayNamesBackgroundColor = ta.getColor(R.styleable.CalendarNumbersView_cellDayNamesCellBackgroundColor, dayNamesBackgroundColor);
        }

        selectedDate = Calendar.getInstance();
        shownMonth = (Calendar) selectedDate.clone();
    }

    public int calculateQuadCellSideWidth() {
        Rect bounds = new Rect();
        String str = "WW";//widest possible cell string
        paint.getTextBounds(str, 0, str.length(), bounds);
        int maxWidth = bounds.width();
        int maxHeight = bounds.height();
        _textHeight = bounds.height();
        return Math.max(maxWidth, maxHeight) + cellPadding * 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int quadCellSideWidth = calculateQuadCellSideWidth();
        int calculatedWidth = quadCellSideWidth * shownMonth.getActualMaximum(Calendar.DAY_OF_WEEK) + getPaddingLeft() + getPaddingRight();
        int calculatedHeight = quadCellSideWidth * MAX_WEEKS_IN_MONTH + getPaddingTop() + getPaddingBottom();
        if (showDayNames) {
            calculatedHeight += quadCellSideWidth;
        }
        int minimumWidth = Math.max(getSuggestedMinimumWidth(), calculatedWidth);
        int minimumHeight = Math.max(getSuggestedMinimumHeight(), calculatedHeight);
        int width = chooseSize(minimumWidth, widthMeasureSpec);
        int height = chooseSize(minimumHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public int chooseSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = size;
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        _cachedCellSideWidth = (w - getPaddingRight() - getPaddingLeft()) / shownMonth.getActualMaximum(Calendar.DAY_OF_WEEK);
        _cachedCellSideHeight = (h - getPaddingTop() - getPaddingBottom()) / MAX_WEEKS_IN_MONTH;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showDayNames) {
            setCalendarToFirstVisibleDay(_calendar);
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            for (int col = 0; col < _calendar.getActualMaximum(Calendar.DAY_OF_WEEK); col++) {
                String name = _calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);;
                drawCell(canvas, -1, col, dayNamesTextColor, dayNamesBackgroundColor, name, true);
                _calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        setCalendarToFirstVisibleDay(_calendar);
        for (int row = 0; row < MAX_WEEKS_IN_MONTH; row++) {
            for (int col = 0; col < _calendar.getActualMaximum(Calendar.DAY_OF_WEEK); col++) {
                int textColor;
                int backgroundColor;
                if (_calendar.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                        _calendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)) {
                    textColor = selectionTextColor;
                    backgroundColor = cellSelectionBackgroundColor;
                } else {
                    if (_calendar.get(Calendar.MONTH) == shownMonth.get(Calendar.MONTH)) {
                        textColor = this.textColor;
                    } else {
                        textColor = inactiveTextColor;
                    }
                    backgroundColor = cellBackgroundColor;
                }

                int day = _calendar.get(Calendar.DAY_OF_MONTH);
                String str = Integer.toString(day);
                drawCell(canvas, row, col, textColor, backgroundColor, str, false);
                _calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    private void drawCell(Canvas canvas, int row, int col, int textColor, int backgroundColor, String str, boolean bold) {
        getRectForCell(col, row, _rect);
        paint.setColor(backgroundColor);
        _rect.inset(cellPadding, cellPadding);
        canvas.drawRect(_rect, paint);
        _rect.inset(-cellPadding, -cellPadding);
        paint.setColor(textColor);
        if (bold) {
            if (_boldTypeface == null) {
                _boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
            }
            paint.setTypeface(_boldTypeface);
        } else {
            if (_defaultTypeface == null) {
                _defaultTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            }
            paint.setTypeface(_defaultTypeface);
        }
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(str,
                _rect.left + _cachedCellSideWidth / 2f,
                _rect.top + _cachedCellSideHeight / 2f + _textHeight / 2f - paint.getFontMetrics().descent / 2,
                paint);
    }

    private void setCalendarToFirstVisibleDay(Calendar calendar) {
        calendar.setTime(shownMonth.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayInWeek = calendar.getFirstDayOfWeek();
        int firstDayOfWeekOfCurrentMonth = calendar.get(Calendar.DAY_OF_WEEK);
        int shift;
        if (firstDayInWeek > firstDayOfWeekOfCurrentMonth) {
            shift = -(firstDayOfWeekOfCurrentMonth + calendar.getActualMaximum(Calendar.DAY_OF_WEEK) - firstDayInWeek);
        } else {
            shift = -(firstDayOfWeekOfCurrentMonth - firstDayInWeek);
        }
        calendar.add(Calendar.DAY_OF_MONTH, shift);
    }

    private void getRectForCell(int col, int row, Rect outRect) {
        if (showDayNames) {
            row++;
        }
        outRect.set(getPaddingLeft() + col * _cachedCellSideWidth,
                getPaddingTop() + row * _cachedCellSideHeight,
                getPaddingLeft() + col * _cachedCellSideWidth + _cachedCellSideWidth,
                getPaddingTop() + row * _cachedCellSideHeight + _cachedCellSideHeight);
    }

    private CalendarDayCellCoord getCellForCoords(float x, float y) {
        if (x < getPaddingLeft() ||
                x >= getWidth() - getPaddingRight() ||
                y < getPaddingTop() ||
                y >= getHeight() - getPaddingBottom()) {
            return null;
        }
        CalendarDayCellCoord coord = new CalendarDayCellCoord(
                (int) (x - getPaddingLeft()) / _cachedCellSideWidth,
                (int) (y - getPaddingTop()) / _cachedCellSideHeight
        );
        if (showDayNames) {
            coord.row--;
            if (coord.row < 0) {
                return null;
            }
        }
        return coord;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                _x = event.getX();
                _y = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float x = event.getX();
                float y = event.getY();
                if (Math.sqrt(Math.pow(x - _x, 2) + Math.pow(y - _y, 2)) <= MAX_SELECTION_FINGER_SHIFT_DIST) {
                    selectDayAt(x, y);
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void selectDayAt(float x, float y) {
        CalendarDayCellCoord cellCoords = getCellForCoords(x, y);
        if (cellCoords == null) {
            return;
        }
        setCalendarToFirstVisibleDay(_calendar);
        _calendar.add(Calendar.DAY_OF_YEAR, cellCoords.col);
        _calendar.add(Calendar.WEEK_OF_MONTH, cellCoords.row);
        selectedDate.setTime(_calendar.getTime());
        if (listener != null) {
            listener.onDateSelected(selectedDate);
        }
        invalidate();
    }

    public int getCellBackgroundColor() {
        return cellBackgroundColor;
    }

    public void setCellBackgroundColor(int cellBackgroundColor) {
        this.cellBackgroundColor = cellBackgroundColor;
        invalidate();
    }

    public int getCellPadding() {
        return cellPadding;
    }

    public void setCellPadding(int cellPadding) {
        this.cellPadding = cellPadding;
        invalidate();
    }

    public int getCellSelectionBackgroundColor() {
        return cellSelectionBackgroundColor;
    }

    public void setCellSelectionBackgroundColor(int cellSelectionBackgroundColor) {
        this.cellSelectionBackgroundColor = cellSelectionBackgroundColor;
        invalidate();
    }

    public int getInactiveTextColor() {
        return inactiveTextColor;
    }

    public void setInactiveTextColor(int inactiveTextColor) {
        this.inactiveTextColor = inactiveTextColor;
        invalidate();
    }

    public DateSelectionListener getListener() {
        return listener;
    }

    public void setListener(DateSelectionListener listener) {
        this.listener = listener;
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Calendar selectedDate) {
        this.selectedDate = selectedDate;
        invalidate();
    }

    public int getSelectionTextColor() {
        return selectionTextColor;
    }

    public void setSelectionTextColor(int selectionTextColor) {
        this.selectionTextColor = selectionTextColor;
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    public Calendar getShownMonth() {
        return shownMonth;
    }

    public void setShownMonth(Calendar shownMonth) {
        this.shownMonth = shownMonth;
        invalidate();
    }

    public boolean isShowDayNames() {
        return showDayNames;
    }

    public void setShowDayNames(boolean showDayNames) {
        this.showDayNames = showDayNames;
        invalidate();
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        invalidate();
    }

    public int getDayNamesBackgroundColor() {
        return dayNamesBackgroundColor;
    }

    public void setDayNamesBackgroundColor(int dayNamesBackgroundColor) {
        this.dayNamesBackgroundColor = dayNamesBackgroundColor;
        invalidate();
    }

    public int getDayNamesTextColor() {
        return dayNamesTextColor;
    }

    public void setDayNamesTextColor(int dayNamesTextColor) {
        this.dayNamesTextColor = dayNamesTextColor;
        invalidate();
    }
}
