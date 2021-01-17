package com.rsquared.taskmaster;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Task class: holds all the information for an individual task
public class Task implements Parcelable, TaskItem {
    // This function is for using with the view model object, and not sure how to explain it
    public static final Creator<Task> CREATOR = new Creator<Task>() {
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

    public Task(long id, String label, int importance, int urgency, int completed) {
        setID(id);
        setLabel(label);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    public Task(long id, String label, int importance, int urgency, boolean completed) {
        setID(id);
        setLabel(label);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    public Task(String label, int importance, int urgency, int completed) {
        setLabel(label);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    public Task(String label, int importance, int urgency, boolean completed) {
        setLabel(label);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    // This constructor is for using the view model object, and not sure how to explain it
    protected Task(@NotNull Parcel in) {
        _id = in.readInt();
        label = in.readString();
        importance = in.readInt();
        urgency = in.readInt();
        completed = in.readByte() != 0;
    }

    @Override
    public TaskGroup addTaskItem(TaskItem taskItem) {
        return new TaskGroup(this, taskItem);
    }

    // GETTER FUNCTIONS

    public long getID() {
        return _id;
    }

    public void setID(long id) {
        this._id = id;
    }  // Should never be used

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        if (validateImportance(importance))
            this.importance = importance;
        else
            throw new IllegalArgumentException("Importance must be an integer between 0 and 100");
    }

    // VALIDATION & SANITATION FUNCTIONS

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        if (validateUrgency(urgency))
            this.urgency = urgency;
        else
            throw new IllegalArgumentException("Urgency must be an integer between 0 and 100");
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = sanitizeCompleted(completed);
    }

    // SETTER FUNCTIONS

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public TaskGraphic getTaskGraphic() {
        return taskGraphic;
    }

    public void setTaskGraphic(TaskGraphic taskGraphic) {
        this.taskGraphic = taskGraphic;
    }

    // "Rating" is either "importance" or "urgency"
    public boolean validateRating(int rating) {
        return ((rating >= 0) && (rating <= 100));
    }

    public boolean validateImportance(int importance) {
        return validateRating(importance);
    }

    public boolean validateUrgency(int urgency) {
        return validateRating(urgency);
    }

    private boolean sanitizeCompleted(int completed) {
        return (completed >= 1);
    }

    // DEBUG ONLY FUNCTION

    public void debug() {
        System.out.println("CONTENTS OF Task INSTANCE:");
        System.out.println("-id: " + _id);
        System.out.println("-label: " + label);
        System.out.println("-importance: " + importance);
        System.out.println("-urgency: " + urgency);
        System.out.println("-completed " + completed);
        taskGraphic.debug();
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