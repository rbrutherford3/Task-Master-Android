package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

// Todo: possibly move from 0-100 selection to 0-10
// Todo: Permanent notification for most important and urgent item (maybe next release)

// Main activity that initializes all items, including database helpers, and sets listeners to trigger functions
public class MainActivity extends AppCompatActivity {

    // To track which fragments are visible
    private boolean fragmentAddOrEdit = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OPTION TO RESET DATABASE (DEBUG ONLY)
        //resetDatabase(true);

        // taskViewModel holds task information between views, activities, etc.
        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Get all the unfinished tasks for display
        taskViewModel.downloadIncompleteTasks();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Begin program with edit screen
        showHome();

        // Add a custom back button listener with simple switch indicating which screen starting from
        findViewById(R.id.main_activity).getRootView().setFocusableInTouchMode(true);
        findViewById(R.id.main_activity).getRootView().requestFocus();
        findViewById(R.id.main_activity).getRootView().setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (fragmentAddOrEdit) {
                    showHome();
                    return true;
                }
                return false;
            }
            return false;
        });
    }

    // FRAGMENT CONTROL METHODS

    // Show the "home" screen: task graphics, instructions, and button for adding/editing tasks
    public void showHome() {

        // Prepare transaction by clearing activity of frames and creating new ones
        ((ViewGroup) findViewById(R.id.main_activity)).removeAllViews();
        FragmentTaskDraw fragmentTaskDraw = FragmentTaskDraw.newInstance();
        FragmentAddButton fragmentAddButton = FragmentAddButton.newInstance();
        FragmentInstructions fragmentInstructions = FragmentInstructions.newInstance();

        // Begin transaction and attach fragments
        FragmentManager fragmentManager = Objects.requireNonNull(getSupportFragmentManager());
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_activity, fragmentTaskDraw);
        fragmentTransaction.add(R.id.main_activity, fragmentAddButton);
        fragmentTransaction.add(R.id.main_activity, fragmentInstructions);
        fragmentTransaction.commit();

        // Keep track of which fragments are showing
        fragmentAddOrEdit = false;
    }

    // Bring up a screen for a new task
    public void addTask() {
        FragmentTransaction fragmentTransaction = prepareTransaction();
        FragmentAddOrModifyTask fragmentAddOrModifyTask = newFragment();
        finalizeTransaction(fragmentTransaction, fragmentAddOrModifyTask);
    }

    // Bring up a screen for editing an existing task
    public void editTask(Task task) {
        FragmentTransaction fragmentTransaction = prepareTransaction();
        FragmentAddOrModifyTask fragmentAddOrModifyTask = newFragmentWithTask(task);
        finalizeTransaction(fragmentTransaction, fragmentAddOrModifyTask);
    }

    // PIECEMEAL PRIVATE METHODS FOR CHANGING FRAGMENTS (USED BY ABOVE METHODS)

    // Create transaction for the above methods
    private @NotNull FragmentTransaction prepareTransaction() {
        ((ViewGroup) findViewById(R.id.main_activity)).removeAllViews();
        FragmentManager fragmentManager = Objects.requireNonNull(getSupportFragmentManager());
        return fragmentManager.beginTransaction();
    }

    // Create fragment for adding a new task
    private @NotNull FragmentAddOrModifyTask newFragment() {
        return FragmentAddOrModifyTask.newInstance();
    }

    // Create fragment for editing an existing task
    private @NotNull FragmentAddOrModifyTask newFragmentWithTask(Task task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("editTask", task);
        FragmentAddOrModifyTask fragmentAddOrModifyTask = FragmentAddOrModifyTask.newInstance();
        fragmentAddOrModifyTask.setArguments(bundle);
        return fragmentAddOrModifyTask;
    }

    // Finish transaction for the above methods
    private void finalizeTransaction(@NotNull FragmentTransaction fragmentTransaction,
                                     FragmentAddOrModifyTask fragmentAddOrModifyTask) {
        fragmentTransaction.add(R.id.main_activity, fragmentAddOrModifyTask);
        fragmentTransaction.commit();
        fragmentAddOrEdit = true;
    }

    // DEBUGGING FUNCTIONS

    // Debugging function for checking system state
    public void seeResults(@NotNull ArrayList<Task> tasks) {
        // Display task details
        int count = 0;
        StringBuilder msg = new StringBuilder();
        for (Task task : tasks) {
            count++;
            msg.append(task.getLabel()).append(", urgency = ").append(task.getUrgency()).append(", importance = ").
                    append(task.getImportance()).append(System.getProperty("line.separator"));
        }
        if (count == 0) {
            msg.append("EMPTY DATABASE");
        }
        System.out.println(msg.toString());
    }

    // Debugging function to drop the database and start a new one
    private void resetDatabase(boolean dummyValues) {
        TaskDatabaseHelper taskDatabaseHelper = TaskDatabaseHelper.getInstance(this);
        taskDatabaseHelper.dropTable();
        taskDatabaseHelper.createTable();
        if (dummyValues) {
            Task test1 = new Task("This", 75, 75, false);
            Task test2 = new Task("is", 75, 25, false);
            Task test3 = new Task("a", 25, 25, false);
            Task test4 = new Task("test", 25, 75, false);
            Task test5 = new Task("don't", 50, 50, false);
            Task test6 = new Task("tread", 50, 50, false);
            Task test7 = new Task("on", 50, 50, false);
            Task test8 = new Task("me!", 50, 50, false);
            long id1 = taskDatabaseHelper.addNewTask(test1);
            long id2 = taskDatabaseHelper.addNewTask(test2);
            long id3 = taskDatabaseHelper.addNewTask(test3);
            long id4 = taskDatabaseHelper.addNewTask(test4);
            long id5 = taskDatabaseHelper.addNewTask(test5);
            long id6 = taskDatabaseHelper.addNewTask(test6);
            long id7 = taskDatabaseHelper.addNewTask(test7);
            long id8 = taskDatabaseHelper.addNewTask(test8);
            Task task1check = taskDatabaseHelper.getTask(id1);
            Task task2check = taskDatabaseHelper.getTask(id2);
            Task task3check = taskDatabaseHelper.getTask(id3);
            Task task4check = taskDatabaseHelper.getTask(id4);
            Task task5check = taskDatabaseHelper.getTask(id5);
            Task task6check = taskDatabaseHelper.getTask(id6);
            Task task7check = taskDatabaseHelper.getTask(id7);
            Task task8check = taskDatabaseHelper.getTask(id8);
        }
    }
}