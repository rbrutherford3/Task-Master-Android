package com.rsquared.taskmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

//todo: fitting items together in a crowded area

// Class to perform all graphic operations (drawing on canvas, etc)
public class TaskDraw extends View {

    // INITIALIZE PRIVATE MEMBERS

    // Paint objects used for drawing on canvas
    private Paint paintRect;
    private Paint paintCheckBox;
    private Paint paintText;

    // Constants for sizing
    private final int rectSide = 20;
    private final int margin = 10;
    private final int strokeWidth = 2;
    private final int textSize = 30;

    // Canvas dimensions (should be the same -> square)
    private int height;
    private int width;

    // Store a taskViewModel passed in from MainActivity, because views cannot initiate view models
    private TaskViewModel taskViewModel;

    // CONSTRUCTOR

    // requires no special parameters or functions.  Sets up paint objects.
    public TaskDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaintRect();
        setupPaintText();
    }

    // SETTER FUNCTIONS

    // Store the taskViewModel to refer to and write to database and other stored items
    public void setTaskViewModel(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
    }

    // Set up paint objects with color and stroke styles
    private void setupPaintRect() {
        paintRect = new Paint();
        paintRect.setColor(Color.WHITE);
        paintRect.setAntiAlias(true);
        paintRect.setStrokeWidth(strokeWidth);
        paintRect.setStyle(Paint.Style.STROKE);
        paintRect.setStrokeJoin(Paint.Join.ROUND);
        paintRect.setStrokeCap(Paint.Cap.ROUND);

        paintCheckBox = new Paint(paintRect);
        paintCheckBox.setStrokeWidth(strokeWidth*2);
    }

    // Set up text paint object with color and size
    private void setupPaintText() {
        paintText = new Paint();
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(textSize);
    }

    // Get the overall dimensions of the graphic
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // GETTER FUNCTIONS

    // Function to determine the relative position on the urgency vs importance graphic
    private float[] getPercentCoordinates(Task task) {
        int importance = task.getImportance();
        int urgency = task.getUrgency();
        float x = (100 - (float) urgency) / 100;
        float y = (100 - (float) importance) / 100;
        return new float[]{x, y};
    }

    // Function to determine the absolute distance position on the urgency vs importance graphic
    public float[] getPixelCoordinates(Task task) {
        float [] percentCoordinates = getPercentCoordinates(task);
        float x = percentCoordinates[0] * this.getWidth();
        float y = percentCoordinates[1] * this.getHeight();
        return new float[]{x, y};
    }

    // SETUP FUNCTIONS

    // Function to get all the necessary dimensions for the task label, check box, and check mark.
    // These metrics are stored in hash maps in taskViewModel to be pulled during an 'ondraw()' call
    public void initTask(Task task) {

        // Get the position on the canvas for the given task
        float[] coordinates = getPixelCoordinates(task);
        float xCheckbox = coordinates[0]; // The horizontal position of the left side of the checkbox
        float yBaseline = coordinates[1]; // The vertical position of the baseline
                                            // (lower edge of checkbox and baseline for text)

        // Create the dimensions for the task text on the canvas
        Rect rectText = new Rect(); // Outlines the text

        // Get text dimensions using two different approaches
        paintText.getTextBounds(task.getTask(), 0, task.getTask().length(), rectText);
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();

        // width from checkbox to end of text:
        int layoutWidth = rectSide + margin + rectText.width();

        // Determine which is higher: the text or the checkbox
        float layoutHighest;
        if (Math.abs(fontMetrics.top) > rectSide) {
            layoutHighest = Math.abs(fontMetrics.top);
        }
        else
            layoutHighest = rectSide;
        float layoutLowest = Math.abs(fontMetrics.bottom);  // Text will always be lowest

        // Adjust position of baseline if encroaching on the upper edge of the canvas
        if (yBaseline - layoutHighest < margin)
            yBaseline = layoutHighest + margin;

        // Adjust position of baseline if encroaching on the lower edge of the canvas
        if (yBaseline + layoutLowest > height - margin)
            yBaseline = height - layoutLowest - margin;

        float xText; // the starting horizontal position of text

        // Adjust horizontal positions if encroaching on the left or right edges of the screen
        if (xCheckbox + layoutWidth > width - margin) {
            if (xCheckbox + rectSide > width - margin)  // test checkbox without text
                xCheckbox = width - margin - rectSide;
            xText = xCheckbox - margin - rectText.width();
        }
        else {
            if (xCheckbox < margin)
                xCheckbox = margin;
            xText = xCheckbox + rectSide + margin;
        }

        // Create one rectangle that covers the whole area (checkbox + text) for touch screen
        Rect touchArea;
        if (xCheckbox < xText)
            touchArea = new Rect(
                    (int) xCheckbox,
                    (int) (yBaseline - layoutHighest),
                    (int) xCheckbox + layoutWidth,
                    (int) (yBaseline + layoutLowest));
        else
            touchArea = new Rect(
                    (int) xText,
                    (int) (yBaseline - layoutHighest),
                    (int) xText + layoutWidth,
                    (int) (yBaseline + layoutLowest));

        // Update measurements in taskViewModel (so they don't have to be recalculated on drawing)
        long id = task.getID();
        taskViewModel.setBaseline(id, yBaseline);
        taskViewModel.setTextStart(id, xText);
        taskViewModel.setCheckBoxStart(id, xCheckbox);
        taskViewModel.setTouchArea(id, touchArea);
    }

    // Initialize all the tasks at once (for beginning of program)
    public void initTasks() {
        for (Task task : taskViewModel.getTasks())
            initTask(task);
    }

    // DRAW FUNCTION (THE HEART OF THE CLASS AND MAY BE CALLED VERY FREQUENTLY)

    // Actually draw the elements onto the canvas (because this function has the potential to be
    // called very frequently, we are not doing any calculations or large allocations here)
    protected void onDraw(Canvas canvas) {

        // Make sure teh taskViewModel exists (not too early in program)
        if (taskViewModel != null) {
            // For each and every task...
            for (Task task : taskViewModel.getTasks()) {

                // Pull the pre-determined position information for the task
                long id = task.getID();
                float yBaseline = taskViewModel.getBaseline(id);
                float xCheckbox = taskViewModel.getCheckBoxStart(id);
                float xText = taskViewModel.getTextStart(id);

                // Draw the checkbox in the pre-determined position
                canvas.drawRect(xCheckbox,
                        yBaseline - rectSide,
                        xCheckbox + rectSide,
                        yBaseline,
                        paintRect);
                // Display the text in the pre-determined position
                canvas.drawText(task.getTask(),
                        xText,
                        yBaseline,
                        paintText);

                // If task is completed, add a check mark
                if (task.getCompleted()) {
                    canvas.drawLine(xCheckbox + rectSide,
                            (float) (yBaseline - (1.5 * rectSide)),
                            (float) (xCheckbox + (0.5 * rectSide)),
                            yBaseline,
                            paintCheckBox);
                    canvas.drawLine((float) (xCheckbox + (0.5 * rectSide)),
                            yBaseline,
                            xCheckbox,
                            (float) (yBaseline - (0.5 * rectSide)),
                            paintCheckBox);
                }
            }
        }
    }

    // TOUCH RESPONSE FUNCTION (WHICH, IF ANY, TASK(S) WERE TOUCHED?)

    // This function returns a list of tasks that were touched by the user on the canvas
    public ArrayList<Task> touchedTasks(float x, float y) {

        // Store touched tasks here
        ArrayList<Task> touchedTasks = new ArrayList<>();

        // Loop through all texts
        for (Task task : taskViewModel.getTasks()) {

            // Grab the pre-determined touch area and widen it a little
            long id = task.getID();
            Rect touchArea = taskViewModel.getTouchArea(id);
            touchArea.inset(-rectSide, -rectSide);

            // If the tap coordinates were inside the touch area for a task, then save the task
            if (touchArea.contains((int)x, (int)y))
                touchedTasks.add(task);
        }
        return touchedTasks;
    }
}