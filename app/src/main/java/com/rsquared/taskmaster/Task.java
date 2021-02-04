package com.rsquared.taskmaster;

import android.os.Parcel;
import android.os.Parcelable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Task class: holds all the information for an individual task
public class Task implements Parcelable {
  // This function is for using with the view model object, and not sure how to explain it
  public static final Creator<Task> CREATOR =
      new Creator<Task>() {
        @Contract("_ -> new")
        @Override
        public @NotNull Task createFromParcel(Parcel in) {
          return new Task(in);
        }

        @Contract(value = "_ -> new", pure = true)
        @Override
        public Task @NotNull [] newArray(int size) {
          return new Task[size];
        }
      };

  private long _id;
  private String label;
  private int importance; // (1-100)
  private int urgency; // (1-100)
  private boolean completed; // (0 or 1)

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

  // This constructor is for using the view model object, and not sure how to explain it
  protected Task(@NotNull Parcel in) {
    _id = in.readInt();
    label = in.readString();
    importance = in.readInt();
    urgency = in.readInt();
    completed = in.readByte() != 0;
  }

  // SETTER FUNCTIONS

  private void setID(long newID) {
    this._id = newID;
  }

  public void setLabel(String newLabel) {
    label = newLabel;
  }

  public void setImportance(int newImportance) {
    if (validateImportance(newImportance)) {
      importance = newImportance;
    } else {
      throw new IllegalArgumentException("Importance must be an integer between 0 and 100");
    }
  }

  public void setUrgency(int newUrgency) {
    if (validateUrgency(newUrgency)) {
      urgency = newUrgency;
    } else {
      throw new IllegalArgumentException("Urgency must be an integer between 0 and 100");
    }
  }

  public void setCompleted(boolean newCompleted) {
    completed = newCompleted;
  }

  public void setTaskGraphic(TaskGraphic newTaskGraphic) {
    taskGraphic = newTaskGraphic;
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

  // VALIDATION & SANITATION FUNCTIONS

  // "Rating" is either "importance" or "urgency"
  public boolean validateRating(int rating) {
    return rating >= 0 && rating <= 100;
  }

  public boolean validateImportance(int importance) {
    return validateRating(importance);
  }

  public boolean validateUrgency(int urgency) {
    return validateRating(urgency);
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

  // VIEW MODEL FUNCTIONS

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NotNull Parcel dest, int flags) {
    dest.writeLong(_id);
    dest.writeString(label);
    dest.writeInt(importance);
    dest.writeInt(urgency);
    dest.writeByte((byte) (completed ? 1 : 0));
  }
}
