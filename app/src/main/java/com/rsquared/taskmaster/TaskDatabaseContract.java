package com.rsquared.taskmaster;

import android.provider.BaseColumns;

// Defines all the information necessary for the database access
public final class TaskDatabaseContract {

  // Define basic database information, types, and syntax
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "TaskMaster.db";
  private static final String TEXT_TYPE = " TEXT";
  private static final String INT_TYPE = " INTEGER";
  private static final String COMMA_SEP = ",";

  // To prevent someone from accidentally instantiating the contract class,
  // give it an empty constructor.
  private TaskDatabaseContract() {
    // Purposefully empty
  }

  // Inner class to define the actual creation/destruction queries
  public abstract static class Table implements BaseColumns {

    // Define table and column names
    public static final String TABLE_NAME = "tasks";
    public static final String COLUMN_NAME_TASK = "task";
    public static final String COLUMN_NAME_IMPORTANCE = "importance";
    public static final String COLUMN_NAME_URGENCY = "urgency";
    public static final String COLUMN_NAME_COMPLETED = "completed";

    // Define table creation query
    public static final String CREATE_TABLE =
        "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + _ID
            + INT_TYPE
            + " PRIMARY KEY,"
            + COLUMN_NAME_TASK
            + TEXT_TYPE
            + COMMA_SEP
            + COLUMN_NAME_IMPORTANCE
            + INT_TYPE
            + COMMA_SEP
            + COLUMN_NAME_URGENCY
            + INT_TYPE
            + COMMA_SEP
            + COLUMN_NAME_COMPLETED
            + INT_TYPE
            + " )";

    // Define table destruction query
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    // Get all tasks
    public static final String QUERY_TABLE = "SELECT * FROM " + TABLE_NAME;

    // Get only incomplete tasks
    public static final String QUERY_TABLE_INCOMPLETE =
        "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_COMPLETED + "=0";

    public static final String ID_CLAUSE = _ID + "=?";
  }
}
