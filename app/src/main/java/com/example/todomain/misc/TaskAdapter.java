package com.example.todomain.misc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todomain.screens.CustomTabs;
import com.example.todomain.R;
import com.example.todomain.itemtouchhelpers.ItemTouchHelperAdapter;
import com.example.todomain.pojo.Task;

import java.util.ArrayList;
import java.util.Collections;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static final String TAG = "RBRTaskListAdapter";
    private final ArrayList<Task> taskList;
    private final Context context;
    private final Activity activity;
    private final int category;

    public TaskAdapterListener listener;

    public TaskAdapter(Context context, Activity activity, int category) {
        taskList = new ArrayList<>();
        this.context = context;
        this.activity = activity;
        this.category = category;
        this.listener = (TaskAdapterListener) context;
    }

    /******************************** Recycler View Adapter ********************************/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Invoked!");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.ViewHolder holder, int position) {
        Task currentTask = taskList.get(holder.getAdapterPosition());
        Log.d(TAG, "onBindViewHolder: Invoked!\n currentTask - " + currentTask.toString());



        // Binding the task details and setting them on the UI components

        holder.templateText.setText(currentTask.getTaskTitle());

        String desc = currentTask.getTaskDesc();
        holder.templateDesc.setText(desc);
        holder.templateDesc.setVisibility(
                (!desc.equals(""))? (View.VISIBLE):(View.GONE));

        holder.templateTime.setText(currentTask.getTaskScheduleString(Task.SCHEDULE));
        holder.templateTime.setVisibility(
                (currentTask.getTaskSchedule()==null ? View.GONE : View.VISIBLE));


        Button btnLeft = holder.btnMoveLeft;
        Button btnRight = holder.btnMoveRight;

        // Setting up the progress category switching buttons
        switch(this.category){
            case 0:
                btnRight.setOnClickListener(v -> {
                    int pos = holder.getLayoutPosition();
                    this.removeTaskFromList(pos);
                    listener.transferTask(currentTask, CustomTabs.DOING);
                });
                break;

            case 1:
                btnLeft.setOnClickListener(vL -> {
                    int pos = holder.getLayoutPosition();
                    this.removeTaskFromList(pos);
                    listener.transferTask(currentTask, CustomTabs.TODO);
                });

                btnRight.setOnClickListener(vR -> {
                    int pos = holder.getLayoutPosition();
                    this.removeTaskFromList(pos);
                    listener.transferTask(currentTask, CustomTabs.DONE);
                });
                break;

            case 2:
                btnLeft.setOnClickListener(v -> {
                    int pos = holder.getLayoutPosition();
                    this.removeTaskFromList(pos);
                    listener.transferTask(currentTask, CustomTabs.DOING);
                });
                break;
            default: break;
        }

        holder.btnEditTask.setOnClickListener(v -> {
            int pos = holder.getLayoutPosition();
            listener.displayEditDialog(this, taskList.get(pos), pos);
        });

        holder.btnRemoveTask.setOnClickListener(v -> {
            int pos = holder.getLayoutPosition();
            hideExtraViewsOnDelete(holder);
            this.removeTaskFromList(pos);
        });

        holder.btnMinUtility.setOnClickListener(v -> {
            holder.taskParentLayout.clearFocus();
        });

        holder.taskParentLayout.setOnFocusChangeListener((v, hasFocus) -> {
            boolean isFocussed;
            if(hasFocus) {
                holder.utilityLayout.setVisibility(View.VISIBLE);
                holder.btnMinUtility.setVisibility(View.VISIBLE);
                showTransferTaskButton(holder);
                Log.d(TAG, "onFocusChange: hasFocus");
            }
            else{
                holder.utilityLayout.setVisibility(View.GONE);
                holder.btnMinUtility.setVisibility(View.GONE);
                holder.btnMoveLeft.setVisibility(View.GONE);
                holder.btnMoveRight.setVisibility(View.GONE);
                Log.d(TAG, "onFocusChange: Doesn't have focus");
            }
        });

// TODO - To animate long press of a task on the list
//  and also give extra functions like edit, delete etc
        holder.taskParentLayout.setOnLongClickListener(v -> {
//                ValueAnimator colorAnim = ObjectAnimator.ofInt(btn, "backgroundColor",
//                        getColor(startColor), getColor(endColor));
//                colorAnim.setDuration(500);
//                colorAnim.setEvaluator(new ArgbEvaluator());
//                colorAnim.setRepeatCount(ValueAnimator.INFINITE);
//                colorAnim.setRepeatMode(ValueAnimator.REVERSE);
//                return colorAnim;


//                Animation anim = new AlphaAnimation(0.0f, 1.0f);
//                anim.setDuration(100);
//                anim.setStartOffset(20);
//                anim.setRepeatMode(Animation.REVERSE);
//                anim.setRepeatCount(1);
//                holder.taskParentLayout.startAnimation(anim);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /***************************** Recycler View List Helper methods ***************************/

    public void removeTaskFromList(int position){
        this.taskList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.taskList.size());
    }

    public void addTaskToList(Task newTask){
        Log.d(TAG, "addTaskToList: getItemCount - " + getItemCount() + '\n'
                + "arrayList size - " + taskList.size() + '\n'
                + "newTask - " + newTask.toString());
        this.taskList.add(newTask);
        notifyItemInserted(getItemCount());
    }

    public void updateTask(Task updatedTask, int position){
        taskList.set(position, updatedTask);
        notifyItemChanged(position);
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    private void hideExtraViewsOnDelete(ViewHolder holder) {
        holder.templateDesc.setVisibility(View.GONE);
        holder.templateTime.setVisibility(View.GONE);
        holder.taskParentLayout.clearFocus();
    }

    private void showTransferTaskButton(ViewHolder holder){
        Button btnLeft = holder.btnMoveLeft;
        Button btnRight = holder.btnMoveRight;

        // Setting up the progress category switching buttons
        switch(this.category){
            case 0:
                btnRight.setVisibility(View.VISIBLE);
                break;

            case 1:
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.VISIBLE);
                break;

            case 2:
                btnLeft.setVisibility(View.VISIBLE);
                break;
            default: break;
        }
    }

    /******************************** Item Touch Helper ********************************/

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(taskList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(taskList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        /* Implement this if swipe feature is to be enabled */
//        taskList.remove(position);
//        notifyItemRemoved(position);
    }

    /******************************** Inner View Holder Class ********************************/

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final ConstraintLayout taskParentLayout, utilityLayout;
        private final TextView templateText, templateDesc, templateTime;
        private final Button btnMoveLeft, btnMoveRight;
        private final Button btnEditTask, btnRemoveTask, btnMinUtility;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskParentLayout = itemView.findViewById(R.id.taskParentLayout);

            templateText = itemView.findViewById(R.id.templateText);
            templateDesc = itemView.findViewById(R.id.templateDesc);
            templateTime = itemView.findViewById(R.id.templateTime);

            btnMoveLeft = itemView.findViewById(R.id.btnMoveLeft);
            btnMoveRight = itemView.findViewById(R.id.btnMoveRight);

            utilityLayout = itemView.findViewById(R.id.utilityLayout);
            btnEditTask = itemView.findViewById(R.id.btnEditTask);
            btnRemoveTask = itemView.findViewById(R.id.btnRemoveTask);
            btnMinUtility = itemView.findViewById(R.id.btnMinUtility);

        }
    }
}
