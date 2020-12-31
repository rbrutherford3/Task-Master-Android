package com.rsquared.taskmaster;

import android.app.Application;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.HashMap;

// View model class to hold all the active tasks, plus update or read from database
public class TaskViewModel extends AndroidViewModel {

    // Sub-class for information that is graphical in nature (coordinates, shapes)
    public static class TaskGraphic {

        // PRIVATE MEMBERS

        private Float baseline;         // y-value of bottom of task text and checkbox
        private Float checkBoxStart;    // x-value of the beginning (left side) of task checkbox
        private Float textStart;        // x-value of the beginning of task text
        private Rect touchArea;         // rectangular area around task graphic for touch response

        // CONSTRUCTORS

        public TaskGraphic() {
        }

        public TaskGraphic(Float baseline, Float checkBoxStart, Float textStart, Rect touchArea) {
            this.baseline = baseline;
            this.checkBoxStart = checkBoxStart;
            this.textStart = textStart;
            this.touchArea = touchArea;
        }

        // GETTER METHODS

        public Float getBaseline() {
            return baseline;
        }

        public Float getCheckBoxStart() {
            return checkBoxStart;
        }

        public Float getTextStart() {
            return textStart;
        }

        public Rect getTouchArea() {
            return touchArea;
        }

        // SETTER METHODS

        public void setBaseline(Float y) {
            baseline = y;
        }

        public void setCheckBoxStart(Float x) {
            checkBoxStart = x;
        }

        public void setTextStart(Float x) {
            textStart = x;
        }

        public void setTouchArea(Rect area) {
            touchArea = area;
        }
    }

    // PRIVATE MEMBERS

    // List of tasks
    private ArrayList<Task> tasks = new ArrayList<>();

    // Hash maps to add supplemental graphics information for TaskDraw function
    private HashMap<Task, TaskGraphic> taskGraphicsHash = new HashMap<>();

    // Database object
    private final TaskDatabaseHelper taskDatabaseHelper = TaskDatabaseHelper.getInstance(this.getApplication());

    // An extra measure to assure that downloading data from the database only occurs once
    private boolean addTasksLocked = false;

    // CONSTRUCTOR
    public TaskViewModel(@NonNull Application application) {
        super(application);
    }

    // GETTER FUNCTIONS

    // retrieve task with matching ID (not used)
    public Task getTask(long id) {
        for (Task task : tasks) {
            if (task.getID() == id)
                return task;
        }
        return null;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public TaskGraphic getTaskGraphic(Task task) {
        return taskGraphicsHash.get(task);
    }

    public ArrayList<TaskGraphic> getTaskGraphics() {
        ArrayList<TaskGraphic> taskGraphics = new ArrayList<>();
        for (Task task : tasks) {
            taskGraphics.add(getTaskGraphic(task));
        }
        return taskGraphics;
    }

    public TaskDatabaseHelper getTaskDatabaseHelper() {
        return this.taskDatabaseHelper;
    }

    // SETTER FUNCTIONS (note that the key to the hash maps is the task object)

    // Adds new task to task list and database
    public long addNewTask(Task newTask) {
        tasks.add(newTask);
        return taskDatabaseHelper.addNewTask(newTask);
    }

    // Store all incomplete tasks from the database to the task list array (used at the beginning)
    public void downloadIncompleteTasks() {
        if (!addTasksLocked) {
            tasks = taskDatabaseHelper.getTasks(true);
            addTasksLocked = true;
        } else
            throw new IllegalStateException("Downloading from the database should occur only once");
    }

    // Store graphic information for an individual task
    public void addTaskGraphic(Task task, TaskGraphic taskGraphic) {
        taskGraphicsHash.put(task, taskGraphic);
    }

    // Update a modified task in the database
    public void updateTask(Task task) {
        // Since the Task object is actually just a pointer, we only need to update the database
        taskDatabaseHelper.updateTask(task);
    }
}