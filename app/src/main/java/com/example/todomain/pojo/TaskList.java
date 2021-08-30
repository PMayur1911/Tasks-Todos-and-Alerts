package com.example.todomain.pojo;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class TaskList {
    private String taskListName;
    private ArrayList<Task> todoList, doingList, doneList;

    public TaskList(String taskListName,
                    ArrayList<Task> todoList, ArrayList<Task> doingList, ArrayList<Task> doneList) {
        this.taskListName = taskListName;
        this.todoList = todoList;
        this.doingList = doingList;
        this.doneList = doneList;
    }

    public String getTaskListName() {
        return taskListName;
    }

    public void setTaskListName(String taskListName) {
        this.taskListName = taskListName;
    }

    public ArrayList<Task> getTodoList() {
        return todoList;
    }

    public void setTodoList(ArrayList<Task> todoList) {
        this.todoList = todoList;
    }

    public ArrayList<Task> getDoingList() {
        return doingList;
    }

    public void setDoingList(ArrayList<Task> doingList) {
        this.doingList = doingList;
    }

    public ArrayList<Task> getDoneList() {
        return doneList;
    }

    public void setDoneList(ArrayList<Task> doneList) {
        this.doneList = doneList;
    }
}
