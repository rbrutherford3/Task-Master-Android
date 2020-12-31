package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

// Fragment for creating or modifying a task
public class FragmentAddOrModifyTask extends Fragment implements Parcelable {

    // PRIVATE MEMBERS

    // Input objects:
    EditText editTextTask;
    int editTextTaskHintColor;
    SeekBar seekBarImportance;
    SeekBar seekBarUrgency;
    Button buttonSubmitNewTask;

    // Get new instance of taskViewModel to save information (Source of task information)
    private TaskViewModel taskViewModel;

    boolean newTask;    // new or existing task?
    Task existingTask;  // task to be modified, if editing

    // CONSTRUCTORS AND PARCEL-RELATED FUNCTIONS

    // Receive existing task as a parcel
    protected FragmentAddOrModifyTask(Parcel in) {
        existingTask = in.readParcelable(Task.class.getClassLoader());
    }

    // Creator method
    public static final Creator<FragmentAddOrModifyTask> CREATOR = new Creator<FragmentAddOrModifyTask>() {
        @Override
        public FragmentAddOrModifyTask createFromParcel(Parcel in) {
            return new FragmentAddOrModifyTask(in);
        }

        @Override
        public FragmentAddOrModifyTask[] newArray(int size) {
            return new FragmentAddOrModifyTask[size];
        }
    };

    // Provide an instance of this class
    public static FragmentAddOrModifyTask newInstance() {
        return new FragmentAddOrModifyTask();
    }

    // Required empty public constructor
    public FragmentAddOrModifyTask() {
    }

    // Differentiate between adding new task and editing existing task
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null)
            newTask = true;
        else {
            existingTask = getArguments().getParcelable("editTask");
            newTask = false;
        }
    }

    // Inflate layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_or_modify_task, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize taskViewModel for getting and saving task data
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Grab inputs
        editTextTask = requireActivity().findViewById(R.id.edit_text_task);
        editTextTaskHintColor = editTextTask.getCurrentHintTextColor();
        seekBarImportance = requireActivity().findViewById(R.id.seek_bar_importance);
        seekBarUrgency = requireActivity().findViewById(R.id.seek_bar_urgency);
        buttonSubmitNewTask = requireActivity().findViewById(R.id.button_submit_new_task);

        // On submission...
        buttonSubmitNewTask.setOnClickListener(l -> {
            // Get text entered by user for the task
            String textTask = editTextTask.getText().toString();

            // Set hint color to red if nothing entered (for user feedback)
            if (textTask.isEmpty())
                editTextTask.setHintTextColor(Color.RED);
            else {
                if (newTask) {

                    // Create task based on user input
                    Task newTask = new Task(editTextTask.getText().toString(), seekBarImportance.getProgress(),
                            seekBarUrgency.getProgress(), false);

                    // Add that task to the view model (which will update the database)
                    taskViewModel.addNewTask(newTask);
                } else {

                    // Update existing task with new values from inputs
                    existingTask.setTask(editTextTask.getText().toString());
                    existingTask.setImportance(seekBarImportance.getProgress());
                    existingTask.setUrgency(seekBarUrgency.getProgress());

                    // Commit changes to ViewModel and database
                    taskViewModel.updateTask(existingTask);
                    //existingTask = null;
                }

                // Go back to "home" screen to view tasks
                ((MainActivity) requireActivity()).showHome();
            }
        });

        // If existing task was passed, then change the inputs to reflect current values
        if (!newTask) {
            ((EditText) requireActivity().findViewById(R.id.edit_text_task)).setText(existingTask.getTask());
            ((SeekBar) requireActivity().findViewById(R.id.seek_bar_importance)).setProgress(existingTask.getImportance());
            ((SeekBar) requireActivity().findViewById(R.id.seek_bar_urgency)).setProgress(existingTask.getUrgency());
        }
    }

    // ADDITIONAL PARCEL FUNCTIONS

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(existingTask, flags);
    }
}