package com.rsquared.taskmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import static java.lang.Math.max;
import static java.lang.Math.min;

// Class for popup view that generates when the user taps on a group (uses TaskDraw as model)
public class GroupPopup extends TaskDraw {

  // INITIALIZE PRIVATE MEMBERS

  private static final int padding = 20; // dp
  private static final int borderColor = 0xFF000000;
  private static final int borderThickness = 2;
  private TaskGroup taskGroup;

  // Inherit constructor from parent
  public GroupPopup(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  // Set up paint objects, graphics for the group, etc
  public void initialize(TaskGroup taskGroup) {
    setupPaintRect();
    setupPaintText();
    setGroup(taskGroup);
    nudgeTasks(taskGroup, true);
    setDimensions();
    setGraphic();
    prepareCanvas();
  }

  // Get dimensions for the group popup ahead of time, base on the location and size of tasks
  protected void setDimensions() {

    // Declare and initialize individual values
    float leftest = 0;
    float rightest = 0;
    float highest = 0;
    float lowest = 0;

    // Get the lowest lows and the highest highs of the collection of task graphics
    if (taskGroup.getTasks().size() > 0) {
      boolean first = true;
      for (Task task : taskGroup.getTasks()) {
        Rect touchArea = task.getTaskGraphic().getTouchArea();
        if (first) {
          first = false;
          leftest = touchArea.left;
          rightest = touchArea.right;
          highest = touchArea.top;
          lowest = touchArea.bottom;
        } else {
          leftest = min(leftest, touchArea.left);
          rightest = max(rightest, touchArea.right);
          highest = min(highest, touchArea.top);
          lowest = max(lowest, touchArea.bottom);
        }
      }

      // Translate the values gathered into canvas dimensions (with a little bit of a margin)
      widthCanvas = rightest - leftest + 2*marginInner;
      heightCanvas = lowest - highest + 2*marginInner;

      // Translate values into uniform displacement to the origin, and move
      float deltaX = -leftest + marginInner;
      float deltaY = -highest + marginInner;
      for (Task task : taskGroup.getTasks()) {
        task.getTaskGraphic().move((int) deltaX, (int) deltaY);
      }
    }
  }

  // User defined group
  public void setGroup(TaskGroup taskGroup) {
    this.taskGroup = taskGroup;
  }

  // Set up popup area by moving everything to the center
  public void setGraphic() {

    // Get the minimum size of the popup by joining each tasks' touch area
    Rect groupArea = new Rect();
    for (Task task : this.taskGroup.getTasks()) {
      groupArea.union(new Rect(task.getTaskGraphic().getTouchArea()));
    }

    // Determine where to move each task based on the area gathered above
    float deltaX = -groupArea.left + padding;
    float deltaY = -groupArea.top + padding;
    for (Task task : taskGroup.getTasks()) {
      task.getTaskGraphic().move((int) deltaX, (int) deltaY);
    }
  }

  // Set up popup background color, border thickness, dimensions, etc
  public void prepareCanvas() {

    // Determine background color of popup based on group location on taskDraw
    int backgroundColor = getColor(taskGroup.getImportance(), taskGroup.getUrgency());
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
    params.height = (int) heightCanvas;
    params.width = (int) widthCanvas;
    setLayoutParams(params);
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    gradientDrawable.setStroke(borderThickness, borderColor);
    gradientDrawable.setColor(backgroundColor);
    setBackground(gradientDrawable);
  }

  // Draw all the group's tasks
  @Override
  protected void onDraw(Canvas canvas) {
    if (taskGroup != null) {
      for (Task task : taskGroup.getTasks()) {
        drawTask(canvas, task, 1, false);
        System.out.println(task.toString());
      }
    }
  }

  // Return a task after touching an area by looping through each task and checking
  public Task getTouchedTask(float x, float y) {

    // Loop through all tasks
    for (Task task : taskGroup.getTasks()) {

      // If the tap coordinates were inside the touch area for a task, then save the task
      if (task.getTaskGraphic().getTouchArea().contains((int) x, (int) y)) {
        return task;
      }
    }
    return null; // nothing hit if you've made it this far
  }

  // Determine color of popup based on the groups' relative location on the taskDraw canvas
  // A 3-value gradient can be interpreted as 4 2-color gradients, four quadrants w/ four corners
  public int getColor(int importance, int urgency) {

    // Get colors from layout file

    int highest = ContextCompat.getColor(getContext(), R.color.highest);
    int middle = ContextCompat.getColor(getContext(), R.color.middle);
    int lowest = ContextCompat.getColor(getContext(), R.color.lowest);

    // Divide each color into color bands

    int highestA = (highest >> 24) & 0xFF;
    int highestR = (highest >> 16) & 0xFF;
    int highestB = (highest >> 8) & 0xFF;
    int highestG = highest & 0xFF;

    int lowestA = (lowest >> 24) & 0xFF;
    int lowestR = (lowest >> 16) & 0xFF;
    int lowestB = (lowest >> 8) & 0xFF;
    int lowestG = lowest & 0xFF;

    int middleA = (middle >> 24) & 0xFF;
    int middleR = (middle >> 16) & 0xFF;
    int middleB = (middle >> 8) & 0xFF;
    int middleG = middle & 0xFF;

    // Compile intermediate colors by averaging in each band

    int lowMixA = (middleA + lowestA) / 2;
    int lowMixR = (middleR + lowestR) / 2;
    int lowMixG = (middleG + lowestG) / 2;
    int lowMixB = (middleB + lowestB) / 2;

    int highMixA = (middleA + highestA) / 2;
    int highMixR = (middleR + highestR) / 2;
    int highMixG = (middleG + highestG) / 2;
    int highMixB = (middleB + highestB) / 2;

    // Compile intermediate colors

    int lowMix = lowMixA << 24 | lowMixR << 16 | lowMixB << 8 | lowMixG;
    int highMix = highMixA << 24 | highMixR << 16 | highMixB << 8 | highMixG;

    // For storing the color for each quadrant's corner
    int upperLeft;
    int upperRight;
    int lowerRight;
    int lowerLeft;

    // Weight factor for each quadrant corner's color (closer to corner, more of that color)
    float xWeight;
    float yWeight;

    // Divide colors by quadrant and pick one based on importance and urgency, assign colors
    if (importance > 50) {
      yWeight = (importance - 50) / (float) 50;
      if (urgency > 50) {
        xWeight = (urgency - 50) / (float) 50;
        upperLeft = highest;
        upperRight = highMix;
        lowerRight = middle;
        lowerLeft = highMix;
      } else {
        xWeight = urgency / (float) 50;
        upperLeft = highMix;
        upperRight = middle;
        lowerRight = lowMix;
        lowerLeft = middle;
      }
    } else {
      yWeight = importance / (float) 50;
      if (urgency < 50) {
        xWeight = urgency / (float) 50;
        upperLeft = middle;
        upperRight = lowMix;
        lowerRight = lowest;
        lowerLeft = lowMix;
      } else {
        xWeight = (urgency - 50) / (float) 50;
        upperLeft = highMix;
        upperRight = middle;
        lowerRight = lowMix;
        lowerLeft = middle;
      }
    }

    // Given quadrant, apply two-factor weight to the color of each corner to get final color

    int resultA =
        (int)
            (Color.alpha(upperLeft) * (yWeight * xWeight)
                + Color.alpha(lowerRight) * (1 - xWeight) * (1 - yWeight)
                + Color.alpha(upperRight) * yWeight * (1 - xWeight)
                + Color.alpha(lowerLeft) * (1 - yWeight) * xWeight);

    int resultR =
        (int)
            (Color.red(upperLeft) * (yWeight * xWeight)
                + Color.red(lowerRight) * (1 - xWeight) * (1 - yWeight)
                + Color.red(upperRight) * yWeight * (1 - xWeight)
                + Color.red(lowerLeft) * (1 - yWeight) * xWeight);

    int resultG =
        (int)
            (Color.green(upperLeft) * (yWeight * xWeight)
                + Color.green(lowerRight) * (1 - xWeight) * (1 - yWeight)
                + Color.green(upperRight) * yWeight * (1 - xWeight)
                + Color.green(lowerLeft) * (1 - yWeight) * xWeight);

    int resultB =
        (int)
            (Color.blue(upperLeft) * (yWeight * xWeight)
                + Color.blue(lowerRight) * (1 - xWeight) * (1 - yWeight)
                + Color.blue(upperRight) * yWeight * (1 - xWeight)
                + Color.blue(lowerLeft) * (1 - yWeight) * xWeight);

    // Compile color and return result
    return resultA << 24 | resultR << 16 | resultG << 8 | resultB;
  }
}
