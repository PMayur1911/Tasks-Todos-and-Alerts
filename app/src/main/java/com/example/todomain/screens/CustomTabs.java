package com.example.todomain.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.todomain.R;
import com.example.todomain.misc.AlarmReceiver;
import com.example.todomain.misc.TaskAdapter;
import com.example.todomain.misc.TaskAdapterListener;
import com.example.todomain.pojo.Task;
import com.example.todomain.pojo.TaskList;
import com.example.todomain.tabbedfragments.DoingFragment;
import com.example.todomain.tabbedfragments.DoneFragment;
import com.example.todomain.tabbedfragments.FragmentAdapter;
import com.example.todomain.tabbedfragments.TodoFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CustomTabs extends AppCompatActivity
        implements TaskAdapterListener {

    private static final String TAG = "RBRCustomTabs";
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MaterialToolbar toolbar;

    private FragmentAdapter fAdapter;
    private TodoFragment todoFragment;
    private DoingFragment doingFragment;
    private DoneFragment doneFragment;

    private AlarmManager alarmManager;

    private int TASK_LIST_POSITION;
    private TaskList taskList;
    public static final String RETURN_INTENT_KEY_TASK_LIST = "returnTaskList";
    public static final String RETURN_INTENT_KEY_TASK_LIST_POSITION = "returnTaskListPosition";

    public static final int TODO = 0;
    public static final int DOING = 1;
    public static final int DONE = 2;


    @Override
    protected void onPause() {
        saveTasks();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tabs);

        initViews();
        getTaskList();
        createTabs();
    }

    /**
     * Get 3 arraylists of tasks - to-do, doing,
     * done for the current task list header
     * from intent extras from the previous activity
     */
    private void getTaskList() {
        Intent intent = getIntent();
        String taskListString = intent.getStringExtra(TaskListCategory.INTENT_KEY_TASK_LIST);
        TASK_LIST_POSITION = intent.getIntExtra(TaskListCategory.INTENT_KEY_TASK_LIST_POSITION, 0);

        Gson gson = new Gson();
        Type type = new TypeToken<TaskList>() {}.getType();

        taskList = gson.fromJson(taskListString, type);
        toolbar.setTitle(taskList.getTaskListName());
    }

    private void createTabs() {
        FragmentManager fm = getSupportFragmentManager();
        fAdapter = new FragmentAdapter(fm, getLifecycle());

        todoFragment = TodoFragment.newInstance(taskList.getTodoList());
        doingFragment = DoingFragment.newInstance(taskList.getDoingList());
        doneFragment = DoneFragment.newInstance(taskList.getDoneList());

        fAdapter.addFragment(todoFragment);
        fAdapter.addFragment(doingFragment);
        fAdapter.addFragment(doneFragment);

        viewPager2.setAdapter(fAdapter);

        tabLayout.addTab(tabLayout.newTab().setText("To-do"));
        tabLayout.addTab(tabLayout.newTab().setText("Doing"));
        tabLayout.addTab(tabLayout.newTab().setText("Done"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolBar);
        tabLayout = findViewById(R.id.tabLayout1);
        viewPager2 = findViewById(R.id.viewPager2);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> saveTasks());

        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    }

    /******************************** Task Adapter Listener ********************************/

    /**
     * <pre>
     * Transfer task to the destination fragment
     * 1. Switch the tab to the task's destination category
     * 2. Get instance of the fragment of the destination category
     * 3. Call the addNewTaskToList method of the fragment in a new handler
     *         If not done, this causes NPE as the layout might not be inflated and the adapter in
     *         the fragment might be null. Run this snippet in a new handler
     *         (works without delay as well)
     * </pre>
     *
     * @param task             - Task to transfer between progress categories
     * @param destFragPosition - Destination fragment position (To-do: 0, Doing: 1, Done: 2)
     */
    @Override
    public void transferTask(Task task, int destFragPosition) {
        Log.d(TAG, "transferTask: \nTask: " + task.toString() + "\nDestination Fragment - " + destFragPosition);
        viewPager2.setCurrentItem(destFragPosition, true);

        switch (destFragPosition) {
            case 0:
                new Handler(Looper.myLooper()).post(() -> (
                        (TodoFragment) fAdapter.getFragment(destFragPosition))
                        .addNewTaskToList(task));
                break;
            case 1:
                new Handler(Looper.myLooper()).post(() -> (
                        (DoingFragment) fAdapter.getFragment(destFragPosition))
                        .addNewTaskToList(task));
                break;
            case 2:
                new Handler(Looper.myLooper()).post(() -> (
                        (DoneFragment) fAdapter.getFragment(destFragPosition))
                        .addNewTaskToList(task));
                break;
        }
    }

    @Override
    public void displayEditDialog(TaskAdapter taskAdapter, Task task, int pos) {
        // Inflate the custom edit task layout
        LayoutInflater inflater = this.getLayoutInflater();
        View editTaskView = inflater.inflate(R.layout.activity_edit_task_view, null);

        // Set the layout fields with current task details
        ((EditText)editTaskView.findViewById(R.id.editTaskTitle)).setText(task.getTaskTitle());
        ((EditText)editTaskView.findViewById(R.id.editTaskDesc)).setText(task.getTaskDesc());
        ((SwitchMaterial)editTaskView.findViewById(R.id.notifySwitch)).setChecked(task.isSoundAlarm());
        if(task.isSoundAlarm()){
            editTaskView.findViewById(R.id.alarmOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AEEA00")));
            editTaskView.findViewById(R.id.notifyOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        }
        else{
            editTaskView.findViewById(R.id.alarmOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            editTaskView.findViewById(R.id.notifyOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AEEA00")));
        }

        TextView editTaskTime = ((TextView)editTaskView.findViewById(R.id.editTaskTime));
        TextView editTaskDate = ((TextView)editTaskView.findViewById(R.id.editTaskDate));

        editTaskDate.setText(task.getTaskScheduleString(Task.DATE));
        editTaskTime.setText(task.getTaskScheduleString(Task.TIME));

        // Set event listeners for date and time buttons
        editTaskView.findViewById(R.id.btnEditTaskDate).setOnClickListener(v -> showDateDialog(editTaskDate));
        editTaskView.findViewById(R.id.btnEditTaskTime).setOnClickListener(v -> showTimeDialog(editTaskTime));

        ((SwitchMaterial) editTaskView.findViewById(R.id.notifySwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                editTaskView.findViewById(R.id.alarmOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AEEA00")));
                editTaskView.findViewById(R.id.notifyOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            }
            else{
                editTaskView.findViewById(R.id.alarmOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                editTaskView.findViewById(R.id.notifyOption).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AEEA00")));
            }
        });
        editTaskView.findViewById(R.id.btnRemoveTime).setOnClickListener(v -> {
            editTaskDate.setText("");
            editTaskTime.setText("");
            cancelAlarm(task);
        });



        // Display the edit task dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Task")
                .setView(editTaskView)
                .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = ((EditText)editTaskView.findViewById(R.id.editTaskTitle)).getText().toString();
                        if(!title.equals("")) {
                            String desc = ((EditText) editTaskView.findViewById(R.id.editTaskDesc)).getText().toString();
                            boolean option = ((SwitchMaterial)editTaskView.findViewById(R.id.notifySwitch)).isChecked();

                            Calendar c = task.getTaskSchedule();

                            if(!editTaskTime.getText().toString().equals("")){
                                c = (c == null) ? new GregorianCalendar() : c;

                                LocalTime localTime = LocalTime.parse(editTaskTime.getText().toString());
                                c.set(Calendar.HOUR, localTime.getHour());
                                c.set(Calendar.MINUTE, localTime.getMinute());

                                if(!editTaskDate.getText().toString().equals("")){
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy");
                                    LocalDate localDate = LocalDate.parse(editTaskDate.getText().toString(), formatter);
                                    c.set(Calendar.YEAR, localDate.getYear());
                                    c.set(Calendar.MONTH, localDate.getMonthValue());
                                    c.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());

                                    // Set/ Update Alarm Here
                                    if(option) {
                                        if(task.isSoundAlarm())
                                            alarmManager.cancel(task.getPendingIntentAlarm());

                                        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                                        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext()
                                                , 0, intent, 0);
                                        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pIntent);

                                        task.setPendingIntentAlarm(pIntent);
                                    }
                                    else{
                                        alarmManager.cancel(task.getPendingIntentAlarm());
                                    }

                                    // Set notification either-way
                                }
                                else
                                    c = null;
                            }
                            else{
                                if(c != null){
                                    alarmManager.cancel(task.getPendingIntentAlarm());
                                    task.setPendingIntentAlarm(null);
                                }
                                c = null;
                            }

                            task.setTaskTitle(title);
                            task.setTaskDesc(desc);
                            task.setTaskSchedule(c);
                            task.setSoundAlarm(option);

                            taskAdapter.updateTask(task, pos);
                            dialog.dismiss();
                        }
                    }
                });

        builder.create().show();
    }

    private void cancelAlarm(Task task) {
        task.setTaskSchedule(null);
        task.setSoundAlarm(false);

    }

    private void showTimeDialog(TextView view) {
        String timeString = view.getText().toString();

        int HOUR, MIN;

        if(timeString.equals("")){
            Calendar calendar = Calendar.getInstance();
            HOUR = calendar.get(Calendar.HOUR_OF_DAY);
            MIN = calendar.get(Calendar.MINUTE);
        }
        else{
            LocalTime localTime = LocalTime.parse(view.getText().toString());
            HOUR = localTime.getHour();
            MIN = localTime.getMinute();
        }

        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (timePickerView, hourOfDay, minute) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    c.set(Calendar.MINUTE, minute);

                    CharSequence cs = DateFormat.format("HH:mm",c);
                    view.setText(cs);
                }, HOUR, MIN, is24HourFormat);

        timePickerDialog.show();
    }

    private void showDateDialog(TextView view) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy");
        String dateString = view.getText().toString();

        int YEAR, MONTH, DATE;

        if(dateString.equals("")){
            Calendar calendar = Calendar.getInstance();
            YEAR = calendar.get(Calendar.YEAR);
            MONTH = calendar.get(Calendar.MONTH);
            DATE = calendar.get(Calendar.DAY_OF_MONTH);
        }
        else{
            LocalDate date = LocalDate.parse(view.getText().toString(), formatter);
            YEAR = date.getYear();
            MONTH = date.getMonthValue();
            DATE = date.getDayOfMonth();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePickerView, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONDAY, month);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    CharSequence cs = DateFormat.format("MMM d yyyy",c);
                    ((TextView)view).setText(cs);
                }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void saveTasks() {
        taskList.setTodoList(todoFragment.getTasks());
        taskList.setDoingList(doingFragment.getTasks());
        taskList.setDoneList(doneFragment.getTasks());

        Log.d(TAG, "saveTasks: todo - " + taskList.getTodoList().size() + '\n' +
                "doing - " + taskList.getDoingList().size() + '\n' +
                "done - " + taskList.getDoneList().size());
        Gson gson = new Gson();
        Intent intent = new Intent(CustomTabs.this, TaskListCategory.class);
        String taskToEdit = gson.toJson(taskList);

        intent.putExtra(RETURN_INTENT_KEY_TASK_LIST, taskToEdit);
        intent.putExtra(RETURN_INTENT_KEY_TASK_LIST_POSITION, TASK_LIST_POSITION);
        setResult(RESULT_OK, intent);
        finish();
    }
}