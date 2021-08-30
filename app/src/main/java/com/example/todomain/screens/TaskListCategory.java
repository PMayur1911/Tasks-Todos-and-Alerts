package com.example.todomain.screens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.todomain.R;
import com.example.todomain.misc.TaskListAdapter;
import com.example.todomain.pojo.Task;
import com.example.todomain.pojo.TaskList;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TaskListCategory extends AppCompatActivity
        implements TaskListAdapter.onTaskListClickedListener{

    private static final String TAG = "TaskListCategory";
    public static final String INTENT_KEY_TASK_LIST = "taskList";
    public static final String INTENT_KEY_TASK_LIST_POSITION = "taskListPosition";
    public static final int RC_TASK_LIST = 48;
    private static final String SHARED_PREFS = "shared_prefs_task_list";
    private static final String SP_KEY_TASK_LIST = "sp_task_list_key";


    private TaskListAdapter adapter;

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list_category);

        buildRecyclerView();
        setFabAction();
        loadData();
    }

    private void setFabAction() {
        FloatingActionButton fab = findViewById(R.id.taskListFab);
        fab.setOnClickListener(v -> {
            // Setup the bottom sheet dialog
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,
                    R.style.BottomSheetDialogTheme);

            View bottomSheetView = LayoutInflater.from(this)
                    .inflate(R.layout.bottom_sheet_dialog_task_list, (ConstraintLayout)
                            (this.findViewById(R.id.bottomSheetDialogTaskListContainer)));

            // Save Button
            bottomSheetView.findViewById(R.id.btnSaveTaskList).setOnClickListener(v1 -> {
                String title = ((EditText)bottomSheetView.findViewById(R.id.taskListName)).getText().toString();

                if(!title.equals("")) {
                    ArrayList<Task> listTemplate = new ArrayList<>();
                    adapter.addTaskList(new TaskList(title, listTemplate, listTemplate, listTemplate));
                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });
    }

    private void buildRecyclerView() {
        RecyclerView rv = findViewById(R.id.taskListRecyclerView);

        adapter = new TaskListAdapter(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Saving the list of tasks using shared preferences
     */
    private void saveData(){
        final ArrayList<TaskList> taskList = adapter.getTaskList();

        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();

        String taskInJson = gson.toJson(taskList);
        editor.putString(SP_KEY_TASK_LIST, taskInJson);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String taskInJson = sp.getString(SP_KEY_TASK_LIST, null);
        Type type = new TypeToken<ArrayList<TaskList>>() {}.getType();

        ArrayList<TaskList> taskList = gson.fromJson(taskInJson, type);

        if(taskList == null)
            taskList = new ArrayList<TaskList>();

        for(TaskList task: taskList)
            adapter.addTaskList(task);
    }

    @Override
    public void onTaskListClick(TaskList taskList, int position) {
        Intent intent = new Intent(TaskListCategory.this, CustomTabs.class);
        Gson gson = new Gson();

        String taskListString = gson.toJson(taskList);
        intent.putExtra(INTENT_KEY_TASK_LIST, taskListString);
        intent.putExtra(INTENT_KEY_TASK_LIST_POSITION, position);

        startActivityForResult(intent, RC_TASK_LIST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: Result - " + resultCode + "\t Request - " + requestCode);
        if(requestCode == RC_TASK_LIST){
            Log.d(TAG, "onActivityResult: Request code matched!");
            if(data != null){
                Log.d(TAG, "onActivityResult: intent data not null!");
                int position = data.getIntExtra(CustomTabs.RETURN_INTENT_KEY_TASK_LIST_POSITION, 0);
                String taskListString = data.getStringExtra(CustomTabs.RETURN_INTENT_KEY_TASK_LIST);

                Gson gson = new Gson();
                Type type = new TypeToken<TaskList>() {}.getType();

                adapter.updateTaskList((TaskList) gson.fromJson(taskListString, type), position);

                TaskList taskList = adapter.getTaskList().get(position);
                Log.d(TAG, "saveTasks: todo - " + taskList.getTodoList().size() + '\n' +
                        "doing - " + taskList.getDoingList().size() + '\n' +
                        "done - " + taskList.getDoneList().size());
            }
        }
    }
}