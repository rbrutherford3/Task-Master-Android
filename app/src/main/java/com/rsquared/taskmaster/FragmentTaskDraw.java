package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rsquared.taskmaster.TaskViewModel.TaskGraphic;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Task Draw fragment (canvas with interactive task listings_
public class FragmentTaskDraw extends Fragment {

    // PRIVATE MEMBERS

    private TaskDraw taskDraw;              // "Canvas" view
    private TaskViewModel taskViewModel;    // For accessing and modifying task information

    // CONSTRUCTORS

    // Required empty public constructor
    public FragmentTaskDraw() {}

    // Provide an instance of this class
    public static FragmentTaskDraw newInstance() {
        return new FragmentTaskDraw();
    }

    // OVER-RIDDEN LIFECYCLE METHODS

    // Necessary override
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_draw, container, false);
    }

    // Most code executed here
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Place on user interface thread
        // (not sure why that's important, but this is the only way this code will work)
        view.post(() -> {

            // Initialize private members
            taskDraw = requireActivity().findViewById(R.id.task_draw);
            taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
            taskDraw.setDimensions(taskDraw.getWidth(), taskDraw.getHeight());
            taskDraw.setTaskViewModel(taskViewModel);   // Give canvas object access to the tasks

            // Create graphics information for each task and save
            for (Task task : taskViewModel.getTasks()) {
                TaskGraphic taskGraphic = taskDraw.setGraphics(task);
                taskViewModel.addTaskGraphic(task, taskGraphic);
            }
            taskDraw.invalidate();  // force a redraw

            final Context context = getContext();

            // Create responses to tap gestures on the screen
            final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

                // Apparently this override is necessary for the rest to be successful
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                    return true;
                }

                // Check/uncheck task with a single tap
                @Override
                public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                    Task touchedTask = getTouchedTask(motionEvent.getX(), motionEvent.getY());
                    if (touchedTask != null) {
                        touchedTask.setCompleted(!touchedTask.getCompleted());
                        taskViewModel.updateTask(touchedTask);
                        taskDraw.invalidate();
                    }
                    return true;
                }

                // Edit a task by double tapping it
                @Override
                public boolean onDoubleTap(MotionEvent motionEvent) {
                    Task touchedTask = getTouchedTask(motionEvent.getX(), motionEvent.getY());
                    if (touchedTask != null)
                        ((MainActivity) requireActivity()).editTask(touchedTask);
                    return true;
                }

                // Edit a task by pressing and holding the task, as well
                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    Task touchedTask = getTouchedTask(motionEvent.getX(), motionEvent.getY());
                    if (touchedTask != null)
                        ((MainActivity) requireActivity()).editTask(touchedTask);
                }
            };

            // Finalize the tap responses and pass along to the canvas object
            final GestureDetector detector = new GestureDetector(context, listener);
            detector.setOnDoubleTapListener(listener);
            detector.setIsLongpressEnabled(true);
            taskDraw.setOnTouchListener((View taskView, MotionEvent motionEvent) ->
                    detector.onTouchEvent(motionEvent));
        });
    }

    // Todo: handle multiple tasks in same location
    // Method to retrieve a task from touch coordinates (only the first one if multiple items)
    Task getTouchedTask(float x, float y) {
        TaskDraw taskDraw = requireActivity().findViewById(R.id.task_draw);
        ArrayList<Task> touchedTasks = taskDraw.getTouchedTasks(x, y);
        if (touchedTasks.isEmpty())
            return null;
        else
            return touchedTasks.get(0);
    }
}