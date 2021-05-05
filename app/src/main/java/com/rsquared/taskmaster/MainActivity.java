package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// Todo: possibly move from 0-100 selection to 0-10
// Todo: Permanent notification for most important and urgent item (maybe next release)

// Main activity that initializes all items, including database helpers, and sets listeners to
// trigger functions
public class MainActivity extends AppCompatActivity {

  // To track which fragments are visible
  private boolean fragmentAddOrEdit = false;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
    findViewById(R.id.linear_layout_activity_main).getRootView().setFocusableInTouchMode(true);
    findViewById(R.id.linear_layout_activity_main).getRootView().requestFocus();
    findViewById(R.id.linear_layout_activity_main)
        .getRootView()
        .setOnKeyListener(
            (View v, int keyCode, KeyEvent event) -> {
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
    FragmentTransaction fragmentTransaction = prepareTransaction();

    // Create new fragment instances
    FragmentTaskDraw fragmentTaskDraw = FragmentTaskDraw.newInstance();
    FragmentAddButton fragmentAddButton = FragmentAddButton.newInstance();
    FragmentInstructions fragmentInstructions = FragmentInstructions.newInstance();

    // Keep track of which fragments are showing
    fragmentAddOrEdit = false;

    finalizeTransaction(
        fragmentTransaction, fragmentTaskDraw, fragmentAddButton, fragmentInstructions);
  }

  // Bring up a screen for a new task
  public void addTask() {
    FragmentTransaction fragmentTransaction = prepareTransaction();
    FragmentAddOrModifyTask fragmentAddOrModifyTask = newFragmentAddTask();
    fragmentAddOrEdit = true;
    finalizeTransaction(fragmentTransaction, fragmentAddOrModifyTask);
  }

  // Bring up a screen for editing an existing task
  public void editTask(Task task) {
    FragmentTransaction fragmentTransaction = prepareTransaction();
    FragmentAddOrModifyTask fragmentAddOrModifyTask = newFragmentModifyTask(task);
    fragmentAddOrEdit = true;
    finalizeTransaction(fragmentTransaction, fragmentAddOrModifyTask);
  }

  // PIECEMEAL PRIVATE METHODS FOR CHANGING FRAGMENTS (USED BY ABOVE METHODS)

  // Create fragment for adding a new task
  private @NotNull FragmentAddOrModifyTask newFragmentAddTask() {
    Bundle bundle = new Bundle();
    bundle.putByte("isNewTask", (byte) 1);
    FragmentAddOrModifyTask fragmentAddOrModifyTask = FragmentAddOrModifyTask.newInstance();
    fragmentAddOrModifyTask.setArguments(bundle);
    return fragmentAddOrModifyTask;
  }

  // Create fragment for editing an existing task
  private @NotNull FragmentAddOrModifyTask newFragmentModifyTask(Task task) {
    Bundle bundle = new Bundle();
    bundle.putByte("isNewTask", (byte) 0);
    bundle.putParcelable("editTask", (Parcelable) task);
    FragmentAddOrModifyTask fragmentAddOrModifyTask = FragmentAddOrModifyTask.newInstance();
    fragmentAddOrModifyTask.setArguments(bundle);
    return fragmentAddOrModifyTask;
  }

  // Create transaction for the above methods
  private @NotNull FragmentTransaction prepareTransaction() {
    ((ViewGroup) findViewById(R.id.linear_layout_placeholder)).removeAllViews();
    FragmentManager fragmentManager = Objects.requireNonNull(getSupportFragmentManager());
    return fragmentManager.beginTransaction();
  }

  // Finish transaction for the above methods
  private void finalizeTransaction(
      @NotNull FragmentTransaction fragmentTransaction, Fragment @NotNull ... fragments) {
    for (Fragment fragment : fragments) {
      fragmentTransaction.add(R.id.linear_layout_placeholder, fragment);
    }
    fragmentTransaction.commit();
  }
}
