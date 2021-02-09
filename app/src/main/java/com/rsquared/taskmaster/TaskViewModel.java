package com.rsquared.taskmaster;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

// View model class to hold all the active tasks, plus update or read from database
// Note that task groups are not considered because they don't need to survive fragment changes
// Todo: add ability to revert to database values loaded upon start of app
public class TaskViewModel extends AndroidViewModel {

  // PRIVATE MEMBERS

  // Database object
  private final TaskDatabaseHelper taskDatabaseHelper =
      TaskDatabaseHelper.getInstance(this.getApplication());
  // List of tasks
  private Set<Task> tasks = new HashSet<>();
  private Set<TaskGroup> taskGroups = new HashSet<>();
  // An extra measure to assure that downloading data from the database only occurs once
  private boolean downloadTasksLocked = false;

  // CONSTRUCTOR

  public TaskViewModel(@NonNull Application application) {
    super(application);
  }

  // SETTER FUNCTIONS

  // Adds new task to task list and database

  public void addTask(Task task) {
    tasks.add(task);
    taskDatabaseHelper.addTask(task);
  }

  public void addTasks(@NotNull Set<Task> tasks) {
    for (Task task : tasks) {
      addTask(task);
    }
  }

  // Adds new task to task list and database
  public void addTaskGroup(@NotNull TaskGroup taskGroup) {
    tasks.removeAll(taskGroup.getTasks());
    taskGroups.add(taskGroup);
  }

  public void addTaskGroups(@NotNull Set<TaskGroup> taskGroups) {
    for (TaskGroup taskGroup : taskGroups) {
      addTaskGroup(taskGroup);
    }
  }

  // Update a modified task in the database
  public void updateTask(Task task) {
    // Since the Task object is actually just a pointer, we only need to update the database
    taskDatabaseHelper.updateTask(task);
  }

  // Store all incomplete tasks from the database to the task list array (used at the beginning)
  public void downloadIncompleteTasks() {
    if (!downloadTasksLocked) {
      tasks.clear();
      taskGroups.clear();
      tasks.addAll(taskDatabaseHelper.getTasks(true));
      downloadTasksLocked = true;
    } // else {
    // throw new IllegalStateException("Downloading from the database should occur only once");
    // }
  }

  public void deGroupTasks() {
    for (TaskGroup taskGroup : taskGroups) {
      tasks.addAll(taskGroup.getTasks());
    }
    taskGroups = new HashSet<>();
  }

  // GETTER FUNCTIONS

  public Set<Task> getTasks() {
    return tasks;
  }

  public Set<TaskGroup> getTaskGroups() {
    return taskGroups;
  }

  public TaskDatabaseHelper getTaskDatabaseHelper() {
    return taskDatabaseHelper;
  }
}
