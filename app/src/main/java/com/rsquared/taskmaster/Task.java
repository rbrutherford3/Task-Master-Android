package com.rsquared.taskmaster;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

// Task class: holds all the information for an individual task
public class Task implements Parcelable {

  private long _id;
  private String label;
  private int urgency; // (1-100)
  private int importance; // (1-100)
  private boolean completed; // (0 or 1)
  private boolean moving = false;

  // CONSTRUCTORS
  // 4 overloaded constructors (2 with 'ID', 2 w/out; 2 w/ boolean as 'completed',  2 w/ an int)
  private TaskGraphic taskGraphic;

  public Task(
      long newID, String newLabel, int newUrgency, int newImportance, boolean newCompleted) {
    setID(newID);
    setLabel(newLabel);
    setUrgency(newUrgency);
    setImportance(newImportance);
    setCompleted(newCompleted);
  }

  public Task(String newLabel, int newUrgency, int newImportance, boolean newCompleted) {
    setLabel(newLabel);
    setUrgency(newUrgency);
    setImportance(newImportance);
    setCompleted(newCompleted);
  }

  // SETTER FUNCTIONS

  protected Task(Parcel in) {
    _id = in.readLong();
    label = in.readString();
    urgency = in.readInt();
    importance = in.readInt();
    completed = in.readByte() != 0;
    moving = in.readByte() != 0;
  }

  public static final Creator<Task> CREATOR = new Creator<Task>() {
    @Override
    public Task createFromParcel(Parcel in) {
      return new Task(in);
    }

    @Override
    public Task[] newArray(int size) {
      return new Task[size];
    }
  };

  private void setID(long newID) {
    this._id = newID;
  }

  public void setLabel(String newLabel) {
    label = newLabel;
  }

  public void setUrgency(int newUrgency) {
    urgency = sanitizeRating(newUrgency);
  }

  public void setImportance(int newImportance) {
      importance = sanitizeRating(newImportance);
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

  public int getUrgency() {
    return urgency;
  }

  public int getImportance() {
    return importance;
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
    output = output.concat("-urgency: " + urgency + "\n");
    output = output.concat("-importance: " + importance + "\n");
    output = output.concat("-completed " + completed + "\n");
    output = output.concat(taskGraphic.toString());
    return output;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(_id);
    dest.writeString(label);
    dest.writeInt(urgency);
    dest.writeInt(importance);
    dest.writeByte((byte) (completed ? 1 : 0));
    dest.writeByte((byte) (moving ? 1 : 0));
  }
}
