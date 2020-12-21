package com.rsquared.taskmaster;

import android.graphics.Rect;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;

// View model class to hold all the information for individual tasks
public class TaskViewModel extends ViewModel {

    // PRIVATE MEMBERS TO HOLD INFORMATION
    
    // List of tasks
    private final ArrayList<Task> tasks = new ArrayList<>();
    // Holds the task that pops up to be marked for completion
    private Task completedTask;
    // ***Hash maps to add supplemental graphics information for TaskDraw function***
    // y-axis value of bottom of checkbox/text
    private final HashMap<Long, Float> yBaselines = new HashMap<>();
    // x-axis value of left side of checkbox
    private final HashMap<Long, Float> xCheckBoxStarts = new HashMap<>();
    // x-axis value of left side of text
    private final HashMap<Long, Float> xTextStarts = new HashMap<>();
    // derived rectangle object surrounding everything
    private final HashMap<Long, Rect> touchAreas = new HashMap<>();

    // GETTER FUNCTIONS (note that the key to the hash maps is the database ID)

    public ArrayList<Task> getTasks() {return this.tasks;}
    public Task getCompletedTask() {return completedTask;}
    public Float getBaseline(long id) {return yBaselines.get(id);}
    public Float getCheckBoxStart(long id) {return xCheckBoxStarts.get(id);}
    public Float getTextStart(long id) {return xTextStarts.get(id);}
    public Rect getTouchArea(long id) {return touchAreas.get(id);}

    // SETTER FUNCTIONS (note that the key to the hash maps is the database ID)

    public void addTask(Task task) {this.tasks.add(task);}
    public void setCompletedTask(Task completedTask) {this.completedTask = completedTask;}
    public void setBaseline(long id, Float y) {this.yBaselines.put(id, y);}
    public void setCheckBoxStart(long id, Float x) {this.xCheckBoxStarts.put(id, x);}
    public void setTextStart(long id, Float x) {this.xTextStarts.put(id, x);}
    public void setTouchArea(long id, Rect touchArea) {this.touchAreas.put(id, touchArea);}
}