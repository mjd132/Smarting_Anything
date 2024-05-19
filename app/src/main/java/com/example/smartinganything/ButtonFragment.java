package com.example.smartinganything;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartinganything.BluetoothHandling.BluetoothService;
import com.example.smartinganything.Database.ButtonDataAccess;
import com.example.smartinganything.RecyclerView.AutoFitGridLayoutManager;
import com.example.smartinganything.RecyclerView.ButtonListRecyclerAdapter;
import com.example.smartinganything.ViewModels.ButtonViewModel;
import com.example.smartinganything.databinding.FragmentButtonBinding;

import java.util.ArrayList;
import java.util.List;


public class ButtonFragment extends Fragment {

    PopupMenu popupMenu;
    List<ButtonViewModel> buttonsList = new ArrayList<>();
    ButtonDataAccess buttonDataAccess;
    ButtonListRecyclerAdapter adapter;
    BluetoothService bluetoothService;
    RecyclerView rcButton;
    private GridLayout buttonContainer;
    private FragmentButtonBinding binding;
    private Parcelable recyclerViewState;


    public ButtonFragment(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonDataAccess = new ButtonDataAccess(getActivity());

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentButtonBinding.inflate(inflater, container, false);
        int highlightColor = Color.WHITE;
        int DURATION = 300;
        rcButton = binding.buttonRecyclerView;
        AutoFitGridLayoutManager autoFitGridLayoutManager = new AutoFitGridLayoutManager(getContext(), 2);
        adapter = new ButtonListRecyclerAdapter(buttonDataAccess, bluetoothService, this);

        rcButton.setItemAnimator(new DefaultItemAnimator());
        rcButton.setLayoutManager(autoFitGridLayoutManager);
        rcButton.setAdapter(adapter);

        AddButtonFormFragment addButtonFormFragment = new AddButtonFormFragment(adapter, buttonDataAccess);

        binding.addFAButton.setOnClickListener(v -> {
            addButtonFormFragment.show(getParentFragmentManager(), AddButtonFormFragment.TAG);
        });

        return binding.getRoot();

    }

    public void editButton(ButtonViewModel buttonViewModel) {
        AddButtonFormFragment addButtonFormFragment = new AddButtonFormFragment(adapter, buttonDataAccess, buttonViewModel);
        addButtonFormFragment.show(getParentFragmentManager(), AddButtonFormFragment.TAG);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}