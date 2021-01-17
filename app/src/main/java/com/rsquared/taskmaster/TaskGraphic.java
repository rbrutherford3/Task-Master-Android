package com.rsquared.taskmaster;

import android.graphics.Rect;

// Class for task information that is graphical in nature (coordinates, shapes)
public class TaskGraphic {

	// PRIVATE MEMBERS

	private Float baseline;             // y-value of bottom of task text and checkbox
	private Float checkBoxStart;        // x-value of the beginning (left side) of task checkbox
	private Float textStart;            // x-value of the beginning of task text
	private Rect touchArea;             // rectangular area around task graphic for touch response

	// CONSTRUCTORS

	public TaskGraphic() {
	}

	public TaskGraphic(Float baseline, Float checkBoxStart, Float textStart, Rect touchArea) {
		this.baseline = baseline;
		this.checkBoxStart = checkBoxStart;
		this.textStart = textStart;
		this.touchArea = touchArea;
	}

	// GETTER METHODS

	public Float getBaseline() {
		return baseline;
	}

	public void setBaseline(Float y) {
		baseline = y;
	}

	public Float getCheckBoxStart() {
		return checkBoxStart;
	}

	public void setCheckBoxStart(Float x) {
		checkBoxStart = x;
	}


	// SETTER METHODS

	public Float getTextStart() {
		return textStart;
	}

	public void setTextStart(Float x) {
		textStart = x;
	}

	public Rect getTouchArea() {
		return touchArea;
	}

	public void setTouchArea(Rect area) {
		touchArea = area;
	}

	public void move(int dx, int dy) {
		setBaseline(getBaseline() + dy);
		setCheckBoxStart(getCheckBoxStart() + dx);
		setTextStart(getTextStart() + dx);
		getTouchArea().offset(dx, dy);
	}

	public void debug() {
		System.out.println("CONTENTS OF TaskGraphic INSTANCE:");
		System.out.println("-baseline: " + baseline);
		System.out.println("-checkBoxStart: " + checkBoxStart);
		System.out.println("-textStart: " + textStart);
		System.out.println("-touchArea: (no good way to display)");
	}
}