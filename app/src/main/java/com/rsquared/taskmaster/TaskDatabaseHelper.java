package com.rsquared.taskmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.rsquared.taskmaster.TaskDatabaseContract.DATABASE_NAME;
import static com.rsquared.taskmaster.TaskDatabaseContract.DATABASE_VERSION;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.COLUMN_NAME_COMPLETED;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.COLUMN_NAME_IMPORTANCE;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.COLUMN_NAME_TASK;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.COLUMN_NAME_URGENCY;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.CREATE_TABLE;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.DROP_TABLE;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.ID_CLAUSE;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.QUERY_TABLE;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.QUERY_TABLE_INCOMPLETE;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.TABLE_NAME;

// This class serves as the entry point to the database in activities, using info from TaskDatabaseContract
public class TaskDatabaseHelper extends SQLiteOpenHelper {

    // CONSTRUCTORS

    // Use singleton method to provide only one instance of a database helper at a time
    private static TaskDatabaseHelper singletonTaskDatabaseHelper;

    public static synchronized TaskDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you don't accidentally leak an Activity's context.
        if (singletonTaskDatabaseHelper == null)
            singletonTaskDatabaseHelper = new TaskDatabaseHelper(context.getApplicationContext());
        return singletonTaskDatabaseHelper;
    }

    // If you change the database schema, you must increment the database version.
    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // DATABASE MANIPULATION METHODS (add/drop table, etc)

    // Delete the data (be careful!)
    public void dropTable() {
        getWritableDatabase().execSQL(DROP_TABLE);
    }

    // Set up the database (for initial use only)
    public void createTable() {
        getWritableDatabase().execSQL(CREATE_TABLE);
    }

    // OVER-RIDDEN METHODS

    @Override
    public void onCreate(@NotNull SQLiteDatabase database) {
        createTable();
    }

    @Override
    public void onUpgrade(@NotNull SQLiteDatabase database, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        dropTable();
        onCreate(database);
    }

    @Override
    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
    }

    // GETTER METHODS

    // Get a particular task (using id) from the database
    public Task getTask(long id) {

        // Get database
        SQLiteDatabase database = getReadableDatabase();

        // Setup up query
        Cursor cursor = database.rawQuery(Table.getTaskQuery(id), null);
        Task task;

        // Save the information from the first query hit into the task to be returned
        if (cursor.moveToFirst()) {
            long ID = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TASK));
            int importance = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMPORTANCE));
            int urgency = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_URGENCY));
            boolean completed = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) >= 1);
            task = new Task(ID, taskName, importance, urgency, completed);
        } else {
            task = null;
        }

        // Close database connection
        cursor.close();

        // Return task object compiled from database entry
        return task;
    }

    // Retrieve all tasks from the database that have yet to be completed
    public ArrayList<Task> getTasks(boolean incompleteTasksOnly) {

        // Get database
        SQLiteDatabase database = getReadableDatabase();

        // Initialize task list return object
        ArrayList<Task> tasks = new ArrayList<>();

        // Set up query (either all tasks are donwloaded or only incomplete tasks)
        Cursor cursor;
        if (incompleteTasksOnly)
            cursor = database.rawQuery(QUERY_TABLE_INCOMPLETE, null);
        else
            cursor = database.rawQuery(QUERY_TABLE, null);

        // Pull information from database and store
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long ID = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TASK));
                int importance = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMPORTANCE));
                int urgency = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_URGENCY));
                boolean completed = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) >= 1);
                tasks.add(new Task(ID, taskName, importance, urgency, completed));
                cursor.moveToNext();
            }
        }

        // Close database connection
        cursor.close();

        // Return query results
        return tasks;
    }

    // SETTER METHODS

    // Add a new task to the database
    public long addNewTask(@NotNull Task newTask) {

        // Get the database
        SQLiteDatabase database = getWritableDatabase();

        // Create insert entries
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TASK, newTask.getTask());
        values.put(COLUMN_NAME_IMPORTANCE, newTask.getImportance());
        values.put(COLUMN_NAME_URGENCY, newTask.getUrgency());
        values.put(COLUMN_NAME_COMPLETED, newTask.getCompleted());

        // Insert the new row, returning the primary key value of the new row
        return database.insert(
                TABLE_NAME,
                null,
                values);
    }

    // Update a task's information (used to modify task and also mark complete/incomplete
    public void updateTask(@NotNull Task task) {

        // Get the database
        SQLiteDatabase database = getWritableDatabase();

        // Match fields to new information
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TASK, task.getTask());
        values.put(COLUMN_NAME_IMPORTANCE, task.getImportance());
        values.put(COLUMN_NAME_URGENCY, task.getUrgency());
        values.put(COLUMN_NAME_COMPLETED, task.getCompleted());

        // Update the database
        database.update(TABLE_NAME, values, ID_CLAUSE, new String[]{Long.toString(task.getID())});
    }
}