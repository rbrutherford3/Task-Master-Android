package com.rsquared.taskmaster;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;

// View model class to hold all the active tasks, plus update or read from database
public class TaskViewModel extends AndroidViewModel {

    // PRIVATE MEMBERS

    // Database object
    private final TaskDatabaseHelper taskDatabaseHelper = TaskDatabaseHelper.getInstance(this.getApplication());
    // List of tasks
    private ArrayList<TaskItem> taskItems = new ArrayList<>();
    // An extra measure to assure that downloading data from the database only occurs once
    private boolean addTasksLocked = false;

    // CONSTRUCTOR

    public TaskViewModel(@NonNull Application application) {
        super(application);
    }

    // GETTER FUNCTIONS

    // retrieve task with matching ID (not used)
    public Task getTask(long id) {
        for (TaskItem taskItem : taskItems) {
            if (taskItem instanceof Task && ((Task) taskItem).getID() == id)
                return (Task) taskItem;
        }
        return null;
    }

    public ArrayList<TaskItem> getTaskItems() {
        return taskItems;
    }

    // Get task items from outside source
    public void setTaskItems(ArrayList<TaskItem> taskItems) {
        this.taskItems = taskItems;
    }

    // SETTER FUNCTIONS

    public TaskDatabaseHelper getTaskDatabaseHelper() {
        return this.taskDatabaseHelper;
    }

    // Adds new task to task list and database
    public void addNewTaskItem(TaskItem newTaskItem) {
        taskItems.add(newTaskItem);
        if (newTaskItem instanceof Task) taskDatabaseHelper.addNewTask((Task) newTaskItem);
    }

    // Store all incomplete tasks from the database to the task list array (used at the beginning)
    public void downloadIncompleteTasks() {
        if (!addTasksLocked) {
            taskItems.addAll(taskDatabaseHelper.getTasks(true));
            addTasksLocked = true;
        } else
            throw new IllegalStateException("Downloading from the database should occur only once");
    }

    // Update a modified task in the database
    public void updateTask(Task task) {
        // Since the Task object is actually just a pointer, we only need to update the database
        taskDatabaseHelper.updateTask(task);
    }

    // De-attach each individual task from their groups
    public void deGroupTasks() {
        ArrayList<TaskGroup> taskGroups = new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<>();
        for (TaskItem taskItem : taskItems) {
            if (taskItem instanceof TaskGroup) {
                tasks.addAll(((TaskGroup) taskItem).getTasks());
                taskGroups.add((TaskGroup) taskItem);
            }
        }
        taskItems.addAll(tasks);
        taskItems.removeAll(taskGroups);
    }
}