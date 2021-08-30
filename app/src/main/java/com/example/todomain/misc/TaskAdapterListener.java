package com.example.todomain.misc;

import com.example.todomain.pojo.Task;

public interface TaskAdapterListener {
    void transferTask(Task task, int destFragPosition);
    void displayEditDialog(TaskAdapter taskAdapter, Task task, int pos);
}
