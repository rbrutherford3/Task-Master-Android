package com.rsquared.taskmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static android.graphics.Rect.intersects;

// Class to perform all graphic operations (drawing on canvas, etc)
public class TaskDraw extends View {

  // INITIALIZE PRIVATE MEMBERS

  // Constants for sizing
  protected static final float scaleAdjustment = 1;
  protected static final float checkBoxSide = 30; // length of one side of the checkboxes
  protected static final float marginOuter = 20; // distance between screen edges and axes labels
  protected static final float marginInner = 20; // distance between axis label and margin edge
  protected static final float spacing = 20; // distance between checkbox and text
  protected static final float padding = 20; // how far the touch area of a task should extend
  protected static final float textSize = 40; // height of text characters
  protected static final float stroke = 2; // thickness of text and checkbox
  protected static final float strokeCheckmark = 10; // thickness of check mark
  protected static final float arrowLength = 50;
  protected static final float arrowPointLength = 20;
  protected static final float maxNudgeRatio = (float) 0.25; // Only nudge up to 25% importance
  protected static final String labelHorizontal = "URGENCY";
  protected static final String labelVertical = "IMPORTANCE";
  // Paint objects used for drawing on canvas
  protected Paint paintRect;
  protected Paint paintCheckMark;
  protected Paint paintText;

  // Values for vertical and horizontal labels
  protected Paint paintAxisLabels;
  protected static float margin; // margin inner + |fontTop| + outer margin
  protected static float fontTop; // distance between baseline and highest point in text (-)
  protected static float fontBottom; // distance between baseline and lowest point in text (+)
  protected static float labelVerticalDeltaX;
  protected static float labelVerticalDeltaY;
  protected static float labelHorizontalDeltaX;
  protected static float labelHorizontalDeltaY;
  protected static float[][][] arrowHorizontal;
  protected static float[][][] arrowVertical;
  // Canvas dimensions (should be the same -> square)
  protected static float widthCanvas;
  protected static float heightCanvas;

  // Store a taskViewModel passed in from MainActivity, because views cannot initiate view models
  private TaskViewModel taskViewModel;

  // CONSTRUCTOR

  // requires no special parameters or functions.  Sets up paint objects.
  public TaskDraw(Context context, AttributeSet attrs) {
    super(context, attrs);
    setFocusable(true);
    setFocusableInTouchMode(true);
  }

  // Function to determine the relative position on the urgency vs importance graphic
  @Contract(value = "_, _ -> new", pure = true)
  public static float @NotNull [] getPercentCoordinates(int urgency, int importance) {
    float x = (100 - (float) urgency) / 100;
    float y = (100 - (float) importance) / 100;
    return new float[] {x, y};
  }

  // SETTER FUNCTIONS

  public void initialize(TaskViewModel taskViewModel, int width, int height) {
    setupPaintRect();
    setupPaintText();
    setTaskViewModel(taskViewModel);
    setDimensions(width, height);
    setupCanvasValues(); // need the dimensions to be set before this setup
    overlappingTasks();
  }

  // Store the taskViewModel to refer to and write to database and other stored items
  public void setTaskViewModel(TaskViewModel newTaskViewModel) {
    taskViewModel = newTaskViewModel;
  }

  // Set up paint objects with color and stroke styles
  protected void setupPaintRect() {

    // Paint object for checkbox rectangle and arrows
    paintRect = new Paint();
    paintRect.setColor(Color.WHITE);
    paintRect.setAntiAlias(true);
    paintRect.setStrokeWidth(stroke);
    paintRect.setStyle(Paint.Style.STROKE);
    paintRect.setStrokeJoin(Paint.Join.ROUND);
    paintRect.setStrokeCap(Paint.Cap.ROUND);

    // Paint object for check mark
    paintCheckMark = new Paint(paintRect);
    paintCheckMark.setStrokeWidth(strokeCheckmark);
  }

  // Set up text paint object with color and size
  protected void setupPaintText() {

    // Paint object for task label text
    paintText = new Paint();
    paintText.setColor(Color.WHITE);
    paintText.setTextSize(textSize);
    paintText.getFontMetrics();

    // Extract font measurements for margins and location, etc
    fontTop = paintText.getFontMetrics().top;
    fontBottom = paintText.getFontMetrics().bottom;
    margin = marginOuter - fontTop + marginInner;

    // Paint object for axis label text
    paintAxisLabels = new Paint(paintText);
    paintAxisLabels.setUnderlineText(true);
  }

  // Set the overall dimensions of the graphic
  public void setDimensions(float width, float height) {
    widthCanvas = width;
    heightCanvas = height;
  }

  // GETTER FUNCTIONS

  // Function to determine the absolute distance position on the urgency vs importance graphic
  protected float[] getPixelCoordinates(int urgency, int importance) {
    float[] percentCoordinates = getPercentCoordinates(urgency, importance);
    float x = percentCoordinates[0] * (widthCanvas - 2 * margin) + margin;
    float y =
        percentCoordinates[1] * (heightCanvas - 2 * margin - (fontBottom - fontTop))
            + margin
            + padding
            - fontTop;
    return new float[] {x, y};
  }

  // Inverse of function above
  public int[] getRatings(float x, float y) {
    float percentX = (x - margin) / (widthCanvas - 2 * margin - (fontBottom - fontTop));
    float percentY = (y - margin) / (heightCanvas - 2 * margin - (fontBottom - fontTop));
    int urgency = (int) (100 * (1.0 - percentX));
    int importance = (int) (100 * (1.0 - percentY));
    return new int[] {urgency, importance};
  }

  // SETUP FUNCTIONS

  // Derive values for axis labels with arrows (arrows drawn manually, many lines)
  private void setupCanvasValues() {

    // Gather measurements for vertical label (importance)
    // (Note that the text is meant to read from bottom to top and the canvas is rotated
    //  temporarily to make that happen)
    Rect rectVertical = new Rect();
    paintText.getTextBounds(labelVertical, 0, labelVertical.length(), rectVertical);
    labelVerticalDeltaX = heightCanvas / 2 - rectVertical.width() / (float) 2;
    labelVerticalDeltaY = -fontTop + marginOuter;

    // Gather measurements for horizontal label (importance)
    Rect rectHorizontal = new Rect();
    paintText.getTextBounds(labelHorizontal, 0, labelHorizontal.length(), rectHorizontal);
    labelHorizontalDeltaX = widthCanvas / 2 - rectHorizontal.width() / (float) 2;
    labelHorizontalDeltaY = -fontTop + marginOuter;

    // Create measurements for vertical arrow
    arrowVertical = new float[3][2][2]; // 3 lines, 2 points per line, 2 dimensions per point
    arrowVertical[0][0] =
        new float[] {
          labelVerticalDeltaY - rectVertical.height() / (float) 2, labelVerticalDeltaX - spacing
        };
    arrowVertical[0][1] =
        new float[] {
          labelVerticalDeltaY - rectVertical.height() / (float) 2,
          labelVerticalDeltaX - spacing - arrowLength
        };
    arrowVertical[1][0] =
        new float[] {
          labelVerticalDeltaY - rectVertical.height() / (float) 2,
          labelVerticalDeltaX - spacing - arrowLength
        };
    arrowVertical[1][1] =
        new float[] {
          labelVerticalDeltaY
              - rectVertical.height() / (float) 2
              + arrowPointLength / (float) Math.sqrt(2),
          labelVerticalDeltaX - spacing - arrowLength + arrowPointLength / (float) Math.sqrt(2)
        };
    arrowVertical[2][0] =
        new float[] {
          labelVerticalDeltaY - rectVertical.height() / (float) 2,
          labelVerticalDeltaX - spacing - arrowLength
        };
    arrowVertical[2][1] =
        new float[] {
          labelVerticalDeltaY
              - rectVertical.height() / (float) 2
              - arrowPointLength / (float) Math.sqrt(2),
          labelVerticalDeltaX - spacing - arrowLength + arrowPointLength / (float) Math.sqrt(2)
        };

    // Create measurements for horizontal arrow
    arrowHorizontal = new float[3][2][2]; // 3 lines, 2 points per line, 2 dimensions per point
    arrowHorizontal[0][0] =
        new float[] {
          labelHorizontalDeltaX - spacing,
          labelHorizontalDeltaY - rectHorizontal.height() / (float) 2
        };
    arrowHorizontal[0][1] =
        new float[] {
          labelHorizontalDeltaX - spacing - arrowLength,
          labelHorizontalDeltaY - rectHorizontal.height() / (float) 2
        };
    arrowHorizontal[1][0] =
        new float[] {
          labelHorizontalDeltaX - spacing - arrowLength,
          labelHorizontalDeltaY - rectHorizontal.height() / (float) 2
        };
    arrowHorizontal[1][1] =
        new float[] {
          labelHorizontalDeltaX - spacing - arrowLength + arrowPointLength / (float) Math.sqrt(2),
          labelHorizontalDeltaY
              - rectHorizontal.height() / (float) 2
              + arrowPointLength / (float) Math.sqrt(2)
        };
    arrowHorizontal[2][0] =
        new float[] {
          labelHorizontalDeltaX - spacing - arrowLength,
          labelHorizontalDeltaY - rectHorizontal.height() / (float) 2
        };
    arrowHorizontal[2][1] =
        new float[] {
          labelHorizontalDeltaX - spacing - arrowLength + arrowPointLength / (float) Math.sqrt(2),
          labelHorizontalDeltaY
              - rectHorizontal.height() / (float) 2
              - arrowPointLength / (float) Math.sqrt(2)
        };
  }

  public void setTaskGraphic(@NotNull Task task) {
    String label = task.getLabel();
    int urgency = task.getUrgency();
    int importance = task.getImportance();
    task.setTaskGraphic(setGraphic(label, urgency, importance));
  }

  public void setTaskGroupGraphic(@NotNull TaskGroup taskGroup) {
    String label = taskGroup.getLabel();
    int urgency = taskGroup.getUrgency();
    int importance = taskGroup.getImportance();
    taskGroup.setTaskGraphic(setGraphic(label, urgency, importance));
  }

  // Function to get all the necessary dimensions for the task label, check box, and check mark.
  // These metrics are stored in hash maps in taskViewModel to be pulled during an 'onDraw()' call
  @Contract("_, _, _ -> new")
  protected @NotNull TaskGraphic setGraphic(String label, int urgency, int importance) {

    // Get the position on the canvas for the given task
    float[] coordinates = getPixelCoordinates(urgency, importance);
    float x = coordinates[0]; // The horizontal position of the left side of the checkbox
    float y = coordinates[1]; // The baseline for text and checkbox

    // Create the dimensions for the task text on the canvas
    Rect rectText = new Rect(); // Outlines the text

    // Get text dimensions using two different approaches
    paintText.getTextBounds(label, 0, label.length(), rectText);
    float textWidth = rectText.width();

    float width = checkBoxSide + spacing + textWidth;

    float bottom = y + fontBottom;
    float top = y + fontTop;

    float rectLeft;
    float rectRight;
    float textLeft;
    float textRight;

    float left;
    float right;

    float checkBoxStart;

    // See if contents should be on left or right of origin
    if (x + width > widthCanvas - margin) {
      x = widthCanvas - margin - width;
    }
    rectLeft = x;
    rectRight = x + checkBoxSide;
    checkBoxStart = rectLeft;
    textLeft = rectRight + spacing;
    textRight = textLeft + textWidth;
    left = rectLeft;
    right = textRight;

    Rect touchArea = new Rect((int) left, (int) top, (int) right, (int) bottom);

    // Increase the touch area a little bit for smoother response
    touchArea.inset(-(int) padding, -(int) padding);

    // Update measurements in taskViewModel (so they don't have to be recalculated on drawing)
    return new TaskGraphic(y, checkBoxStart, textLeft, touchArea);
  }

  // This function moves ("nudges") individual tasks that overlap so they are next to each other
  // but not overlapping.
  // Todo: change code so that tasks can fit in the negative spaces of task groups
  protected boolean nudgeTasks(@NotNull TaskGroup taskGroup, boolean forceNudge) {

    // Get the position on the canvas for the given task
    float[] coordinates = getPixelCoordinates(taskGroup.getUrgency(), taskGroup.getImportance());
    float yOrigin = coordinates[1]; // The vertical position of the baseline
    // (lower edge of checkbox and baseline for text)

    // Setting up task measurements (including totals)
    int numTasks = taskGroup.getTasks().size();
    float taskHeight = fontBottom - fontTop;
    float paddedTaskHeight = taskHeight + 2 * padding;
    float totalTaskHeight = numTasks * paddedTaskHeight;
    float topOfTasks = (float) (yOrigin - totalTaskHeight / 2.0);
    float bottomOfTasks = (float) (yOrigin + totalTaskHeight / 2.0);

    // Adjust so margins are not crossed
    // (Note that we assume that the tasks don't cross both the top and bottom, that they fit)
    if (topOfTasks < margin) {
      topOfTasks = margin;
      bottomOfTasks = topOfTasks + totalTaskHeight;
    }
    if (bottomOfTasks > heightCanvas - margin) {
      bottomOfTasks = heightCanvas - margin;
      topOfTasks = bottomOfTasks - totalTaskHeight;
    }

    if (!forceNudge) {
      // Test dimensions to see if nudging would produce any unwanted overlap of graphics
      int counter = 0;
      for (Task task : taskGroup.getTasks()) {

        // See if the minimum required nudging exceeds the predetermined movement limit
        float yBaseline = task.getTaskGraphic().getBaseline();
        float yBaselineDest = topOfTasks + padding - fontTop + counter * paddedTaskHeight;
        float nudgeY = yBaselineDest - yBaseline;
        if (Math.abs(nudgeY) / heightCanvas > maxNudgeRatio) { // Only move tasks so far
          return false;
        }

        // Check and see if there is any overlap with other tasks for dedicated touch area
        Rect testRect = new Rect(task.getTaskGraphic().getTouchArea());
        testRect.offset(0, (int) nudgeY);
        for (Task checkTask : taskViewModel.getTasks()) {
          if (!taskGroup.isTaskInGroup(checkTask)
              && testRect.intersect(checkTask.getTaskGraphic().getTouchArea())) {
            return false;
          }
        }

        // Check task group touch areas as well
        for (TaskGroup checkTaskGroup : taskViewModel.getTaskGroups()) {
          if (taskGroup != checkTaskGroup
              && testRect.intersect(checkTaskGroup.getTaskGraphic().getTouchArea())) {
            return false;
          }
        }
        counter++;
      }
    }

    // If the tests were passed, then nudge each task to an unoccupied location
    int counter = 0;
    for (Task task : taskGroup.getTasks()) {
      float yBaseline = task.getTaskGraphic().getBaseline();
      float yBaselineDest = topOfTasks + padding - fontTop + counter * paddedTaskHeight;
      float nudgeY = yBaselineDest - yBaseline;
      task.getTaskGraphic().move(0, (int) nudgeY);
      counter++;
    }
    return true;
  }

  // Collects pairs of tasks whose touch areas overlap and turns them into a group of items
  // Any task that overlaps another task or a group becomes part of that group
  protected void overlappingTasks() {

    taskViewModel.deGroupTasks();
    boolean newPairingFound;
    Set<Task> tasks = taskViewModel.getTasks();
    Set<TaskGroup> taskGroups = taskViewModel.getTaskGroups();

    // Make sure all the graphic information is up-to-date (including tasks in groups)
    for (Task task : tasks) {
      setTaskGraphic(task);
    }

    // Loop through possible pairs until two that overlap are found,
    // then queue the group to be created and the tasks to be moved to a group list
    do {
      TaskGroup taskGroupToAdd = new TaskGroup();
      Set<Task> tasksToRemove = new HashSet<>();
      Set<Task> otherTasks = new HashSet<>(tasks);
      newPairingFound = false;

      // Loop through every possible combination of tasks
      for (Task task : tasks) {
        if (otherTasks.size() > 0) {
          otherTasks.remove(task);
        }
        // (Note that the lower bound of this loop increases with the first loop.
        //   This is to prevent redundancy, ex: #1 & #2 vs #2 & #1.  Only first will occur)
        for (Task otherTask : Collections.unmodifiableSet(otherTasks)) {

          // See if item touch areas overlap and group them together (groups and/or tasks)
          Rect touchArea = task.getTaskGraphic().getTouchArea();
          Rect otherTouchArea = otherTask.getTaskGraphic().getTouchArea();
          if (intersects(touchArea, otherTouchArea)) {
            newPairingFound = true; // Break while loop to start over with new info

            // Combine into groups:
            // (item + item, item + group, group + item, or group + group)
            taskGroupToAdd.addTask(task);
            taskGroupToAdd.addTask(otherTask);
            setTaskGroupGraphic(taskGroupToAdd);
            tasksToRemove.add(task);
            tasksToRemove.add(otherTask);
          }
          // Stop looking for pairs and process this pair
          // NOTE: the reason we break the double for-loop instead of marking all
          // combinations and processing them in the end is that grouping changes the
          // initial conditions of the process, so we start over for each pair found
          if (newPairingFound) {
            break;
          }
        }
        if (newPairingFound) {
          break;
        }
      }

      // Todo: this area below needs be organized better (check first, then add/remove, not vice versa)

      // Add new groups to the master list for groups and remove grouped tasks from master
      // list for tasks (because they are now a part of a group, eliminates redundancy)
      if (newPairingFound) {
        taskGroups.add(taskGroupToAdd);
        tasks.removeAll(tasksToRemove);

        // See if the newly formed group overlaps any other tasks
        for (Task task : tasks) {
          if (taskGroupToAdd
              .getTaskGraphic()
              .getTouchArea()
              .intersect(task.getTaskGraphic().getTouchArea())) {
            taskGroupToAdd.addTask(task);
            setTaskGroupGraphic(taskGroupToAdd);
            tasksToRemove.add(task);
          }
        }

        tasks.removeAll(tasksToRemove);
      }

    } while (newPairingFound); // Keep going until no more overlaps are detected

    // If nudging the tasks worked, then no need for a group
    Set<TaskGroup> taskGroupsToRemove = new HashSet<>();
    Set<Task> tasksToAdd = new HashSet<>();
    for (TaskGroup taskGroup : taskGroups) {
      if (nudgeTasks(taskGroup, false)) {
        taskGroupsToRemove.add(taskGroup);
        tasksToAdd.addAll(taskGroup.getTasks());
      }
    }

    taskGroups.removeAll(taskGroupsToRemove);
    tasks.addAll(tasksToAdd);
    invalidate(); // force a re-draw
  }

  // DRAW FUNCTION (THE HEART OF THE CLASS AND MAY BE CALLED VERY FREQUENTLY)

  // Called by the view whenever an update to the graphics is warranted (automatic)
  // It sets up every graphic on the screen (except the background)
  protected void onDraw(Canvas canvas) {

    // Draw each task individually
    // Make sure the taskViewModel exists (not too early in program)
    if (taskViewModel != null) {
      // For each and every task...
      for (TaskGroup taskGroup : taskViewModel.getTaskGroups()) {
        drawTaskGroup(canvas, taskGroup);
      }
      for (Task task : taskViewModel.getTasks()) {
        if (!task.getMoving()){
          drawTask(canvas, task, scaleAdjustment, false);
        }
      }
      setupCanvas(canvas); // Draw axes elements
    }
  }

  // Draw permanent graphics such as axes labels and arrows
  public void setupCanvas(@NotNull Canvas canvas) {

    // Place axes labels along the top and right sides
    // Canvas must be rotated to create label for vertical axis
    canvas.save();
    canvas.rotate(-90, widthCanvas / 2, heightCanvas / 2);
    canvas.drawText(labelVertical, labelVerticalDeltaX, labelVerticalDeltaY, paintAxisLabels);
    canvas.restore();
    canvas.drawText(labelHorizontal, labelHorizontalDeltaX, labelHorizontalDeltaY, paintAxisLabels);

    // Drawing arrow for vertical axis
    canvas.drawLine(
        arrowVertical[0][0][0],
        arrowVertical[0][0][1],
        arrowVertical[0][1][0],
        arrowVertical[0][1][1],
        paintRect);
    canvas.drawLine(
        arrowVertical[1][0][0],
        arrowVertical[1][0][1],
        arrowVertical[1][1][0],
        arrowVertical[1][1][1],
        paintRect);
    canvas.drawLine(
        arrowVertical[2][0][0],
        arrowVertical[2][0][1],
        arrowVertical[2][1][0],
        arrowVertical[2][1][1],
        paintRect);

    // Drawing arrow for horizontal axis
    canvas.drawLine(
        arrowHorizontal[0][0][0],
        arrowHorizontal[0][0][1],
        arrowHorizontal[0][1][0],
        arrowHorizontal[0][1][1],
        paintRect);
    canvas.drawLine(
        arrowHorizontal[1][0][0],
        arrowHorizontal[1][0][1],
        arrowHorizontal[1][1][0],
        arrowHorizontal[1][1][1],
        paintRect);
    canvas.drawLine(
        arrowHorizontal[2][0][0],
        arrowHorizontal[2][0][1],
        arrowHorizontal[2][1][0],
        arrowHorizontal[2][1][1],
        paintRect);
  }

  // Todo: possibly recombine parts of the drawTask and drawTaskGroup functions

  // Draw the task onto the canvas (because this function has the potential to be
  // called very frequently, no calculations or large allocations are performed here)
  protected void drawTask(@NotNull Canvas canvas, @NotNull Task task, float scaleFactor, boolean center) {

    // Modify to center, not align to origin

    float displacementX = 1;

    TaskGraphic graphic = task.getTaskGraphic();
    String label = task.getLabel();

    // Pull the pre-determined position information for the task
    float yBaseline;
    if (center) {
      yBaseline = canvas.getHeight()-scaleFactor*(padding + fontBottom);
    }
    else {
      yBaseline = graphic.getBaseline();
    }
    float xCheckbox = scaleFactor*graphic.getCheckBoxStart() + displacementX;
    float xText = scaleFactor*graphic.getTextStart() + displacementX;


    if (center) {
      if (xText >  xCheckbox) {
        xText = xText - xCheckbox + displacementX;
        xCheckbox = displacementX;
      }
      else {
        xCheckbox = xCheckbox - xText + displacementX;
        xText = displacementX;
      }
    }
    // Draw checkbox
    canvas.drawRect(
        xCheckbox, yBaseline - scaleFactor*checkBoxSide,
        xCheckbox + scaleFactor*checkBoxSide, yBaseline, paintRect);

    // Display the label in the pre-determined position
    float originalTextSize = paintText.getTextSize();
    paintText.setTextSize(paintText.getTextSize()*scaleFactor);
    canvas.drawText(label, xText, yBaseline, paintText);
    paintText.setTextSize(originalTextSize);

    // If task is completed, add a check mark
    if (task.getCompleted()) {
      canvas.drawLine(
          scaleFactor*(xCheckbox + checkBoxSide),
          (float) (yBaseline - (1.5 * scaleFactor * checkBoxSide)),
          (float) (xCheckbox + (0.5 * scaleFactor * checkBoxSide)),
          yBaseline,
          paintCheckMark);
      canvas.drawLine(
          (float) (xCheckbox + (0.5 * scaleFactor * checkBoxSide)),
          yBaseline,
          xCheckbox,
          (float) (yBaseline - (0.5 * scaleFactor * checkBoxSide)),
          paintCheckMark);
    }
  }

  protected void drawTaskGroup(@NotNull Canvas canvas, @NotNull TaskGroup taskGroup) {

    TaskGraphic graphic = taskGroup.getTaskGraphic();
    String label = taskGroup.getLabel();

    // Pull the pre-determined position information for the task
    float yBaseline = graphic.getBaseline();
    float xCheckbox = graphic.getCheckBoxStart();
    float xText = graphic.getTextStart();

    // Display cross (plus sign) instead of checkbox for task group
    canvas.drawLine(
        xCheckbox + (checkBoxSide / 2),
        yBaseline,
        xCheckbox + (checkBoxSide / 2),
        yBaseline - checkBoxSide,
        paintRect);
    canvas.drawLine(
        xCheckbox,
        yBaseline - (checkBoxSide / 2),
        xCheckbox + checkBoxSide,
        yBaseline - (checkBoxSide / 2),
        paintRect);

    // Display the label in the pre-determined position
    canvas.drawText(label, xText, yBaseline, paintText);
  }

  // TOUCH RESPONSE FUNCTION (WHICH, IF ANY, TASK(S) WERE TOUCHED?)

  // This function returns a list of tasks that were touched by the user on the canvas
  public Task getTouchedTask(float x, float y) {

    // Loop through all tasks (groups, too) and see if touch coordinates are inside a touch area
    for (Task task : taskViewModel.getTasks()) {
      if (task.getTaskGraphic().getTouchArea().contains((int) x, (int) y)) {
        return task;
      }
    }
    return null; // return null if no tasks were found
  }

  public TaskGroup getTouchedTaskGroup(float x, float y) {
    for (TaskGroup taskGroup : taskViewModel.getTaskGroups()) {
      if (taskGroup.getTaskGraphic().getTouchArea().contains((int) x, (int) y)) {
        return taskGroup;
      }
    }
    return null;
  }

  // DEBUG

  // Debugging function for checking system state
  @Override
  public @NotNull String toString() {
    // Display task details
    int count = 0;
    StringBuilder msg = new StringBuilder();
    for (Task task : taskViewModel.getTasks()) {
      count++;
      msg.append(task.getLabel())
          .append(", urgency = ")
          .append(task.getUrgency())
          .append(", importance = ")
          .append(task.getImportance())
          .append(System.getProperty("line.separator"));
    }
    if (count == 0) {
      msg.append("EMPTY DATABASE");
    }
    return msg.toString();
  }
}
