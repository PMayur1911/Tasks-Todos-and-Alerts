package com.example.todomain.screens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.todomain.R;
import com.example.todomain.itemtouchhelpers.ItemTouchHelperCallback;
import com.example.todomain.misc.NotificationHelper;
import com.example.todomain.misc.TaskAdapter;
import com.example.todomain.pojo.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TaskAdapter taskAdapter;
    private RecyclerView taskListRecyclerView;
    private FloatingActionButton fab;

    private NotificationManagerCompat notificationManagerCompat;
    private int notificationNumber;


    private static final String TAG = "MainActivity";
    private static final String SHARED_PREFS = "SHARED_PREFS";
    private static final String PRIMARY_LIST = "primaryTaskList";
    public static final int UPDATE_TASK = 16;

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        buildRecyclerView();
        setFabAction();
        loadData();

        Log.d(TAG, "onCreate: Complete!");
    }

    /**
     * Saving the list of tasks using shared preferences
     */
    private void saveData(){
        final ArrayList<Task> taskList = taskAdapter.getTaskList();

        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();

        String taskInJson = gson.toJson(taskList);
        editor.putString(PRIMARY_LIST, taskInJson);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String taskInJson = sp.getString(PRIMARY_LIST, null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();

        ArrayList<Task> taskList = gson.fromJson(taskInJson, type);

        if(taskList == null)
            taskList = new ArrayList<>();

        for(Task task: taskList)
            taskAdapter.addTaskToList(task);
    }

    private void buildRecyclerView() {
        // Setup the custom adapter to the recycler view
        taskAdapter = new TaskAdapter(this, MainActivity.this, -1);
        taskListRecyclerView.setAdapter(taskAdapter);
        taskListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // For drag and drop feature on the recycler view
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(taskAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);

        touchHelper.attachToRecyclerView(taskListRecyclerView);
    }

    /**
     * Floating action button - to add a new task
     * Shows the bottom sheet dialog to enter details for a task
     */
    private void setFabAction() {
        fab.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this,
                    R.style.BottomSheetDialogTheme);

            View bottomSheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.bottom_sheet_dialog_task, (ConstraintLayout)findViewById(R.id.bottomSheetDialogTaskListContainer));

            // Save Button
            bottomSheetView.findViewById(R.id.btnSaveTaskList).setOnClickListener(v1 -> {
                String title = ((EditText)bottomSheetView.findViewById(R.id.taskTitle)).getText().toString();
                String detail = ((EditText)bottomSheetView.findViewById(R.id.taskDetail)).getText().toString();
                String deadline = ((TextView)bottomSheetView.findViewById(R.id.taskSchedule)).getText().toString();

                if(!title.equals("")) {
                    taskAdapter.addTaskToList(new Task(title, detail));
                    bottomSheetDialog.dismiss();
//                    notifyNewTask(title, detail);
                }
            });

            // Set Schedule Button
            bottomSheetView.findViewById(R.id.btnSetSchedule).setOnClickListener(v13 -> {
                ConstraintLayout constraintLayout = bottomSheetView.findViewById(R.id.scheduleLayout);
                if(constraintLayout.getVisibility() == View.GONE){
                    ((ConstraintLayout) bottomSheetView.findViewById(R.id.scheduleLayout)).setVisibility(View.VISIBLE);
                    showDateDialog(bottomSheetView);
                }
            });

            // Edit Schedule Button
            bottomSheetView.findViewById(R.id.taskSchedule);
            bottomSheetView.setOnClickListener(v12 -> showDateDialog(bottomSheetView));

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });

    }

    private void showDateDialog(View view) {

        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePickerView, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONDAY, month);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimeDialog(view, c);
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void showTimeDialog(View view, Calendar cd) {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR_OF_DAY);
        int MIN = calendar.get(Calendar.MINUTE);

        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (timePickerView, hourOfDay, minute) -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, cd.get(Calendar.YEAR));
            c.set(Calendar.MONTH, cd.get(Calendar.MONTH));
            c.set(Calendar.DAY_OF_MONTH, cd.get(Calendar.DAY_OF_MONTH));
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);


            String format = (is24HourFormat)?
                    ("HH:mm, MMM d yyyy"):("hh:mm A, MMM d yyyy");
            CharSequence cs = DateFormat.format(format, c);
            ((TextView)view.findViewById(R.id.taskSchedule)).setText(cs);
        }, HOUR, MIN, is24HourFormat);

        timePickerDialog.show();
    }

    private void initViews() {
        taskListRecyclerView = findViewById(R.id.tasks_list);
        fab = findViewById(R.id.addNewTask);
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationNumber = 1;
    }

    private void notifyNewTask(String title, String desc){
        Notification notification = new NotificationCompat.Builder(this, NotificationHelper.notifyNewTaskID)
                .setSmallIcon(R.drawable.ic_notification_new_task)
                .setContentTitle(title + " created!")
                .setContentText(desc)
                .setLights(Color.argb(255,255,0,0), 1500, 1000)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build();

        notificationManagerCompat.notify(notificationNumber++, notification);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == UPDATE_TASK && resultCode == RESULT_OK){
            if(data != null) {
                String taskToUpdate = data.getStringExtra(getString(R.string.updatedTaskKey));
                int position = data.getIntExtra(getString(R.string.updateTaskPositionKey), 0);

                Gson gson = new Gson();
                Type type = new TypeToken<Task>() {
                }.getType();

                Task task = gson.fromJson(taskToUpdate, type);
                taskAdapter.updateTask(task, position);
            }
        }
    }
}