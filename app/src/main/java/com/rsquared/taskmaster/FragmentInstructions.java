package com.rsquared.taskmaster;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Simple fragment to display text instructions
public class FragmentInstructions extends Fragment {

    // Provide an instance of this class
    @Contract(" -> new")
    public static @NotNull FragmentInstructions newInstance() {
        return new FragmentInstructions();
    }

    // OVER-RIDDEN LIFECYCLE METHODS

    // Only code that's necessary (fragment simply displays static text)
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instructions, container, false);
    }
}