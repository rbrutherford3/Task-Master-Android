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

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

// Class to perform all graphic operations (drawing on canvas, etc)
public class TaskDraw extends View {

    // INITIALIZE PRIVATE MEMBERS

    // Constants for sizing
    protected final float checkBoxSide = 30;        // length of one side of the checkboxes
    protected final float marginOuter = 15;         // distance between screen edges and axes labels
    protected final float marginInner = 15;         // distance between axis label and margin edge
    protected final float spacing = 15;             // distance between checkbox and text
    protected final float padding = 15;             // how far the touch area of a task should extend
    protected final float textSize = 40;            // height of text characters
    protected final float stroke = 2;               // thickness of text and checkbox
    protected final float strokeCheckmark = 10;     // thickness of check mark
    protected final float arrowLength = 50;
    protected final float arrowPointLength = 20;
    protected final String labelVertical = "IMPORTANCE";
    protected final String labelHorizontal = "URGENCY";
    // Paint objects used for drawing on canvas
    protected Paint paintRect;
    protected Paint paintCheckMark;
    protected Paint paintText;

    // Values for vertical and horizontal labels
    protected Paint paintAxisLabels;
    protected float margin;                         // margin inner + |fontTop| + outer margin
    protected float fontTop;                        // distance between baseline and highest point in text (-)
    protected float fontBottom;                     // distance between baseline and lowest point in text (+)
    protected float labelVerticalDeltaX;
    protected float labelVerticalDeltaY;
    protected float labelHorizontalDeltaX;
    protected float labelHorizontalDeltaY;
    protected float[][][] arrowHorizontal;
    protected float[][][] arrowVertical;
    // Canvas dimensions (should be the same -> square)
    protected float heightCanvas;
    protected float widthCanvas;

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
    public static float @NotNull [] getPercentCoordinates(int importance, int urgency) {
        float x = (100 - (float) urgency) / 100;
        float y = (100 - (float) importance) / 100;
        return new float[]{x, y};
    }

    // SETTER FUNCTIONS

    public void initialize(TaskViewModel taskViewModel, int width, int height) {
        setupPaintRect();
        setupPaintText();
        setTaskViewModel(taskViewModel);
        taskViewModel.deGroupTasks();
        setDimensions(width, height);
        setupCanvasValues();    // need the dimensions to be set before this setup
        overlappingTasks();
    }

    // Store the taskViewModel to refer to and write to database and other stored items
    public void setTaskViewModel(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
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

    // GETTER FUNCTIONS

    // Get the overall dimensions of the graphic
    public void setDimensions(float width, float height) {
        widthCanvas = width;
        heightCanvas = height;
    }

    // Function to determine the absolute distance position on the urgency vs importance graphic
    protected float[] getPixelCoordinates(int importance, int urgency) {
        float[] percentCoordinates = getPercentCoordinates(importance, urgency);
        float x = percentCoordinates[0] * (widthCanvas - 2 * margin) + margin;
        float y = percentCoordinates[1] * (heightCanvas - 2 * margin -
                (fontBottom - fontTop)) + margin + padding - fontTop;
        return new float[]{x, y};
    }

    // SETUP FUNCTIONS

    // Derive values for axis labels with arrows (arrows drawn manually, many lines)
    private void setupCanvasValues() {

        // Gather measurements for vertical label (importance
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
        arrowVertical[0][0] = new float[]{
                labelVerticalDeltaY - rectVertical.height() / (float) 2,
                labelVerticalDeltaX - spacing};
        arrowVertical[0][1] = new float[]{
                labelVerticalDeltaY - rectVertical.height() / (float) 2,
                labelVerticalDeltaX - spacing - arrowLength};
        arrowVertical[1][0] = new float[]{
                labelVerticalDeltaY - rectVertical.height() / (float) 2,
                labelVerticalDeltaX - spacing - arrowLength};
        arrowVertical[1][1] = new float[]{
                (float) (labelVerticalDeltaY - rectVertical.height() / 2 +
                        arrowPointLength / Math.sqrt(2)),
                (float) (labelVerticalDeltaX - spacing - arrowLength +
                        arrowPointLength / Math.sqrt(2))};
        arrowVertical[2][0] = new float[]{
                labelVerticalDeltaY - rectVertical.height() / (float) 2,
                labelVerticalDeltaX - spacing - arrowLength};
        arrowVertical[2][1] = new float[]{
                (float) (labelVerticalDeltaY - rectVertical.height() / 2 -
                        arrowPointLength / Math.sqrt(2)),
                (float) (labelVerticalDeltaX - spacing - arrowLength +
                        arrowPointLength / Math.sqrt(2))};

        // Create measurements for vertical arrow
        arrowHorizontal = new float[3][2][2]; // 3 lines, 2 points per line, 2 dimensions per point
        arrowHorizontal[0][0] = new float[]{
                labelHorizontalDeltaX - spacing,
                labelHorizontalDeltaY - rectHorizontal.height() / (float) 2};
        arrowHorizontal[0][1] = new float[]{
                labelHorizontalDeltaX - spacing - arrowLength,
                labelHorizontalDeltaY - rectHorizontal.height() / (float) 2};
        arrowHorizontal[1][0] = new float[]{
                labelHorizontalDeltaX - spacing - arrowLength,
                labelHorizontalDeltaY - rectHorizontal.height() / (float) 2};
        arrowHorizontal[1][1] = new float[]{
                (float) (labelHorizontalDeltaX - spacing - arrowLength +
                        arrowPointLength / Math.sqrt(2)),
                (float) (labelHorizontalDeltaY - rectHorizontal.height() / 2 +
                        arrowPointLength / Math.sqrt(2))};
        arrowHorizontal[2][0] = new float[]{
                labelHorizontalDeltaX - spacing - arrowLength,
                labelHorizontalDeltaY - rectHorizontal.height() / (float) 2};
        arrowHorizontal[2][1] = new float[]{
                (float) (labelHorizontalDeltaX - spacing - arrowLength +
                        arrowPointLength / Math.sqrt(2)),
                (float) (labelHorizontalDeltaY - rectHorizontal.height() / 2 -
                        arrowPointLength / Math.sqrt(2))};
    }

    // Function to get all the necessary dimensions for the task label, check box, and check mark.
    // These metrics are stored in hash maps in taskViewModel to be pulled during an 'ondraw()' call
    public TaskGraphic setGraphics(@NotNull TaskItem taskItem) {

        // Get the position on the canvas for the given task
        float[] coordinates = getPixelCoordinates(taskItem.getImportance(), taskItem.getUrgency());
        float xOrigin = coordinates[0]; // The horizontal position of the left side of the checkbox
        float yOrigin = coordinates[1]; // The baseline for text and checkbox
        // (lower edge of checkbox and baseline for text)

        // Create the dimensions for the task text on the canvas
        Rect rectText = new Rect(); // Outlines the text

        // Get text dimensions using two different approaches
        paintText.getTextBounds(taskItem.getLabel(), 0,
                taskItem.getLabel().length(), rectText);
        float textWidth = rectText.width();

        float width = checkBoxSide + spacing + textWidth;

        float bottom = yOrigin + fontBottom;
        float top = yOrigin + fontTop;

        float rectLeft, rectRight, textLeft, textRight;

        float left, right;
        float checkBoxStart;

        // See if contents should be on left or right of origin
        if (xOrigin + width > widthCanvas - margin) {
            rectRight = xOrigin;
            rectLeft = xOrigin - checkBoxSide;
            checkBoxStart = rectLeft;
            textRight = rectLeft - spacing;
            textLeft = textRight - textWidth;
            left = textLeft;
            right = rectRight;
        } else {
            rectLeft = xOrigin;
            rectRight = xOrigin + checkBoxSide;
            checkBoxStart = rectLeft;
            textLeft = rectRight + spacing;
            textRight = textLeft + textWidth;
            left = rectLeft;
            right = textRight;
        }

        Rect touchArea = new Rect(
                (int) left,
                (int) top,
                (int) right,
                (int) bottom);

        // Increase the touch area a little bit for smoother response
        touchArea.inset(-(int) padding, -(int) padding);

        // Update measurements in taskViewModel (so they don't have to be recalculated on drawing)
        return new TaskGraphic(yOrigin, checkBoxStart, textLeft, touchArea);
    }

    // This function moves ("nudges") individual tasks that overlap so they are next to each other
    // but not overlapping.
    protected void nudgeTasks(@NotNull TaskGroup taskGroup) {

        // Get the position on the canvas for the given task
        float[] coordinates = getPixelCoordinates(taskGroup.getImportance(), taskGroup.getUrgency());
        float yOrigin = coordinates[1]; // The vertical position of the baseline
        // (lower edge of checkbox and baseline for text)

        // Setting up task measurements (including totals)
        int numTasks = taskGroup.getTasks().size();
        float taskHeight = fontBottom - fontTop;
        float paddedTaskHeight = taskHeight + 2 * padding;
        float totalTaskHeight = numTasks * paddedTaskHeight;
        float topOfTasks = yOrigin - totalTaskHeight / 2 - totalTaskHeight % 2;
        float bottomOfTasks = yOrigin + totalTaskHeight / 2;

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

        // Nudge each task to an unoccupied location and get total area of all grouped tasks
        ArrayList<Rect> touchAreas = new ArrayList<>();
        int counter = 0;
        for (Task task : taskGroup.getTasks()) {
            float yBaseline = task.getTaskGraphic().getBaseline();
            float yBaselineDest = topOfTasks - padding - fontTop + counter * paddedTaskHeight;
            float nudgeY = yBaselineDest - yBaseline;
            task.getTaskGraphic().move(0, (int) nudgeY);
            counter++;
            touchAreas.add(new Rect(task.getTaskGraphic().getTouchArea()));
        }
        Rect touchArea = unionRects(touchAreas);

        // Return graphics piece for combined tasks (no individual graphic information)
        new TaskGraphic(yOrigin, (float) touchArea.left,
                touchArea.left + checkBoxSide + spacing, touchArea);
    }

    // Given multiple touch areas, what is the smallest rectangle that encompasses all touch areas?
    protected Rect unionRects(@NotNull ArrayList<Rect> rects) {

        // Get the maximum of each dimension (or minimum, depending) as rectangle bounds
        Float left = null;
        Float top = null;
        Float right = null;
        Float bottom = null;
        for (Rect rect : rects) {
            left = (left == null ? rect.left : min(left, rect.left));
            top = (top == null ? rect.top : min(top, rect.top));
            right = (right == null ? rect.right : max(right, rect.right));
            bottom = (bottom == null ? rect.bottom : max(bottom, rect.bottom));
        }
        assert left != null;    // Not sure why this was necessary, but it was kicked out as error

        // Return the finished rectangle object
        return new Rect((int) left.floatValue(), (int) top.floatValue(),
                (int) right.floatValue(), (int) bottom.floatValue());
    }

    // Collects pairs of tasks whose touch areas overlap and turns them into a group of items
    // Any task that overlaps another task or a group becomes part of that group
    protected void overlappingTasks() {

        int start;
        boolean newPairingFound;
        ArrayList<TaskItem> taskItems = taskViewModel.getTaskItems();

        // Loop through possible pairs until two that overlap are found,
        // then queue the group to be created and the tasks to be moved to a group list
        do {
            ArrayList<TaskGroup> taskGroupsToAdd = new ArrayList<>();
            ArrayList<TaskItem> taskItemsToRemove = new ArrayList<>();
            start = 1;
            newPairingFound = false;

            // Make sure all the graphic information is up-to-date (including tasks in groups)
            for (TaskItem taskItem : taskItems) {
                taskItem.setTaskGraphic(setGraphics(taskItem));
                if (taskItem instanceof TaskGroup)
                    for (Task task : ((TaskGroup) taskItem).getTasks())
                        task.setTaskGraphic(setGraphics(task));
            }

            // Grouped items are "nudged" (moved up or down on vertical axis to prevent overlap)
            for (TaskItem taskItem : taskItems) {
                if (taskItem instanceof TaskGroup)
                    nudgeTasks((TaskGroup) taskItem);
            }

            // Loop through every possible group and/or task pair
            for (TaskItem taskItem : taskItems) {
                // (Note that the lower bound of this loop increases with the first loop.
                //   This is to prevent redundancy, ex: #1 & #2 vs #2 & #1.  Only first will occur)
                for (TaskItem otherTaskItem : taskItems.subList(start, taskItems.size())) {

                    // See if item touch areas overlap and group them together (groups and/or tasks)
                    Rect touchArea = taskItem.getTaskGraphic().getTouchArea();
                    Rect otherTouchArea = otherTaskItem.getTaskGraphic().getTouchArea();
                    if (touchArea.intersect(otherTouchArea)) {
                        newPairingFound = true; // Break while loop to start over with new info

                        // Combine into groups:
                        // (item + item, item + group, group + item, or group + group)
                        if (taskItem instanceof TaskGroup && otherTaskItem instanceof TaskGroup)
                            if (((TaskGroup) taskItem).getTasks().size() >=
                                    ((TaskGroup) otherTaskItem).getTasks().size())
                                ((TaskGroup) taskItem).setForceCombining(true);
                            else ((TaskGroup) otherTaskItem).setForceCombining(true);

                        // Mark groups to be added and solo tasks to be moved to/from master list
                        taskGroupsToAdd.add(new TaskGroup(taskItem, otherTaskItem));
                        taskItemsToRemove.add(taskItem);
                        taskItemsToRemove.add(otherTaskItem);
                    }
                    // Stop looking for pairs and process this pair
                    // NOTE: the reason we break the double for-loop instead of marking all
                    // combinations and processing them in the end is that grouping changes the
                    // initial conditions of the process, so we start over for each pair found
                    if (newPairingFound) break;
                }
                if (newPairingFound) break;
                if (start < taskItems.size())   // iterate start of second for loop
                    start++;
                else
                    break;  // no more pairs to consider
            }

            // Add new groups to the master list for groups and remove grouped tasks from master
            // list for tasks (because they are now a part of a group, eliminates redundancy)
            taskItems.addAll(taskGroupsToAdd);
            taskItems.removeAll(taskItemsToRemove);
        } while (newPairingFound);  // Keep going until no more overlaps are detected
        invalidate();   // force a re-draw
    }

    // DRAW FUNCTION (THE HEART OF THE CLASS AND MAY BE CALLED VERY FREQUENTLY)

    // Called by the view whenever an update to the graphics is warranted (automatic)
    // It sets up every graphic on the screen (except the background)
    protected void onDraw(Canvas canvas) {

        // Draw each task individually
        // Make sure the taskViewModel exists (not too early in program)
        if (taskViewModel != null) {
            // For each and every task...
            for (TaskItem taskItem : taskViewModel.getTaskItems()) {
                if (taskItem instanceof TaskGroup) {
                    if (((TaskGroup) taskItem).getNudging()) {
                        for (TaskItem groupTaskItem : ((TaskGroup) taskItem).getTasks()) {
                            drawTask(canvas, groupTaskItem);
                        }
                    } else
                        drawTask(canvas, taskItem);
                } else
                    drawTask(canvas, taskItem);
            }
            setupCanvas(canvas);    // Draw axes elements
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
        canvas.drawText(labelHorizontal, labelHorizontalDeltaX,
                labelHorizontalDeltaY, paintAxisLabels);

        // Drawing arrow for vertical axis
        canvas.drawLine(
                arrowVertical[0][0][0], arrowVertical[0][0][1],
                arrowVertical[0][1][0], arrowVertical[0][1][1],
                paintRect);
        canvas.drawLine(
                arrowVertical[1][0][0], arrowVertical[1][0][1],
                arrowVertical[1][1][0], arrowVertical[1][1][1],
                paintRect);
        canvas.drawLine(
                arrowVertical[2][0][0], arrowVertical[2][0][1],
                arrowVertical[2][1][0], arrowVertical[2][1][1],
                paintRect);

        // Drawing arrow for horizontal axis
        canvas.drawLine(
                arrowHorizontal[0][0][0], arrowHorizontal[0][0][1],
                arrowHorizontal[0][1][0], arrowHorizontal[0][1][1],
                paintRect);
        canvas.drawLine(
                arrowHorizontal[1][0][0], arrowHorizontal[1][0][1],
                arrowHorizontal[1][1][0], arrowHorizontal[1][1][1],
                paintRect);
        canvas.drawLine(
                arrowHorizontal[2][0][0], arrowHorizontal[2][0][1],
                arrowHorizontal[2][1][0], arrowHorizontal[2][1][1],
                paintRect);
    }

    // Draw the elements onto the canvas (because this function has the potential to be
    // called very frequently, no calculations or large allocations are performed here)
    protected void drawTask(Canvas canvas, @NotNull TaskItem taskItem) {

        TaskGraphic graphic = taskItem.getTaskGraphic();

        String label = taskItem.getLabel();

        // Pull the pre-determined position information for the task
        float yBaseline = graphic.getBaseline();
        float xCheckbox = graphic.getCheckBoxStart();
        float xText = graphic.getTextStart();

        // Draw checkbox for individual tasks
        if (taskItem instanceof Task) {
            // Draw the checkbox in the pre-determined position
            canvas.drawRect(
                    xCheckbox,
                    yBaseline - checkBoxSide,
                    xCheckbox + checkBoxSide,
                    yBaseline,
                    paintRect);
        }

        // Display cross (plus sign) instead of checkbox if it's a task group
        else {
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
        }

        // Display the label in the pre-determined position
        canvas.drawText(
                label,
                xText,
                yBaseline,
                paintText);

        // If task is completed, add a check mark
        if (taskItem instanceof Task && ((Task) taskItem).getCompleted()) {
            canvas.drawLine(xCheckbox + checkBoxSide,
                    (float) (yBaseline - (1.5 * checkBoxSide)),
                    (float) (xCheckbox + (0.5 * checkBoxSide)),
                    yBaseline,
                    paintCheckMark);
            canvas.drawLine((float) (xCheckbox + (0.5 * checkBoxSide)),
                    yBaseline,
                    xCheckbox,
                    (float) (yBaseline - (0.5 * checkBoxSide)),
                    paintCheckMark);
        }
    }

    // TOUCH RESPONSE FUNCTION (WHICH, IF ANY, TASK(S) WERE TOUCHED?)

    // This function returns a list of tasks that were touched by the user on the canvas
    public TaskItem getTouchedTaskItems(float x, float y) {

        // Loop through all tasks (groups, too) and see if touch coordinates are inside a touch area
        for (TaskItem taskItem : taskViewModel.getTaskItems()) {

            Rect touchArea;
            if (taskItem instanceof TaskGroup) {
                if (((TaskGroup) taskItem).getNudging()) {
                    for (Task task : ((TaskGroup) taskItem).getTasks()) {
                        touchArea = task.getTaskGraphic().getTouchArea();
                        if (touchArea.contains((int) x, (int) y))
                            return task;
                    }
                } else {
                    touchArea = taskItem.getTaskGraphic().getTouchArea();
                    if (touchArea.contains((int) x, (int) y))
                        return taskItem;
                }
            }

            // Grab the pre-determined touch area
            touchArea = taskItem.getTaskGraphic().getTouchArea();

            // If the tap coordinates were inside the touch area for a task, then return the task
            if (touchArea.contains((int) x, (int) y))
                return taskItem;
        }
        return null; // nothing hit if you've made it this far
    }
}