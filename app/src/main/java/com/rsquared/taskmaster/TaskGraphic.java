package com.rsquared.taskmaster;

import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

// Class for task information that is graphical in nature (coordinates, shapes)
public class TaskGraphic {

	// PRIVATE MEMBERS

	private Float baseline;             // y-value of bottom of task text and checkbox
	private Float checkBoxStart;        // x-value of the beginning (left side) of task checkbox
	private Float textStart;            // x-value of the beginning of task text
	private Rect touchArea;             // rectangular area around task graphic for touch response

	// CONSTRUCTORS

	public TaskGraphic(Float newBaseline, Float newCheckBoxStart, Float newTextStart, Rect newTouchArea) {
		setBaseline(newBaseline);
		setCheckBoxStart(newCheckBoxStart);
		setTextStart(newTextStart);
		setTouchArea(newTouchArea);
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
		baseline = y;
	}

	public void setCheckBoxStart(Float x) {
		checkBoxStart = x;
	}

	public void setTextStart(Float x) {
		textStart = x;
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

	// DEBUGGING FUNCTIONS

	@Override
	public @NotNull String toString() {
		String output = "";
		output = output.concat("CONTENTS OF TaskGraphic INSTANCE:\n");
		output = output.concat("-baseline: " + baseline + "\n");
		output = output.concat("-checkBoxStart: " + checkBoxStart + "\n");
		output = output.concat("-textStart: " + textStart + "\n");
		output = output.concat("-touchArea: (no good way to display)\n");
		return output;
	}
}