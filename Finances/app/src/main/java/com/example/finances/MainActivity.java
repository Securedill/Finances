package com.example.finances;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ListView;

import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kal.rackmonthpicker.RackMonthPicker;
import com.kal.rackmonthpicker.listener.DateMonthDialogListener;
import com.kal.rackmonthpicker.listener.OnCancelMonthDialogListener;

import java.util.ArrayList;
import java.util.Calendar;



public class MainActivity extends AppCompatActivity {

    final Context context = this;
    private ArrayList<State> states = new ArrayList<>();
    ListView countriesList;
    DBHelper dbHelper;
    public  int current_month;
    int current_year;
    int whole_sum = 0;
    SharedPreferences target;
    final String SAVED_TEXT = "saved_text";




    @SuppressLint({"SetTextI18n", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton button_calendar = findViewById(R.id.button_calendar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);

        countriesList = findViewById(R.id.countriesList);
        final StateAdapter stateAdapter = new StateAdapter(this, R.layout.item, states);
        countriesList.setAdapter(stateAdapter);
        countriesList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        dbHelper = new DBHelper(this);
        final Calendar calendar = Calendar.getInstance();

        // создание массива со всеми месяцами
        final String[] monthNames = { "Декабрь", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь",  "Декабрь" };
        // отображение цели: получение переменной с SharedPreferences
        target = getPreferences(MODE_PRIVATE);
        TextView goalAmount  = findViewById(R.id.goal_amount);
        if (target.getString(SAVED_TEXT, "").equals("")) goalAmount.setText("Цель -" + " Сумма не задана"); else {
            goalAmount.setText("Цель - " + target.getString(SAVED_TEXT, "") + " руб");
            TextView left_to_save = findViewById(R.id.left_to_save);
            left_to_save.setText("Осталось накопить - " + (Math.max(Integer.parseInt(target.getString(SAVED_TEXT, "")) - whole_sum, 0)) +  " руб");
        }
        // получение текущего месяца и года
        final TextView text_month = findViewById(R.id.text_month);
        current_month = calendar.get(Calendar.MONTH)+1;
        current_year = calendar.get(Calendar.YEAR);
        text_month.setText(monthNames[current_month]);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // подключение БД и отображение цели, и остатка к её достижению и всех добавлений при открытии приложения
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);

        if (cursor.moveToLast()) {
            int dateColIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
            int incomeColIndex = cursor.getColumnIndex(DBHelper.KEY_INCOME);
            whole_sum = 0;
            do {
                if(Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[1]) ==  calendar.get(Calendar.MONTH)+1 && Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[2]) ==  calendar.get(Calendar.YEAR))
                {states.add(new State(cursor.getString(dateColIndex),"+ " + cursor.getString(incomeColIndex) + " руб"));whole_sum += Integer.parseInt(cursor.getString(incomeColIndex));
                }

            } while (cursor.moveToPrevious());
        } else
            Log.d("mLog", "0 rows");
        TextView left_to_save = findViewById(R.id.left_to_save);
        left_to_save.setText("Осталось накопить - " + (Math.max(Integer.parseInt(target.getString(SAVED_TEXT, "0")) - whole_sum, 0)) +  " руб");
        cursor.close();

        // установление отметки на прогрессбаре
        progressBar.setProgress((int) (((float)whole_sum/Integer.parseInt(target.getString(SAVED_TEXT, "1"))*100)));

        // обработчик при нажатии на календарь для выбора месяца
        button_calendar.setOnClickListener(new View.OnClickListener()   {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(View v)  {
                new RackMonthPicker(MainActivity.this)
                        .setPositiveButton(new DateMonthDialogListener() {
                            @Override
                            public void onDateMonth(int month1, int startDate, int endDate, int year, String monthLabel) {
                                SQLiteDatabase database = dbHelper.getWritableDatabase();
                                current_month = month1;
                                current_year = year;
                                text_month.setText(monthNames[month1]);

                                // подключение БД и отображение данных за выбранный месяц
                                Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);
                                states.clear();
                                stateAdapter.notifyDataSetChanged();
                                whole_sum = 0;
                                if (cursor.moveToLast()) {
                                    int dateColIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
                                    int incomeColIndex = cursor.getColumnIndex(DBHelper.KEY_INCOME);
                                    do {
                                        if(Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[1]) ==  current_month && Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[2]) ==  current_year)
                                        {states.add(new State(cursor.getString(dateColIndex),"+ " + cursor.getString(incomeColIndex) + " руб"));
                                            whole_sum += Integer.parseInt(cursor.getString(incomeColIndex)); }
                                    } while (cursor.moveToPrevious());
                                } else
                                    Log.d("mLog", "0 rows");
                                TextView left_to_save = findViewById(R.id.left_to_save);
                                left_to_save.setText("Осталось накопить - " + (Math.max(Integer.parseInt(target.getString(SAVED_TEXT, "0")) - whole_sum, 0)) +  " руб");
                                cursor.close();

                                // установка отметки на прогрессбаре
                                progressBar.setProgress((int) (((float)whole_sum/Integer.parseInt(target.getString(SAVED_TEXT, "1"))*100)));
                            }
                        })
                        .setNegativeButton(new OnCancelMonthDialogListener() {
                            @Override
                            public void onCancel(AlertDialog dialog) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        // обработчик нажатия на кнопку добавления дохода
        final FloatingActionButton add_expense = findViewById(R.id.add_expense);
        add_expense.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            public void onClick(View arg0) {
                add_expense.hide();
                LayoutInflater li = LayoutInflater.from(context);
                @SuppressLint("InflateParams") View SumView = li.inflate(R.layout.dialog_add, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(SumView);
                final EditText user_add = SumView.findViewById(R.id.input_text_sum);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Добавить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        // подключение к БД
                                        ContentValues contentValues = new ContentValues();
                                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(user_add.getWindowToken(), 0);

                                        // добавлние данных о доходе в БД
                                        if (user_add.getText().toString().equals("")) user_add.setText("0");
                                        contentValues.put(DBHelper.KEY_INCOME, Integer.parseInt(user_add.getText().toString()));
                                        contentValues.put(DBHelper.KEY_DATE, calendar.get(Calendar.DAY_OF_MONTH)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.YEAR));
                                        database.insert(DBHelper.TABLE_CONTACTS, null, contentValues);
                                        states.clear();
                                        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);

                                        // вывод данных о всех доходах за опредлённый месяц из БД
                                        if (cursor.moveToLast()) {
                                            int dateColIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
                                            int incomeColIndex = cursor.getColumnIndex(DBHelper.KEY_INCOME);
                                            whole_sum = 0;
                                            do {
                                                if(Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[1]) ==  current_month && Integer.parseInt(cursor.getString(dateColIndex).split("\\.")[2]) ==  current_year)
                                                {states.add(new State(cursor.getString(dateColIndex),"+ " + cursor.getString(incomeColIndex) + " руб"));
                                                    whole_sum += Integer.parseInt(cursor.getString(incomeColIndex)); }
                                            } while (cursor.moveToPrevious());
                                        } else
                                            Log.d("mLog", "0 rows");
                                        TextView left_to_save = findViewById(R.id.left_to_save);
                                        left_to_save.setText("Осталось накопить - " + (Math.max(Integer.parseInt(target.getString(SAVED_TEXT, "0")) - whole_sum, 0)) +  " руб");
                                        cursor.close();
                                        progressBar.setProgress((int) (((float)whole_sum/Integer.parseInt(target.getString(SAVED_TEXT, "1"))*100)));
                                        add_expense.show();

                                    }
                                })
                        .setNegativeButton("Отменить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(user_add.getWindowToken(), 0);
                                        dialog.cancel();
                                        add_expense.show();
                                    }
                                });
                // настройка окна ввода дохода и открытие его
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY);
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.DKGRAY);
                    }
                });
                alertDialog.show();
                user_add.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            }
        });

        ImageButton edit_button = findViewById(R.id.edit_button);

        // обработчик нажатия на кнопку изменения цели
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                @SuppressLint("InflateParams") View SumView = li.inflate(R.layout.dialog_edit, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(SumView);
                final EditText user_add = SumView.findViewById(R.id.input_text_sum);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @SuppressLint("SetTextI18n")
                                    public void onClick(DialogInterface dialog, int id) {

                                        // отображение окна для ввода цели и её считывания
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(user_add.getWindowToken(), 0);
                                        target = getPreferences(MODE_PRIVATE);
                                        SharedPreferences.Editor ed = target.edit();
                                        ed.putString(SAVED_TEXT, user_add.getText().toString());
                                        ed.apply();
                                        TextView goalAmount  = findViewById(R.id.goal_amount);

                                        // установка цели в TextView
                                        goalAmount.setText("Цель - " + user_add.getText()+ " руб");
                                        TextView left_to_save = findViewById(R.id.left_to_save);
                                        left_to_save.setText("Осталось накопить - " + (Math.max(Integer.parseInt(target.getString(SAVED_TEXT, "0")) - whole_sum, 0)) +  " руб");
                                        progressBar.setProgress((int) (((float)whole_sum/Integer.parseInt(target.getString(SAVED_TEXT, "1"))*100)));
                                    }
                                })
                        .setNegativeButton("Отменить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        assert imm != null;
                                        imm.hideSoftInputFromWindow(user_add.getWindowToken(), 0);
                                        dialog.cancel();
                                    }
                                });
                // настройка окна ввода цели и открытие его
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY);
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.DKGRAY);
                    }
                });
                alertDialog.show();
                user_add.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        // открытие нового активити при нажатии на кнопку графика
        ImageButton button_graph = findViewById(R.id.button_graph);
        button_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphsActivity.class);
                intent.putExtra("current_month", current_month);
                startActivity(intent);
            }
        });

    }

}

