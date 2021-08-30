package com.example.todomain.tabbedfragments;

import android.app.Application;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.todomain.itemtouchhelpers.ItemTouchHelperCallback;
import com.example.todomain.R;
import com.example.todomain.misc.TaskAdapter;
import com.example.todomain.pojo.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DoingFragment extends Fragment {

    /********************************** Fragment Fields *********************************/

    private static final String ARG_LIST = "argList";
    private static final String TAG = "DoingFragment";

    private RecyclerView rv;

    public TaskAdapter listAdapter;
    private ArrayList<Task> doingList;

    /********************************** Fragment setup methods *********************************/

    public DoingFragment() {}

    public static DoingFragment newInstance(ArrayList<Task> paramList) {
        DoingFragment fragment = new DoingFragment();
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

            doingList = gson.fromJson(taskListJSON, type);
        }
        Log.d(TAG, "onCreate: DoingFragment!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_doing, container, false);
        initViews(v);
        Log.d(TAG, "onCreateView: DoingFragment!");
        return v;
    }

    /********************************** Fragment Helper Methods *********************************/

    private void initViews(View v) {
        FloatingActionButton fab = v.findViewById(R.id.addNewTask2);
        rv = v.findViewById(R.id.tasks_list2);

        buildRecyclerView();
        loadData();
    }

    private void loadData() {
        for(Task task: doingList)
            listAdapter.addTaskToList(task);
    }

    private void buildRecyclerView() {
        // Setup custom adapter to the recycler view
        listAdapter = new TaskAdapter(getContext(), getActivity(), 1);
        rv.setAdapter(listAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // For Drag and Drop of features on the recycler view
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rv);
    }

    public void addNewTaskToList(Task task){
        listAdapter.addTaskToList(task);
    }

    public ArrayList<Task> getTasks(){
        Log.d(TAG, "getTasks: " + (listAdapter!=null));
        return (listAdapter!=null)? (listAdapter.getTaskList()) : (new ArrayList<>());
    }
}