package com.rsquared.taskmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

// Class for popup view that generates when the user taps on a group (uses TaskDraw as model)
public class GroupPopup extends TaskDraw {

	// INITIALIZE PRIVATE MEMBERS

	private final int padding = 20;   // dp
	private final int borderColor = 0xFF000000;
	private final int borderThickness = 2;
	private TaskGroup taskGroup;
	private Rect groupArea;
	private int backgroundColor;

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
		setGraphic();
		prepareCanvas();
	}

	// User defined group
	public void setGroup(TaskGroup taskGroup) {
		this.taskGroup = taskGroup;
	}

	// Set up popup area by moving everything to the center
	public void setGraphic() {

		// Get the minimum size of the popup by joining each tasks' touch area
		groupArea = new Rect(taskGroup.getTasks().get(0).getTaskGraphic().getTouchArea());
		for (Task task : this.taskGroup.getTasks().subList(1, taskGroup.getTasks().size())) {
			groupArea.union(new Rect(task.getTaskGraphic().getTouchArea()));
		}

		// Determine where to move each task based on the area gathered above
		float deltaX = -groupArea.left + padding;
		float deltaY = -groupArea.top + padding;
		for (Task task : taskGroup.getTasks())
			task.getTaskGraphic().move((int) deltaX, (int) deltaY);
	}

	// Set up popup background color, border thickness, dimensions, etc
	public void prepareCanvas() {

		// Determine background color of popup based on group location on taskDraw
		backgroundColor = getColor(taskGroup.getImportance(), taskGroup.getUrgency());
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
		params.height = (int) (groupArea.bottom - groupArea.top + 2 * padding);
		params.width = (int) (groupArea.right - groupArea.left + 2 * padding);
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
		if (taskGroup != null)
			for (Task task : taskGroup.getTasks())
				drawTask(canvas, task);
	}

	// Return a task after touching an area by looping through each task and checking
	public Task getTouchedTask(float x, float y) {

		// Loop through all tasks
		for (Task task : taskGroup.getTasks()) {

			// If the tap coordinates were inside the touch area for a task, then save the task
			if (task.getTaskGraphic().getTouchArea().contains((int) x, (int) y))
				return task;
		}
		return null; // nothing hit if you've made it this far
	}

	// Determine color of popup based on the groups' relative location on the taskdraw canvas
	// A 3-value gradient can be interpreted as 4 2-color gradients, four quadrants w/ four corners
	public int getColor(int importance, int urgency) {

		// Get colors and derive mixed colors
		int red = ContextCompat.getColor(getContext(), R.color.highest);
		int green = ContextCompat.getColor(getContext(), R.color.lowest);
		int yellow = ContextCompat.getColor(getContext(), R.color.middle);

		// Divide each color into color bands

		int redA = Color.alpha(red);
		int redR = Color.red(red);
		int redG = Color.green(red);
		int redB = Color.blue(red);

		int greenA = Color.alpha(green);
		int greenR = Color.red(green);
		int greenG = Color.green(green);
		int greenB = Color.blue(green);

		int yellowA = Color.alpha(yellow);
		int yellowR = Color.red(yellow);
		int yellowG = Color.green(yellow);
		int yellowB = Color.blue(yellow);

		// Compile intermediate colors by averaging in each band

		int yellowGreenA = (yellowA + greenA) / 2;
		int yellowGreenR = (yellowR + greenR) / 2;
		int yellowGreenG = (yellowG + greenG) / 2;
		int yellowGreenB = (yellowB + greenB) / 2;

		int yellowRedA = (yellowA + redA) / 2;
		int yellowRedR = (yellowR + redR) / 2;
		int yellowRedG = (yellowG + redG) / 2;
		int yellowRedB = (yellowB + redB) / 2;

		// Compile intermediate colors

		int yellowGreen = (yellowGreenA << 24 | yellowGreenR << 16 | yellowGreenG << 8 | yellowGreenB);
		int yellowRed = (yellowRedA << 24 | yellowRedR << 16 | yellowRedG << 8 | yellowRedB);

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
				upperLeft = red;
				upperRight = yellowRed;
				lowerRight = yellow;
				lowerLeft = yellowRed;
			} else {
				xWeight = urgency / (float) 50;
				upperLeft = yellowRed;
				upperRight = yellow;
				lowerRight = yellowGreen;
				lowerLeft = yellow;
			}
		} else {
			yWeight = importance / (float) 50;
			if (urgency < 50) {
				xWeight = urgency / (float) 50;
				upperLeft = yellow;
				upperRight = yellowGreen;
				lowerRight = green;
				lowerLeft = yellowGreen;
			} else {
				xWeight = (urgency - 50) / (float) 50;
				upperLeft = yellowRed;
				upperRight = yellow;
				lowerRight = yellowGreen;
				lowerLeft = yellow;
			}
		}

		// Given quadrant, apply two-factor weight to the color of each corner to get final color

		int resultA = (int) ((Color.alpha(upperLeft) * (yWeight * xWeight)) +
				(Color.alpha(lowerRight) * (1 - xWeight) * (1 - yWeight)) +
				(Color.alpha(upperRight) * yWeight * (1 - xWeight)) +
				(Color.alpha(lowerLeft) * (1 - yWeight) * xWeight));

		int resultR = (int) ((Color.red(upperLeft) * (yWeight * xWeight)) +
				(Color.red(lowerRight) * (1 - xWeight) * (1 - yWeight)) +
				(Color.red(upperRight) * yWeight * (1 - xWeight)) +
				(Color.red(lowerLeft) * (1 - yWeight) * xWeight));

		int resultG = (int) ((Color.green(upperLeft) * (yWeight * xWeight)) +
				(Color.green(lowerRight) * (1 - xWeight) * (1 - yWeight)) +
				(Color.green(upperRight) * yWeight * (1 - xWeight)) +
				(Color.green(lowerLeft) * (1 - yWeight) * xWeight));

		int resultB = (int) ((Color.blue(upperLeft) * (yWeight * xWeight)) +
				(Color.blue(lowerRight) * (1 - xWeight) * (1 - yWeight)) +
				(Color.blue(upperRight) * yWeight * (1 - xWeight)) +
				(Color.blue(lowerLeft) * (1 - yWeight) * xWeight));

		// Compile color and return result
		return (resultA << 24 | resultR << 16 | resultG << 8 | resultB);
	}
}