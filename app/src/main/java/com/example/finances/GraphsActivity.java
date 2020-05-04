package com.example.finances;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;

public class GraphsActivity extends AppCompatActivity {

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        // получение месяца с 1 актвити в заголовок
        Intent intent = getIntent();
        int current_month = intent.getIntExtra("current_month", 0);

        // установка данного месяца
        final String[] monthNames = { "Декабрь", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь" };
        TextView textView = findViewById(R.id.textView);
        textView.setText(monthNames[current_month]);

        // создаем массив координат для графика
        Calendar myCalendar = (Calendar) Calendar.getInstance().clone();
        int max_date = myCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int[][] array = new int[max_date][2];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][0] = i+1;
                array[i][1] = 0;
            }
        }

        // подключаем базу данных, где хранятся доходы
        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);
        if (cursor.moveToLast()) {
            int dateColIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
            int incomeColIndex = cursor.getColumnIndex(DBHelper.KEY_INCOME);
            do {
                for(int i = 0; i<array.length; i++){
                    // проверка перед добавлением в массив
                    if(array[i][0] == Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[0])  && Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[1]) == current_month){
                        array[i][1] += Integer.parseInt(cursor.getString(incomeColIndex));
                    }
                }
            } while (cursor.moveToPrevious());
        }
        cursor.close();

        // создаем график используя массив координат
        GraphView graph = findViewById(R.id.graph);
        DataPoint[] dataPoint = new DataPoint[max_date];
        for (int i = 0; i < array.length; i++) {
            if(array[i][1]!=0){
                dataPoint[i] = new DataPoint(array[i][0], array[i][1]);
            }
            else{
                dataPoint[i] = new DataPoint(array[i][0], 0f);
            }
            }
        // настройка оторбражения графика
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(4);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 10}, 250));
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(10) );
        paint.setAntiAlias(true);
        

        series.setCustomPaint(paint);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graph.getViewport().setScrollable(true);  // activate horizontal scrolling
        graph.addSeries(series);
    }
}
