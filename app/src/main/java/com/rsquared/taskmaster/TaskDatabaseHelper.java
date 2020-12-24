package com.rsquared.taskmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import static android.provider.BaseColumns._ID;
import static com.rsquared.taskmaster.TaskDatabaseContract.*;
import static com.rsquared.taskmaster.TaskDatabaseContract.Table.*;

// This class serves as the entry point to the database in activities, using info from TaskDatabaseContract
// Todo: modify 'update' function to use TaskDatabaseContract update function
public class TaskDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // Add a new task to the database
    public long addTask(@NotNull Task newTask) {

        // Get the database. If it does not exist, this is where it will
        // also be created.
        SQLiteDatabase dbw = getWritableDatabase();

        // Create insert entries
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TASK, newTask.getTask());
        values.put(COLUMN_NAME_IMPORTANCE, newTask.getImportance());
        values.put(COLUMN_NAME_URGENCY, newTask.getUrgency());
        values.put(COLUMN_NAME_COMPLETED, newTask.getCompleted());

        // Insert the new row, returning the primary key value of the new row
        return dbw.insert(
                TABLE_NAME,
                null,
                values);
    }

    // Update a task as 'COMPLETED' after pressing check mark
    public void updateTask(@NotNull Task task, boolean completed) {

        SQLiteDatabase dbw = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_COMPLETED, completed);

        dbw.update(TABLE_NAME, values, ID_CLAUSE, new String[]{Long.toString(task.getID())});
    }

    // Get a particular task (using id) in the database
    public Task getTask(long id) {
        SQLiteDatabase dbr = getReadableDatabase();
        Cursor cursor = dbr.rawQuery(Table.getTask(id), null);
        Task task;
        if (cursor.moveToFirst()) {
            long ID = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TASK));
            int importance = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMPORTANCE));
            int urgency = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_URGENCY));
            boolean completed = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) >= 1);
            task = new Task((long) ID, taskName, importance, urgency, completed);
        }
        else {
            task = null;
        }
        cursor.close();
        return task;
    }

    // Retrieve all tasks from the database that have yet to be completed
    public void getTasks(TaskViewModel taskViewModel, boolean incompleteTasksOnly) {
        SQLiteDatabase dbr = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        Cursor cursor;
        if (incompleteTasksOnly)
            cursor = dbr.rawQuery(QUERY_TABLE_INCOMPLETE,null);
        else
            cursor = dbr.rawQuery(QUERY_TABLE, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long ID = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TASK));
                int importance = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMPORTANCE));
                int urgency = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_URGENCY));
                boolean completed = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) >= 1);
                taskViewModel.addTask(new Task((long)ID, taskName, importance, urgency, completed));
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

    // Delete the data (be careful!)
    public void dropTable() {
        getWritableDatabase().execSQL(DROP_TABLE);
    }

    // Set up the database (for initial use only)
    public void createTable() {
        getWritableDatabase().execSQL(CREATE_TABLE);
    }
}