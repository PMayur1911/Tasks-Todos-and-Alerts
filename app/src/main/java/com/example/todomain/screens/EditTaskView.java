package com.example.todomain.screens;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.todomain.R;
import com.example.todomain.pojo.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class EditTaskView extends AppCompatActivity {

    private EditText editTaskTitle, editTaskDesc;
    private TextView editTaskDate, editTaskTime;
    private Button btnEditTaskDate, btnEditTaskTime, btnUpdateTask;

    private Task task;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task_view);

        initViews();
//        extractTaskToEdit();
        setEventListeners();
    }

    private void setEventListeners() {
        btnEditTaskDate.setOnClickListener(v -> showDateDialog(editTaskDate.getText().toString()));

        btnEditTaskTime.setOnClickListener(v -> showTimeDialog(editTaskTime.getText().toString()));

//        btnUpdateTask.setOnClickListener(v -> updateTaskDetails());
    }

    private void updateTaskDetails() {
        task.setTaskTitle(editTaskTitle.getText().toString());
        task.setTaskDesc(editTaskDesc.getText().toString());

        String deadline = editTaskTime.getText().toString() + ", "
                + editTaskDate.getText().toString();
//        task.setTaskDeadline(deadline);

        Intent intent = new Intent(this, MainActivity.class);

        Gson gson = new Gson();
        String taskToEdit = gson.toJson(task);

        intent.putExtra(getString(R.string.updatedTaskKey), taskToEdit);
        intent.putExtra(getString(R.string.updateTaskPositionKey), position);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showTimeDialog(String timeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//        LocalDateTime time = LocalDateTime.from(formatter.parse(timeString));

        LocalTime localTime = LocalTime.parse(timeString);

//        int HOUR = time.getHour();
//        int MIN = time.getMinute();

        int HOUR = localTime.getHour();
        int MIN = localTime.getMinute();

        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (timePickerView, hourOfDay, minute) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    c.set(Calendar.MINUTE, minute);

                    CharSequence cs = DateFormat.format("HH:mm",c);
                    editTaskTime.setText(cs);
                }, HOUR, MIN, is24HourFormat);

        timePickerDialog.show();
    }

    private void showDateDialog(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);

        int YEAR = date.getYear();
        int MONTH = date.getMonthValue();
        int DATE = date.getDayOfMonth();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePickerView, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONDAY, month);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    CharSequence cs = DateFormat.format("MMM d yyyy",c);
                    editTaskDate.setText(cs);
                }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void initViews() {
        btnEditTaskTime = findViewById(R.id.btnEditTaskTime);
        btnEditTaskDate = findViewById(R.id.btnEditTaskDate);
        editTaskTime = findViewById(R.id.editTaskTime);
        editTaskDate = findViewById(R.id.editTaskDate);
        editTaskTitle = findViewById(R.id.editTaskTitle);
        editTaskDesc = findViewById(R.id.editTaskDesc);
//        btnUpdateTask = findViewById(R.id.btnUpdateTask);
    }

//    private void extractTaskToEdit() {
//        Intent intent = getIntent();
//        String taskToEdit = intent.getStringExtra(getString(R.string.editTaskKey));
//        position = intent.getIntExtra(getString(R.string.taskPositionKey), 0);
//
//        Gson gson = new Gson();
//        Type type = new TypeToken<Task>() {}.getType();
//
//        task = gson.fromJson(taskToEdit, type);
//
//        editTaskTitle.setText(task.getTaskTitle());
//        editTaskDesc.setText(task.getTaskDesc());
//
//        String[] deadline = task.getTaskDeadline().split(",");
//        if(deadline.length == 2) {
//            editTaskTime.setText(deadline[0]);
//            editTaskDate.setText(deadline[1].trim());
//        }
//    }
}