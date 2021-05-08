package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

// Fragment for button to show form for adding a new task
public class FragmentAddButton extends Fragment {

  // Provide an instance of this class
  public static @NotNull FragmentAddButton newInstance() {
    return new FragmentAddButton();
  }

  // Inflate layout
  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_add_button, container, false);
  }

  // Add listener to button and call MainActivity to show form, plus rotation button
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Create variables for buttons
    Button addNewTaskButton = requireActivity().findViewById(R.id.add_new_task_button);
    Button rotateScreenButton = requireActivity().findViewById(R.id.rotate_screen_button);

    // Pass a new task along to the new/edit task form
    addNewTaskButton.setOnClickListener(
        (View l) -> ((MainActivity) FragmentAddButton.this.requireActivity()).addTask());

    // Call rotation function whenever rotation button is hit
    rotateScreenButton.setOnClickListener(
        (View l) -> ((MainActivity) requireActivity()).rotate()); // Rotate screen

    // Only allow rotation button to be clicked if NOT auto-rotate
    requireActivity().findViewById(R.id.rotate_screen_button).setEnabled(
        Settings.System.getInt(requireActivity().getContentResolver(),
        Settings.System.ACCELEROMETER_ROTATION, 0) != 1);
  }
}
