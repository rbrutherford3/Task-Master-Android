package com.rsquared.taskmaster;

import android.os.Parcel;
import android.os.Parcelable;

// Task class: holds all the information for an individual task
public class Task implements Parcelable {
    private long _id;
    private String task;
    private int importance; // (1-100)
    private int urgency; // (1-100)
    private boolean completed; // (0 or 1)

    // CONSTRUCTORS
    // 4 overloaded constructors (2 with 'ID', 2 w/out; 2 w/ boolean as 'completed',  2 w/ an int)

    public Task(long id, String task, int importance, int urgency, int completed) {
        setID(id);
        setTask(task);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    public Task(long id, String task, int importance, int urgency, boolean completed) {
        setID(id);
        setTask(task);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    public Task(String task, int importance, int urgency, int completed) {
        setTask(task);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    public Task(String task, int importance, int urgency, boolean completed) {
        setTask(task);
        setImportance(importance);
        setUrgency(urgency);
        setCompleted(completed);
    }

    // This constructor is for using the view model object, and not sure how to explain it
    protected Task(Parcel in) {
        _id = in.readInt();
        task = in.readString();
        importance = in.readInt();
        urgency = in.readInt();
        completed = in.readByte() != 0;
    }

    // This function is for using with the view model object, and not sure how to explain it
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

    // GETTER FUNCTIONS

    public long getID() {return this._id;}

    public String getTask() {return this.task;}

    public int getImportance() {return this.importance;}

    public int getUrgency() {return this.urgency;}

    public boolean getCompleted() {return this.completed;}

    // VALIDATION & SANITATION FUNCTIONS

    // "Rating" is either "importance" or "urgency"
    public boolean validateRating(int rating) {return ((rating >= 0) && (rating <=100));}

    public boolean validateImportance(int importance) {return validateRating(importance);}

    public boolean validateUrgency(int urgency) {return validateRating(urgency);}

    private boolean sanitizeCompleted(int completed){return (completed >= 1);}

    // SETTER FUNCTIONS

    public void setID(long id) {this._id = id;}  // Should never be used

    public void setTask(String task) {this.task = task;}

    public void setImportance(int importance) {
        if (validateImportance(importance))
            this.importance = importance;
        else
            throw new IllegalArgumentException("Importance must be an integer between 0 and 100");
    }

    public void setUrgency(int urgency) {
        if (validateUrgency(urgency))
            this.urgency = urgency;
        else
            throw new IllegalArgumentException("Urgency must be an integer between 0 and 100");
    }

    public void setCompleted(int completed) {this.completed = sanitizeCompleted(completed);}

    public void setCompleted(boolean completed) {this.completed = completed;}

    // DEBUG ONLY FUNCTION

    public void debug() {
        System.out.println("CONTENTS OF Task INSTANCE:");
        System.out.println(getID());
        System.out.println(getTask());
        System.out.println(getImportance());
        System.out.println(getUrgency());
        System.out.println(getCompleted());
    }

    // VIEW MODEL FUNCTIONS

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(task);
        dest.writeInt(importance);
        dest.writeInt(urgency);
        dest.writeByte((byte) (completed ? 1 : 0));
    }
}
