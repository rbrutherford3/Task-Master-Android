package com.rsquared.taskmaster;

import static android.provider.BaseColumns._ID;
import static com.rsquared.taskmaster.TaskDatabaseContract.DATABASE_NAME;
import static com.rsquared.taskmaster.TaskDatabaseContract.DATABASE_VERSION;
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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

// This class serves as the entry point to the database in activities, using info from
// TaskDatabaseContract
public class TaskDatabaseHelper extends SQLiteOpenHelper {

  // CONSTRUCTORS

  // Use singleton method to provide only one instance of a database helper at a time
  private static TaskDatabaseHelper singletonTaskDatabaseHelper;

  // If you change the database schema, you must increment the database version.
  public TaskDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public static synchronized TaskDatabaseHelper getInstance(Context context) {
    // Use the application context, which will ensure that you don't accidentally leak an Activity's
    // context.
    if (singletonTaskDatabaseHelper == null) {
      singletonTaskDatabaseHelper = new TaskDatabaseHelper(context.getApplicationContext());
    }
    return singletonTaskDatabaseHelper;
  }

  // DATABASE MANIPULATION METHODS (add/drop table, etc)

  // Delete the data (be careful!)
  public void dropTable() {
    getWritableDatabase().execSQL(DROP_TABLE);
  }

  // OVER-RIDDEN METHODS

  @Override
  public void onCreate(@NotNull SQLiteDatabase database) {
    database.execSQL(CREATE_TABLE);
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

  // Retrieve all tasks from the database that have yet to be completed
  public Set<Task> getTasks(boolean incompleteTasksOnly) {

    // Get database
    SQLiteDatabase database = getReadableDatabase();

    // Initialize task list return object
    Set<Task> tasks = new HashSet<>();

    // Set up query (either all tasks are downloaded or only incomplete tasks)
    Cursor cursor;
    if (incompleteTasksOnly) {
      cursor = database.rawQuery(QUERY_TABLE_INCOMPLETE, null);
    } else {
      cursor = database.rawQuery(QUERY_TABLE, null);
    }

    // Pull information from database and store
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
        String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TASK));
        int urgency = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_URGENCY));
        int importance = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMPORTANCE));
        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) >= 1;
        tasks.add(new Task(id, taskName, urgency, importance, completed));
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
  public void addTask(@NotNull Task newTask) {

    // Get the database
    SQLiteDatabase database = getWritableDatabase();

    // Create insert entries
    ContentValues values = new ContentValues();
    values.put(COLUMN_NAME_TASK, newTask.getLabel());
    values.put(COLUMN_NAME_URGENCY, newTask.getUrgency());
    values.put(COLUMN_NAME_IMPORTANCE, newTask.getImportance());
    values.put(COLUMN_NAME_COMPLETED, newTask.getCompleted());

    // Insert the new row, returning the primary key value of the new row
	  database.insert(TABLE_NAME, null, values);
  }

  // Update a task's information (used to modify task and also mark complete/incomplete
  public void updateTask(@NotNull Task task) {

    // Get the database
    SQLiteDatabase database = getWritableDatabase();

    // Match fields to new information
    ContentValues values = new ContentValues();
    values.put(COLUMN_NAME_TASK, task.getLabel());
    values.put(COLUMN_NAME_URGENCY, task.getUrgency());
    values.put(COLUMN_NAME_IMPORTANCE, task.getImportance());
    values.put(COLUMN_NAME_COMPLETED, task.getCompleted());

    // Update the database
    database.update(TABLE_NAME, values, ID_CLAUSE, new String[] {Long.toString(task.getID())});
  }
}
