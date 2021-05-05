package com.rsquared.taskmaster;

import org.jetbrains.annotations.NotNull;

// Task class: holds all the information for an individual task
public class Task {

  private long _id;
  private String label;
  private int importance; // (1-100)
  private int urgency; // (1-100)
  private boolean completed; // (0 or 1)
  private boolean moving = false;

  // CONSTRUCTORS
  // 4 overloaded constructors (2 with 'ID', 2 w/out; 2 w/ boolean as 'completed',  2 w/ an int)
  private TaskGraphic taskGraphic;

  public Task(
      long newID, String newLabel, int newImportance, int newUrgency, boolean newCompleted) {
    setID(newID);
    setLabel(newLabel);
    setImportance(newImportance);
    setUrgency(newUrgency);
    setCompleted(newCompleted);
  }

  public Task(String newLabel, int newImportance, int newUrgency, boolean newCompleted) {
    setLabel(newLabel);
    setImportance(newImportance);
    setUrgency(newUrgency);
    setCompleted(newCompleted);
  }

  // SETTER FUNCTIONS

  private void setID(long newID) {
    this._id = newID;
  }

  public void setLabel(String newLabel) {
    label = newLabel;
  }

  public void setImportance(int newImportance) {
      importance = sanitizeRating(newImportance);
  }

  public void setUrgency(int newUrgency) {
      urgency = sanitizeRating(newUrgency);
  }

  public void setCompleted(boolean newCompleted) {
    completed = newCompleted;
  }

  public void setTaskGraphic(TaskGraphic newTaskGraphic) {
    taskGraphic = newTaskGraphic;
  }

  public void setMoving(boolean entry) {
    this.moving = entry;
  }

  // GETTER FUNCTIONS

  public long getID() {
    return _id;
  }

  public String getLabel() {
    return label;
  }

  public int getImportance() {
    return importance;
  }

  public int getUrgency() {
    return urgency;
  }

  public boolean getCompleted() {
    return completed;
  }

  public TaskGraphic getTaskGraphic() {
    return taskGraphic;
  }

  public boolean getMoving() {
    return moving;
  }

  // VALIDATION & SANITATION FUNCTIONS

  public int sanitizeRating(int rating) {
    if (rating < 0) {
      return 0;
    }
    else return Math.min(rating, 100);
  }

  // DEBUG ONLY FUNCTION

  @Override
  public @NotNull String toString() {
    String output = "";
    output = output.concat("CONTENTS OF Task INSTANCE:\n");
    output = output.concat("-id: " + _id + "\n");
    output = output.concat("-label: " + label + "\n");
    output = output.concat("-importance: " + importance + "\n");
    output = output.concat("-urgency: " + urgency + "\n");
    output = output.concat("-completed " + completed + "\n");
    output = output.concat(taskGraphic.toString());
    return output;
  }
}
