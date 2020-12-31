package com.rsquared.taskmaster;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Simple fragment to display text instructions
public class FragmentInstructions extends Fragment {

    // CONSTRUCTORS

    // Required empty public constructor
    public FragmentInstructions() {}

    // Provide an instance of this class
    public static FragmentInstructions newInstance() {
        return new FragmentInstructions();
    }

    // OVER-RIDDEN LIFECYCLE METHODS

    // Only code that's necessary (fragment simply displays static text)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instructions, container, false);
    }
}