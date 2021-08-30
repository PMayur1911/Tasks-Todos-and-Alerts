package com.example.todomain.tabbedfragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todomain.itemtouchhelpers.ItemTouchHelperCallback;
import com.example.todomain.R;
import com.example.todomain.misc.AlarmReceiver;
import com.example.todomain.misc.TaskAdapter;
import com.example.todomain.pojo.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class TodoFragment extends Fragment {

    /********************************** Fragment Fields *********************************/

    private static final String ARG_LIST = "argList";
    private static final String TAG = "RBRTodoFragment";

    private FloatingActionButton fab;
    private RecyclerView rv;

    private TaskAdapter listAdapter;
    private ArrayList<Task> todoList;
    private Calendar schedule;
    private AlarmManager alarmManager;

    /********************************** Fragment setup methods *********************************/

    public TodoFragment() {}

    public static TodoFragment newInstance(ArrayList<Task> paramList) {
        TodoFragment fragment = new TodoFragment();
        Bundle args = new Bundle();
        Gson gson = new Gson();

        String taskListJSON = gson.toJson(paramList);
        args.putString(ARG_LIST, taskListJSON);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();
            String taskListJSON = getArguments().getString(ARG_LIST);
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();

            todoList = gson.fromJson(taskListJSON, type);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_todo, container, false);
        initViews(v);
        return v;
    }

    /********************************** Fragment Helper Methods *********************************/

    private void initViews(View v) {
        fab = v.findViewById(R.id.addNewTask1);
        rv = v.findViewById(R.id.tasks_list1);
        schedule = new GregorianCalendar();
        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        buildRecyclerView();
        setFabAction();
        loadData();
    }

    private void loadData() {
        for(Task task: todoList)
            listAdapter.addTaskToList(task);
    }

    private void buildRecyclerView() {
        // Setup custom adapter to the recycler view
        listAdapter = new TaskAdapter(getContext(), getActivity(), 0);
        rv.setAdapter(listAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // For Drag and Drop of features on the recycler view
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rv);
    }

    public void addNewTaskToList(Task task){
        Log.d(TAG, "addNewTaskToList: Task to Add: " + task.toString());
        listAdapter.addTaskToList(task);
    }

    private void setFabAction() {
        fab.setOnClickListener(v -> {
            // Setup the bottom sheet dialog
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),
                    R.style.BottomSheetDialogTheme);

            View bottomSheetView = LayoutInflater.from(getContext())
                    .inflate(R.layout.bottom_sheet_dialog_task, (ConstraintLayout)(requireActivity()
                            .findViewById(R.id.bottomSheetDialogTaskListContainer)));

            // Save Button
            bottomSheetView.findViewById(R.id.btnSaveTaskList).setOnClickListener(v1 -> {

                String title = ((EditText)bottomSheetView.findViewById(R.id.taskTitle)).getText().toString();
                String detail = ((EditText)bottomSheetView.findViewById(R.id.taskDetail)).getText().toString();
                String deadline = ((TextView)bottomSheetView.findViewById(R.id.taskSchedule)).getText().toString();
                boolean option = ((SwitchMaterial)bottomSheetView.findViewById(R.id.notifySwitchOption)).isChecked();

                if(!title.equals("")) {
                    Task newTask;
                    if(deadline.equals(""))
                        newTask = new Task(title, detail);
                    else{
                        newTask = new Task(title, detail, schedule, option);
                        if(option){
                            // Set Alarm
                            Intent intent = new Intent(requireContext().getApplicationContext(), AlarmReceiver.class);

                            PendingIntent pIntent = PendingIntent.getBroadcast(requireContext().getApplicationContext()
                                    , 0, intent, 0);
                            alarmManager.set(AlarmManager.RTC_WAKEUP, schedule.getTimeInMillis(), pIntent);

                            newTask.setPendingIntentAlarm(pIntent);
                            Toast.makeText(requireContext(), "Alarm set for " + newTask.getTaskScheduleString(Task.SCHEDULE), Toast.LENGTH_SHORT).show();
                        }
                        // Send notification either-way


                    }

                    listAdapter.addTaskToList(newTask);
                    bottomSheetDialog.dismiss();
                }
            });

            // Set Schedule Button
            bottomSheetView.findViewById(R.id.btnSetSchedule).setOnClickListener(v13 -> {
                ConstraintLayout scheduleLayout = bottomSheetView.findViewById(R.id.scheduleLayout);
                ConstraintLayout notifyOptionLayout = bottomSheetView.findViewById(R.id.notifyOptionLayout);
                if(scheduleLayout.getVisibility() == View.GONE) {
                    scheduleLayout.setVisibility(View.VISIBLE);
                    notifyOptionLayout.setVisibility(View.VISIBLE);
                }
                showDateDialog(bottomSheetView);
            });

            // Remove Schedule Button
            bottomSheetView.findViewById(R.id.btnDeleteTime).setOnClickListener(v14 -> {
                ((ConstraintLayout) bottomSheetView.findViewById(R.id.scheduleLayout)).setVisibility(View.GONE);
                ((ConstraintLayout) bottomSheetView.findViewById(R.id.notifyOptionLayout)).setVisibility(View.GONE);
                ((TextView)bottomSheetView.findViewById(R.id.taskSchedule)).setText("");
            });

            // Edit Schedule Button
            bottomSheetView.findViewById(R.id.taskSchedule)
                    .setOnClickListener(v12 -> showDateDialog(bottomSheetView));

            // Notification Switch
            ((SwitchMaterial)bottomSheetView.findViewById(R.id.notifySwitchOption)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                final TextView txtNotifyOnly = bottomSheetView.findViewById(R.id.txtNotifyOnly);
                final TextView txtSound = bottomSheetView.findViewById(R.id.txtSound);
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        txtNotifyOnly.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                        txtSound.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AEEA00")));
                    }
                    else{
                        txtNotifyOnly.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AEEA00")));
                        txtSound.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }
                }
            });


            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });
    }

    private void showDateDialog(View view) {

        schedule = new GregorianCalendar();

        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (datePickerView, year, month, dayOfMonth) -> {
                    schedule.set(Calendar.YEAR, year);
                    schedule.set(Calendar.MONDAY, month);
                    schedule.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    showTimeDialog(view);
                }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void showTimeDialog(View view) {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR_OF_DAY);
        int MIN = calendar.get(Calendar.MINUTE);

        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (timePickerView, hourOfDay, minute) -> {
                    schedule.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    schedule.set(Calendar.MINUTE, minute);

                    String format = "HH:mm, MMM d yyyy";
                    CharSequence cs = DateFormat.format(format, schedule);

                    ((TextView)view.findViewById(R.id.taskSchedule)).setText(cs);
                }, HOUR, MIN, is24HourFormat);

        timePickerDialog.show();
    }

    public ArrayList<Task> getTasks(){
        Log.d(TAG, "getTasks: " + (listAdapter!=null));
        return (listAdapter!=null)? (listAdapter.getTaskList()) : (new ArrayList<>());
    }
}