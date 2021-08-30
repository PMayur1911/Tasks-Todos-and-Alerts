package com.example.todomain.pojo;

import android.app.PendingIntent;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Task {
    private String taskTitle;
    private String taskDesc;

    private Calendar taskSchedule;
    private boolean soundAlarm;
    private PendingIntent pendingIntentAlarm;

    public static final int DATE = 0;
    public static final int TIME = 1;
    public static final int SCHEDULE = 2;

    public Task(){
        this("","");
    }

    public Task(String taskTitle, String taskDesc) {
        this.taskTitle = taskTitle;
        this.taskDesc = taskDesc;
        this.taskSchedule = null;
        this.soundAlarm = false;
        this.pendingIntentAlarm = null;
    }

    public Task(String taskTitle, String taskDesc, Calendar taskSchedule, boolean soundAlarm) {
        this.taskTitle = taskTitle;
        this.taskDesc = taskDesc;
        this.taskSchedule = taskSchedule;
        this.soundAlarm = soundAlarm;
        this.pendingIntentAlarm = null;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public boolean isSoundAlarm() {
        return soundAlarm;
    }

    public void setSoundAlarm(boolean soundAlarm) {
        this.soundAlarm = soundAlarm;
    }

    public Calendar getTaskSchedule() {
        return taskSchedule;
    }

    public String getTaskScheduleString(int form){
        if(this.taskSchedule == null)
            return "";

        String format = "";
        switch(form){
            case DATE:
                format = "MMM d yyyy";
                break;
            case TIME:
                format = "HH:mm";
                break;
            case SCHEDULE:
                format = "HH:mm, MMM d yyyy";
                break;
        }
        CharSequence cs = DateFormat.format(format, taskSchedule);
        return cs.toString();
    }

    public void setTaskSchedule(Calendar taskSchedule) {
        this.taskSchedule = taskSchedule;
    }

    public PendingIntent getPendingIntentAlarm() {
        return pendingIntentAlarm;
    }

    public void setPendingIntentAlarm(PendingIntent pendingIntentAlarm) {
        this.pendingIntentAlarm = pendingIntentAlarm;
    }

    @NonNull
    @Override
    public String toString() {
        final String s = "\nTitle - " + taskTitle + '\n' +
                "Desc - " + taskDesc + '\n' +
                "Schedule - " + getTaskScheduleString(SCHEDULE);
        return s;
    }
}
