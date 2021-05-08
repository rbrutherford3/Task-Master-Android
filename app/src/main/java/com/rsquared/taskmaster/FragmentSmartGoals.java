package com.rsquared.taskmaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Simple fragment to display SMART goal explanation
public class FragmentSmartGoals extends Fragment {

	// Provide an instance of this class
	@Contract(" -> new")
	public static @NotNull FragmentSmartGoals newInstance() {
		return new FragmentSmartGoals();
	}

	// OVER-RIDDEN LIFECYCLE METHODS

	// Only code that's necessary (fragment simply displays static text)
	@Override
	public View onCreateView(
			@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_smart_goals, container, false);
	}
}
