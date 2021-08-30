package com.example.todomain.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todomain.R;
import com.example.todomain.pojo.Task;
import com.example.todomain.pojo.TaskList;

import java.util.ArrayList;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder>{

    private final ArrayList<TaskList> list;
    private final onTaskListClickedListener listener;

    public TaskListAdapter(Context context) {
        this.list = new ArrayList<>();
        this.listener = (onTaskListClickedListener) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        holder.taskListName.setText(list.get(position).getTaskListName());
        holder.taskListName.setOnClickListener(v -> listener.onTaskListClick(list.get(position), holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }


    public void addTaskList(TaskList newTaskList){
        this.list.add(newTaskList);
        notifyItemInserted(getItemCount());
    }

    public void updateTaskList(TaskList updatedTaskList, int position){
        this.list.set(position, updatedTaskList);
        notifyItemChanged(position);
    }

    public ArrayList<TaskList> getTaskList(){
        return this.list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView taskListName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskListName = itemView.findViewById(R.id.taskListName);
        }
    }

    public interface onTaskListClickedListener{
        void onTaskListClick(TaskList taskList, int position);
    }
}
