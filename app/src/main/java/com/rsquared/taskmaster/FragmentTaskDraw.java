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
            taskDraw.initialize(taskViewModel, taskDraw.getWidth(), taskDraw.getHeight());

            final Context context = getContext();

            // Create responses to tap gestures on the screen
            final SimpleOnGestureListener listener = new SimpleOnGestureListener() {

                // Apparently this override is necessary for the rest to be successful
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                    return true;
                }

                // Interpret single tap anywhere on the screen
                @Override
                public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

                    // If in popup mode, set task completed if tapped, otherwise hide the popup
                    if (groupPopup.getVisibility() == View.VISIBLE) {
                        Task touchedTask = getTouchedTaskItemPopup(motionEvent.getX(), motionEvent.getY());
                        if (touchedTask == null) {
                            groupPopup.setVisibility(View.INVISIBLE);
                            popupBackground.setVisibility(View.INVISIBLE);
                        } else {
                            touchedTask.setCompleted(!touchedTask.getCompleted());
                            groupPopup.invalidate();
                        }
                    }

                    // If not in popup mode, mark tapped task as completed/incomplete
                    // Create popup if a group is tapped
                    else {
                        TaskItem touchedTaskItem = getTouchedTaskItem(motionEvent.getX(), motionEvent.getY());
                        if (touchedTaskItem != null) {
                            if (touchedTaskItem instanceof Task) {
                                ((Task) touchedTaskItem).setCompleted(!((Task) touchedTaskItem).getCompleted());
                                taskViewModel.updateTask((Task) touchedTaskItem);
                            } else {
                                groupPopup.initialize((TaskGroup) touchedTaskItem);
                                popupBackground.setVisibility(View.VISIBLE);
                                groupPopup.setVisibility(View.VISIBLE);
                            }
                        }
                        taskDraw.invalidate();  // update image
                    }
                    return true;    // necessary to prevent further action resulting from tap
                }

                // Edit a task by double tapping it
                @Override
                public boolean onDoubleTap(MotionEvent motionEvent) {
                    TaskItem touchedTaskItem;
                    if (groupPopup.getVisibility() == View.VISIBLE) {
                        touchedTaskItem = getTouchedTaskItemPopup(motionEvent.getX(), motionEvent.getY());
                    } else {
                        touchedTaskItem = getTouchedTaskItem(motionEvent.getX(), motionEvent.getY());
                    }
                    if (touchedTaskItem != null) {
                        if (touchedTaskItem instanceof Task) {
                            ((MainActivity) requireActivity()).editTask((Task) touchedTaskItem);
                        }
                    }
                    return true;
                }

                // Edit a task by pressing and holding the task, as well
                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    TaskItem touchedTaskItem;
                    if (groupPopup.getVisibility() == View.VISIBLE) {
                        touchedTaskItem = getTouchedTaskItemPopup(motionEvent.getX(), motionEvent.getY());
                    } else {
                        touchedTaskItem = getTouchedTaskItem(motionEvent.getX(), motionEvent.getY());
                    }
                    if (touchedTaskItem != null) {
                        if (touchedTaskItem instanceof Task) {
                            ((MainActivity) requireActivity()).editTask((Task) touchedTaskItem);
                        }
                    }
                }
            };

            // Finalize the tap responses and assign them to the drawing objects
            final GestureDetector detector = new GestureDetector(context, listener);
            detector.setOnDoubleTapListener(listener);
            detector.setIsLongpressEnabled(true);
            taskDraw.setOnTouchListener((View taskView, MotionEvent motionEvent) ->
                    detector.onTouchEvent(motionEvent));
            groupPopup.setOnTouchListener((View taskView, MotionEvent motionEvent) ->
                    detector.onTouchEvent(motionEvent));
        });
    }

    // Method to retrieve a task from touch coordinates (only the first one if multiple items)
    TaskItem getTouchedTaskItem(float x, float y) {
        TaskDraw taskDraw = requireActivity().findViewById(R.id.task_draw);
        return taskDraw.getTouchedTaskItems(x, y);
    }

    // Same process while popup is showing (for tasks within group only)
    Task getTouchedTaskItemPopup(float x, float y) {
        GroupPopup groupPopup = requireActivity().findViewById(R.id.group_popup);
        return groupPopup.getTouchedTask(x, y);
    }
}