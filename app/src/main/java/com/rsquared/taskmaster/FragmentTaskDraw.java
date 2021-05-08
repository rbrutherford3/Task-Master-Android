package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Task Draw fragment (canvas with interactive task listings, plus a popup, if necessary)
public class FragmentTaskDraw extends Fragment {

  // PRIVATE MEMBERS

  private TaskDraw taskDraw; // "Canvas" view
  private GroupPopup groupPopup; // Pop up element for groups of tasks
  private ImageView popupBackground; // Object for drawing pop up background
  private TaskViewModel taskViewModel; // For accessing and modifying task information
  private final float scaleFactor = (float) 3; // How much bigger should the task appear while dragging?

  // Provide an instance of this class
  @Contract(" -> new")
  public static @NotNull FragmentTaskDraw newInstance() {
    return new FragmentTaskDraw();
  }

  // OVER-RIDDEN LIFECYCLE METHODS

  // Necessary override
  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
    view.post(
        () -> {

          // Set up taskDraw, related views, and canvases
          setupViews();

          // Initialize private members
          final float[] startCoordinates = new float[2];

          final Context context = getContext();

          // TAP LISTENER FOR THE TASK DRAW CANVAS AND THE TASKS THEREIN

          // Create responses to tap gestures on the task layout (not the group popup)
          final SimpleOnGestureListener taskDrawListener =
              new SimpleOnGestureListener() {

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

                  taskDraw.invalidate(); // update image
                  return true; // necessary to prevent further action resulting from tap
                }

                // Edit a task by double tapping it or create a new one by double tapping empty space
                @Override
                public boolean onDoubleTap(MotionEvent motionEvent) {
                  float x = motionEvent.getX();
                  float y = motionEvent.getY();
                  Task touchedTask =
                      taskDraw.getTouchedTask(x, y);
                  if (touchedTask == null) {
                    int[] ratings = taskDraw.getRatings(x, y);
                    int urgency = ratings[0];
                    int importance = ratings[1];
                    ((MainActivity) requireActivity()).addTask(urgency, importance);
                  }
                  else {
                    ((MainActivity) requireActivity()).editTask(touchedTask);
                  }
                  return true;
                }

                // Move a task by pressing and holding the task
                @Override
                public void onLongPress(@NotNull MotionEvent motionEvent) {

                  // Which task was pressed on?
                  startCoordinates[0] = motionEvent.getX();
                  startCoordinates[1] = motionEvent.getY();
                  Task touchedTask = taskDraw.getTouchedTask(startCoordinates[0], startCoordinates[1]);

                  // Create shadow builder to display task while being dragged and dropped
                  if (touchedTask != null) {
                    MyDragShadowBuilder shadowBuilder = new MyDragShadowBuilder(touchedTask);

                    // Past a certain Android release, the function name changed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                      taskDraw.startDragAndDrop(null, shadowBuilder, touchedTask, 0);
                    }
                    else {
                      taskDraw.startDrag(null, shadowBuilder, touchedTask, 0);
                    }
                  }
                }
              };

          // While dragging, display blown up task graphic under the user's finger
          final OnDragListener dragListener = (View v, DragEvent event) -> {

            // Get phase of drag and task
            int action = event.getAction();
            Task newTask = (Task)(event.getLocalState());

            // For each relevant drag state...
            switch (action) {

              // Refresh screen (without dragged task) at drag beginning
              case DragEvent.ACTION_DRAG_STARTED:
                newTask.setMoving(true);
                setupViews();
                break;

              // Grab the location of the dropped task and assign new urgency and importance levels
              case DragEvent.ACTION_DROP:
                float x = event.getX() - taskDraw.getCheckBoxSide();
                float y = event.getY() - taskDraw.getCheckBoxSide();
                int[] ratings = taskDraw.getRatings(x, y);
                newTask.setUrgency(ratings[0]);
                newTask.setImportance(ratings[1]);
                newTask.setTaskGraphic(taskDraw.setGraphic(newTask.getLabel(), newTask.getUrgency(), newTask.getImportance()));
                break;

              // Stop drag process and update the new information up the chain, refresh screen
              case DragEvent.ACTION_DRAG_ENDED:
                newTask.setMoving(false);
                taskViewModel.updateTask(newTask);
                setupViews();
                break;
            }
            return true;
          };

          // TAP LISTENER FOR THE GROUP POPUP CANVAS AND THE TASKS THEREIN

          // Create responses to tap gestures on the screen
          final SimpleOnGestureListener groupPopupListener =
              new SimpleOnGestureListener() {

                // Apparently this override is necessary for the rest to be successful
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                  return true;
                }

                // Interpret single tap anywhere on the screen
                @Override
                public boolean onSingleTapConfirmed(@NotNull MotionEvent motionEvent) {
                  // If in popup mode, set task completed if tapped, otherwise hide the popup
                  Task touchedTask =
                      groupPopup.getTouchedTask(motionEvent.getX(), motionEvent.getY());
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
                  editTask(motionEvent);
                  return true;
                }

                // Move task by pressing and holding the task, as well
                @Override
                public void onLongPress(MotionEvent motionEvent) {
                  dragTask(motionEvent);
                }

                // Get task and pull up editing screen
                private void editTask(@NotNull MotionEvent motionEvent) {
                  Task touchedTask =
                      groupPopup.getTouchedTask(motionEvent.getX(), motionEvent.getY());

                  if (touchedTask != null) {
                    ((MainActivity) requireActivity()).editTask(touchedTask);
                  }
                }

                // Function to enter dragging mode
                private void dragTask(@NotNull MotionEvent motionEvent) {

                  // Get the selected task
                  Task touchedTask = groupPopup.getTouchedTask(motionEvent.getX(), motionEvent.getY());

                  // If a task was selected...
                  if (touchedTask != null) {

                    // Temporarily hide the task from the screen, enter drag builder
                    touchedTask.setMoving(true);
                    MyDragShadowBuilder shadowBuilder = new MyDragShadowBuilder(touchedTask);

                    // Grab a fake beginning location of the task on the canvas (to calculate new
                    // importance and urgency after the task is dropped, need values to compare)
                    taskDraw.getPixelCoordinates(touchedTask.getUrgency(), touchedTask.getImportance());

                    // Past a certain Android release, the function name changed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                      taskDraw.startDragAndDrop(null, shadowBuilder, touchedTask, 0);
                    } else {
                      taskDraw.startDrag(null, shadowBuilder, touchedTask, 0);
                    }

                    // Hide group popup
                    groupPopup.setVisibility(View.INVISIBLE);
                    popupBackground.setVisibility(View.INVISIBLE);
                  }
                }
              };

          // Finalize tap responses for the task draw canvas and tie them with the view objects
          final GestureDetector taskDrawDetector = new GestureDetector(context, taskDrawListener);
          taskDrawDetector.setOnDoubleTapListener(taskDrawListener);
          taskDrawDetector.setIsLongpressEnabled(true);
          taskDraw.setOnTouchListener(
              (View taskView, MotionEvent motionEvent) ->
                  taskDrawDetector.onTouchEvent(motionEvent));
          taskDraw.setOnDragListener(dragListener);

          // Finalize tap responses for the group popup canvas and tie them with the view objects
          final GestureDetector popUpDetector = new GestureDetector(context, groupPopupListener);
          popUpDetector.setOnDoubleTapListener(groupPopupListener);
          popUpDetector.setIsLongpressEnabled(true);
          groupPopup.setOnTouchListener(
              (View taskView, MotionEvent motionEvent) -> popUpDetector.onTouchEvent(motionEvent));
        });
  }

  // Customize task display while being dragged
  class MyDragShadowBuilder extends DragShadowBuilder {

    final Task movedTask;

    public MyDragShadowBuilder(@NotNull Task task) {
      movedTask = task;
    }

    @Override
    public void onProvideShadowMetrics(@NotNull Point shadowSize, @NotNull Point shadowTouchPoint) {

      // Get outline of task touch area for measurement
      Rect touchArea = new Rect(movedTask.getTaskGraphic().getTouchArea());
      float shadowWidth = scaleFactor * (touchArea.right - touchArea.left);
      float shadowHeight = scaleFactor * (touchArea.bottom - touchArea.top);
      // Needs to be slightly bigger than image to avoid flickering
      shadowSize.set((int) shadowWidth + 1, (int) shadowHeight + 1);
      // Finger at bottom left corner:
      shadowTouchPoint.set((int) (taskDraw.getCheckBoxSide() * 1.5 * scaleFactor), (int) shadowHeight);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
      // Draw over-sized task graphic
      taskDraw.drawTask(canvas, movedTask, scaleFactor, true);
    }
  }

  // Set up all the views and their attributes, reset the data, and display
  private void setupViews() {
    taskDraw = requireActivity().findViewById(R.id.task_draw);
    groupPopup = requireActivity().findViewById(R.id.group_popup);
    popupBackground = requireActivity().findViewById(R.id.popup_background);
    taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
    taskViewModel.deGroupTasks();
    taskDraw.initialize(taskViewModel, taskDraw.getWidth(), taskDraw.getHeight());
  }
}
