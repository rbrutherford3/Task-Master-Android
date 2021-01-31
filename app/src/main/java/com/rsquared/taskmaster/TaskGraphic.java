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

	public Float getCheckBoxStart() {
		return checkBoxStart;
	}

	public Float getTextStart() {
		return textStart;
	}

	public Rect getTouchArea() {
		return touchArea;
	}

	// SETTER METHODS

	public void setBaseline(Float y) {
		this.baseline = y;
	}

	public void setCheckBoxStart(Float x) {
		this.checkBoxStart = x;
	}

	public void setTextStart(Float x) {
		this.textStart = x;
	}

	public void setTouchArea(Rect area) {
		this.touchArea = area;
	}

	public void move(int dx, int dy) {
		setBaseline(getBaseline() + dy);
		setCheckBoxStart(getCheckBoxStart() + dx);
		setTextStart(getTextStart() + dx);
		getTouchArea().offset(dx, dy);
	}

	// DEBUGGING FUNCTIONS

	public void debug() {
		System.out.println("CONTENTS OF TaskGraphic INSTANCE:");
		System.out.println("-baseline: " + baseline);
		System.out.println("-checkBoxStart: " + checkBoxStart);
		System.out.println("-textStart: " + textStart);
		System.out.println("-touchArea: (no good way to display)");
	}
}