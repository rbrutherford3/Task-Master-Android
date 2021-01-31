package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Task Draw fragment (canvas with interactive task listings, plus a popup, if necessary)
public class FragmentTaskDraw extends Fragment {

    // PRIVATE MEMBERS

    private TaskDraw taskDraw;              // "Canvas" view
    private GroupPopup groupPopup;          // Pop up element for groups of tasks
    private ImageView popupBackground;      // Object for drawing pop up background
    private TaskViewModel taskViewModel;    // For accessing and modifying task information

    // CONSTRUCTORS

    // Required empty public constructor
    public FragmentTaskDraw() {
    }

    // Provide an instance of this class
    @Contract(" -> new")
    public static @NotNull FragmentTaskDraw newInstance() {
        return new FragmentTaskDraw();
    }

    // OVER-RIDDEN LIFECYCLE METHODS

    // Necessary override
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

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
            groupPopup = requireActivity().findViewById(R.id.group_popup);
            popupBackground = requireActivity().findViewById(R.id.popup_background);
            taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
            taskViewModel.deGroupTasks();
            taskDraw.initialize(taskViewModel, taskDraw.getWidth(), taskDraw.getHeight());

            final Context context = getContext();

            // TAP LISTENER FOR THE TASK DRAW CANVAS AND THE TASKS THEREIN

            // Create responses to tap gestures on the task layout (not the group popup)
            final SimpleOnGestureListener taskDrawListener = new SimpleOnGestureListener() {

                // Apparently this override is necessary for the rest to be successful
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

                    // If in popup mode, hide the popup
                    if (groupPopup.getVisibility() == View.VISIBLE) {
                        groupPopup.setVisibility(View.INVISIBLE);
                        popupBackground.setVisibility(View.INVISIBLE);
                        return true;
                    }

                    // Get touched items
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    TaskGroup touchedTaskGroup = taskDraw.getTouchedTaskGroup(x, y);
                    Task touchedTask = taskDraw.getTouchedTask(x, y);

                    // If tapping a task, then check/uncheck the test
                    if (touchedTask != null) {
                        touchedTask.setCompleted(!touchedTask.getCompleted());
                        taskViewModel.updateTask(touchedTask);
                    }

                    // If tapping a group, then show the group popup
                    else if (touchedTaskGroup != null) {
                        groupPopup.initialize(touchedTaskGroup);
                        popupBackground.setVisibility(View.VISIBLE);
                        groupPopup.setVisibility(View.VISIBLE);
                        groupPopup.invalidate();
                    }

                    taskDraw.invalidate();  // update image
                    return true;    // necessary to prevent further action resulting from tap
                }

                // Edit a task by double tapping it
                @Override
                public boolean onDoubleTap(MotionEvent motionEvent) {
                    return editTask(motionEvent);
                }

                // Edit a task by pressing and holding the task, as well
                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    editTask(motionEvent);
                }

                // If editing an existing task, then pull up the edit task fragment
                private boolean editTask(@NotNull MotionEvent motionEvent) {
                    Task touchedTask = taskDraw.getTouchedTask(motionEvent.getX(), motionEvent.getY());

                    if (touchedTask != null) {
                        ((MainActivity) requireActivity()).editTask(touchedTask);
                    }
                    return true;
                }
            };

            // TAP LISTENER FOR THE GROUP POPUP CANVAS AND THE TASKS THEREIN

            // Create responses to tap gestures on the screen
            final SimpleOnGestureListener groupPopupListener = new SimpleOnGestureListener() {

                // Apparently this override is necessary for the rest to be successful
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                    return true;
                }

                // Interpret single tap anywhere on the screen
                @Override
                public boolean onSingleTapConfirmed(@NotNull MotionEvent motionEvent) {
                    // If in popup mode, set task completed if tapped, otherwise hide the popup
                    Task touchedTask = groupPopup.getTouchedTask(motionEvent.getX(), motionEvent.getY());
                    if (touchedTask != null) {
                        touchedTask.setCompleted(!touchedTask.getCompleted());
                        taskViewModel.updateTask(touchedTask);
                        groupPopup.invalidate();
                    }
                    return true;
                }

                // Edit a task by double tapping it
                @Override
                public boolean onDoubleTap(MotionEvent motionEvent) {
                    return editTask(motionEvent);
                }

                // Edit a task by pressing and holding the task, as well
                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    editTask(motionEvent);
                }

                private boolean editTask(@NotNull MotionEvent motionEvent) {
                    Task touchedTask = groupPopup.getTouchedTask(motionEvent.getX(), motionEvent.getY());

                    if (touchedTask != null) {
                        ((MainActivity) requireActivity()).editTask(touchedTask);
                    }
                    return true;
                }
            };

            // Finalize tap responses for the task draw canvas and tie them with the view objects
            final GestureDetector taskDrawDetector = new GestureDetector(context, taskDrawListener);
            taskDrawDetector.setOnDoubleTapListener(taskDrawListener);
            taskDrawDetector.setIsLongpressEnabled(true);
            taskDraw.setOnTouchListener((View taskView, MotionEvent motionEvent) ->
                    taskDrawDetector.onTouchEvent(motionEvent));

            // Finalize tap responses for the group popup canvas and tie them with the view objects
            final GestureDetector popUpDetector = new GestureDetector(context, groupPopupListener);
            popUpDetector.setOnDoubleTapListener(groupPopupListener);
            popUpDetector.setIsLongpressEnabled(true);
            groupPopup.setOnTouchListener((View taskView, MotionEvent motionEvent) ->
                    popUpDetector.onTouchEvent(motionEvent));
        });
    }
}