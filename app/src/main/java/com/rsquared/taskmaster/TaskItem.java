package com.rsquared.taskmaster;

// Interface to apply same methods to Task or Task Group
public interface TaskItem {

	// GETTER METHODS

	String getLabel();

	void setLabel(String label);

	int getImportance();

	int getUrgency();

	// SETTER METHODS

	TaskGraphic getTaskGraphic();

	void setTaskGraphic(TaskGraphic taskGraphic);

	TaskGroup addTaskItem(TaskItem taskItem);

}