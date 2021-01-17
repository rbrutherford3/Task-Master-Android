package com.rsquared.taskmaster;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

// This class for task groups, and is much like the task class
public class TaskGroup implements TaskItem {

	// PRIVATE MEMBERS

	private final int maxNudges = 3;
	private ArrayList<Task> tasks = new ArrayList<>();
	private String label;
	private int averageImportance;
	private int averageUrgency;
	private boolean forceCombining = false;
	private TaskGraphic taskGraphic;

	// CONSTRUCTORS

	// Initialize with an undetermined mixture of tasks and task groups
	public TaskGroup(@NotNull TaskItem... taskItemList) {
		for (TaskItem taskItem : taskItemList) {
			addTaskItem(taskItem);
		}
	}

	// Alternative constructor for arraylist instead of a list
	public TaskGroup(@NotNull ArrayList<TaskItem> taskItemList) {
		for (TaskItem taskOrGroup : taskItemList)
			addTaskItem(taskOrGroup);
	}

	// SETTER METHODS

	// Create a list of groups
	public TaskGroup addTaskItem(@NotNull TaskItem taskItem) {
		if (taskItem instanceof Task) {
			addTask((Task) taskItem);
		} else {
			addTaskGroup((TaskGroup) taskItem);
		}
		return getGroup();
	}

	private void addTask(@NotNull Task newTask) {
		tasks.add(newTask);
		combine();
	}

	private void addTaskGroup(@NotNull TaskGroup newTaskGroup) {
		for (Task task : newTaskGroup.tasks) {
			addTask(task);
		}
	}

	// Get average urgency and importance from individual tasks, and sort by importance
	private void combine() {
		averageImportance = 0;
		averageUrgency = 0;
		for (Task task : tasks) {
			averageImportance += task.getImportance();
			averageUrgency += task.getUrgency();
		}
		averageImportance = averageImportance / tasks.size();
		averageUrgency = averageUrgency / tasks.size();

		Collections.sort(tasks, (Task t1, Task t2) -> {
			return t2.getImportance() - t1.getImportance(); // Descending
		});

		label = tasks.size() + " tasks";
	}

	public void setForceCombining(boolean forceCombining) {
		this.forceCombining = forceCombining;
	}

	public TaskGroup getGroup() {
		return this;
	}  // In case someone needs the group itself

	public String getLabel() {
		return label;
	}

	// GETTER METHODS

	public void setLabel(String label) {
		this.label = label;
	}

	public int getImportance() {
		return averageImportance;
	}

	public int getUrgency() {
		return averageUrgency;
	}

	public boolean getNudging() {
		return (tasks.size() <= maxNudges) && !forceCombining;
	}

	public TaskGraphic getTaskGraphic() {
		return taskGraphic;
	}

	public void setTaskGraphic(TaskGraphic taskGraphic) {
		this.taskGraphic = taskGraphic;
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}
}