package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.view.View.VISIBLE;
import static com.rsquared.taskmaster.R.id.*;

// Todo: possibly move from 0-100 selection to 0-10
// Todo: Permanent notification for most important and urgent item (maybe next release)

// Main activity that initializes all items, including database helpers, and sets listeners to
// trigger functions
public class MainActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // INITIALIZE VARIABLES

        // Grab layouts and inputs
        final View constraintLayout = findViewById(constraint_layout);
        final View frameLayout = findViewById(frame_layout);
        final View linearLayoutPopup = findViewById(linear_layout_popup);
        final TextView textViewPopup = findViewById(text_view_popup);
        final TaskDraw taskDraw = findViewById(task_draw);
        final CheckBox checkBoxPopup = findViewById(check_box_popup);
        final EditText editTextTask = findViewById(edit_text_task);
        final int editTextTaskHintColor = editTextTask.getCurrentHintTextColor();
        final SeekBar seekBarImportance = findViewById(seek_bar_importance);
        final SeekBar seekBarUrgency = findViewById(seek_bar_urgency);
        final Button submitButton = findViewById(button_submit_new_task);

        // taskViewModel holds task information between views, activities, etc.
        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Create database helper
        TaskDatabaseHelper taskDatabaseHelper = new TaskDatabaseHelper(this);

        // OPTION TO RESET DATABASE (DEBUG ONLY)
        //resetDatabase(taskDatabaseHelper);

        // Grab information from the database
        taskDatabaseHelper.getTasks(taskViewModel, true);

        // Pass the taskViewModel object along to the TaskDraw View (Views cannot create an instance)
        taskDraw.setTaskViewModel(taskViewModel);

        // SET UP TOUCH LISTENERS & BACKGROUND

        // Wait until dimensions of the screen element are recorded to initialize background drawing
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Revert back from listening to not listening (i.e.: only run this code once)
                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get dimensions
                taskDraw.setDimensions(frameLayout.getWidth(), frameLayout.getHeight());

                // Add canvas geometry values for each task
                taskDraw.initTasks();
                taskDraw.invalidate();

                // Set background to red:yellow:green gradient
                taskDraw.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_gradient, null));
            }
        });

        // ADD TOUCH RESPONSE FOR POPUP ON BACKGROUND CANVAS

        // Set up touch response for canvas (with or without a task popup showing)
        taskDraw.setOnTouchListener((View v, MotionEvent event) -> {

            // Only for down pushes
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                // Get coordinates of touch
                float x = event.getX();
                float y = event.getY();

                // If there is a popup, see if the touch occurred outside the popup and close it
                if (checkPopup(x, y))
                    return true;
                else {
                    // Use function in TaskDraw to get all tasks that may coincide with the tap coordinates
                    // Todo: move function from TaskDraw?
                    ArrayList<Task> touchedTasks = taskDraw.touchedTasks(x, y);

                    // Grab the first task found
                    // Todo: modify code to support multiple tasks close together
                    if (touchedTasks.size() > 0) {

                        // Display task in popup
                        textViewPopup.setText(touchedTasks.get(0).getTask());

                        // Apply graphic elements to popup, then show popup
                        GradientDrawable border = new GradientDrawable();
                        // Get background color at task location
                        border.setColor(Color.GRAY);
                        border.setStroke(1, 0xFF000000); //black border with full opacity
                        linearLayoutPopup.setBackground(border);    // Apply border and coloring to popup
                        taskViewModel.setCompletedTask(touchedTasks.get(0)); // Store task selection for processing
                        linearLayoutPopup.setVisibility(VISIBLE); // Show popup
                        return true;
                    } else
                        return false;
                }
            } else
                return false;
        });

        // ADD TOUCH RESPONSE FOR POPUP EVERYWHERE ELSE
        // Todo: check if this is still necessary

        // Remove task popup if clicked elsewhere
        constraintLayout.setOnTouchListener((View v, MotionEvent event) -> {

            // Only for down pushes
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                // Get coordinates of touch
                float x = event.getX();
                float y = event.getY();

                // If there is a popup, see if the touch occurred outside the popup and close it
                return checkPopup(x, y);
            } else
                return false;
        });

        // ADD TOUCH RESPONSE FOR COMPLETION CHECKBOX IN TASK POPUP
        // Todo: check if this is still necessary

        // Whenever the checkbox inside the popup is touched...
        checkBoxPopup.setOnClickListener((View view) -> {

            // Update the task held in locally saved data and also database
            Task completedTask = taskViewModel.getCompletedTask();
            completedTask.setCompleted(true);
            taskDatabaseHelper.updateTask(completedTask);

            // Hide popup, reset checkbox, force redraw
            linearLayoutPopup.setVisibility(View.INVISIBLE);
            checkBoxPopup.setChecked(false);
            taskDraw.invalidate();
        });

        // ADD NEW TASKS TO VIEW MODEL AND DATABASE AND SHOW ON BACKGROUND

        // When user submits information for a new task...
        submitButton.setOnTouchListener((v, event) -> {

            // Only for down pushes
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                linearLayoutPopup.setVisibility(View.INVISIBLE);    // Hide popup

                // Get text entered by user for the task
                String textTask = editTextTask.getText().toString();

                // Set hint color to red if nothing entered (for user feedback)
                if (textTask.isEmpty())
                    editTextTask.setHintTextColor(Color.RED);
                else {
                    editTextTask.setHintTextColor(editTextTaskHintColor); // Reset hint color

                    // Create task based on user input
                    Task newTask = new Task(editTextTask.getText().toString(), seekBarImportance.getProgress(),
                            seekBarUrgency.getProgress(), false);

                    // Add task to database
                    long id = taskDatabaseHelper.addTask(newTask);
                    newTask.setID(id);

                    // Add that task to the view model
                    taskViewModel.addTask(newTask);

                    // Get task graphic information
                    taskDraw.initTask(newTask);

                    // Clear the inputs for the next task to be entered
                    resetInputs();

                    // Force redraw
                    taskDraw.invalidate();
                }
            }
            return true;
        });

        // When focusing on any of the inputs for adding new items, render popup invisible
        editTextTask.setOnTouchListener((v, event) -> checkPopup(event.getX(), event.getY()));
        seekBarImportance.setOnTouchListener((v, event) -> checkPopup(event.getX(), event.getY()));
        seekBarUrgency.setOnTouchListener((v, event) -> checkPopup(event.getX(), event.getY()));
    }

    // Function to close popup if a press occurs outside of the popup location
    private boolean checkPopup(float x, float y) {
        View linearLayoutPopup = findViewById(linear_layout_popup);
        if (linearLayoutPopup.getVisibility() == VISIBLE) {
            if ((linearLayoutPopup.getLeft() <= x) && (x <= linearLayoutPopup.getRight()) &&
                    (linearLayoutPopup.getTop() <= y) && (y <= linearLayoutPopup.getBottom()))
                return false;
            else {
                linearLayoutPopup.setVisibility(View.INVISIBLE);
                return true;
            }
        } else
            return false;
    }

    // Clear all inputs so a new task may be entered
    private void resetInputs() {
        EditText editTextTask = findViewById(edit_text_task);
        SeekBar seekBarImportance = findViewById(seek_bar_importance);
        SeekBar seekBarUrgency = findViewById(seek_bar_urgency);

        editTextTask.setText("");
        seekBarImportance.setProgress(50);
        seekBarUrgency.setProgress(50);
    }

    // Debugging function for checking system state
    public void seeResults(@NotNull ArrayList<Task> tasks) {
        // Display task details
        int count = 0;
        StringBuilder msg = new StringBuilder();
        for (Task task : tasks) {
            count++;
            msg.append(task.getTask()).append(", urgency = ").append(task.getUrgency()).append(", importance = ").
                    append(task.getImportance()).append(System.getProperty("line.separator"));
        }
        if (count == 0) {
            msg.append("EMPTY DATABASE");
        }
        System.out.println(msg.toString());
    }

    // Debugging function to drop the database and start a new one
    private void resetDatabase(@NotNull TaskDatabaseHelper taskDatabaseHelper) {
        taskDatabaseHelper.dropTable();
        taskDatabaseHelper.createTable();
        Task test1 = new Task("There is a hole", 50, 50, false);
        Task test2 = new Task("in my soul", 0, 0, false);
        long id1 = taskDatabaseHelper.addTask(test1);
        long id2 = taskDatabaseHelper.addTask(test2);
        Task task1check = taskDatabaseHelper.getTask(id1);
        Task task2check = taskDatabaseHelper.getTask(id2);
        task1check.debug();
        task2check.debug();
        finish();
        System.exit(0);
    }
}