package com.rsquared.taskmaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

  // Add listener to button and call MainActivity to show form
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Button addNewTaskButton = requireActivity().findViewById(R.id.add_new_task_button);
    addNewTaskButton.setOnClickListener(
        l -> {
          ((MainActivity) requireActivity()).addTask(); // change bad to add task
        });
  }
}
