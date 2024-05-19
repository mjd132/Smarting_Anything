package com.example.smartinganything;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.smartinganything.Database.ActionDataAccess;
import com.example.smartinganything.Database.ButtonDataAccess;
import com.example.smartinganything.RecyclerView.ButtonListRecyclerAdapter;
import com.example.smartinganything.Utility.ButtonGenerator;
import com.example.smartinganything.ViewModels.Action;
import com.example.smartinganything.ViewModels.ButtonViewModel;
import com.example.smartinganything.databinding.FragmentAddButtonFormBinding;


public class AddButtonFormFragment extends DialogFragment {

    public static final String TAG = "Add Button Form";
    FragmentAddButtonFormBinding binding;

    ActionDataAccess actionDataAccess;
    ButtonDataAccess buttonDataAccess;
    ButtonListRecyclerAdapter adapter;
    ButtonViewModel buttonViewModel;
    boolean isEdit = false;

    public AddButtonFormFragment(ButtonListRecyclerAdapter adapter, ButtonDataAccess buttonDataAccess, ButtonViewModel buttonViewModel) {
        this.buttonDataAccess = buttonDataAccess;
        this.adapter = adapter;
        this.buttonViewModel = buttonViewModel;
        isEdit = true;
    }

    public AddButtonFormFragment(ButtonListRecyclerAdapter adapter, ButtonDataAccess buttonDataAccess) {
        this.adapter = adapter;
        this.buttonDataAccess = buttonDataAccess;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionDataAccess = new ActionDataAccess(getContext());
//        buttonDataAccess = new ButtonDataAccess(getContext());
    }


    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                // get the size of the screen
                DisplayMetrics displayMetrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;

                // set the width of the dialog to 90% of the screen width
                int dialogWidth = (int) (screenWidth * 0.9);
                window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddButtonFormBinding.inflate(inflater, container, false);

        if (isEdit) {
            if (buttonViewModel == null) dismiss();
            Action action = buttonViewModel.getAction();
            binding.messageOn.setText(action.btOnMSG);
            binding.messageResponse.setText(action.btRMSG);
            binding.messageOff.setText(action.btOffMSG);
            binding.name.setText(buttonViewModel.name);
        }

        binding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.name.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Name is empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (binding.messageOff.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "ATTENTION: Message for turning off is empty !!", Toast.LENGTH_LONG).show();
                }
                if (binding.messageOn.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "ATTENTION: Message for turning on is empty !!", Toast.LENGTH_LONG).show();
                }


                if (isEdit) {
                    editButton();
                } else {
                    addButton();
                }


            }
        });


        return binding.getRoot();
    }

    private void editButton() {
        Action action = buttonViewModel.getAction();
        ActionDataAccess actionDataAccess = new ActionDataAccess(getContext());
        action.btOnMSG = binding.messageOn.getText().toString();
        action.btOffMSG = binding.messageOff.getText().toString();
        action.btRMSG = binding.messageResponse.getText().toString();
        int row = actionDataAccess.updateAction(action);

        buttonViewModel.setName(binding.name.getText().toString());
        buttonDataAccess.updateButton(buttonViewModel);

        clearEditText();
        adapter.notifyDataSetChanged();
        dismiss();

    }

    private void addButton() {
        Action action = new Action();
        action.btOnMSG = binding.messageOn.getText().toString();
        action.btOffMSG = binding.messageOff.getText().toString();
        action.btRMSG = binding.messageResponse.getText().toString();
        long row = actionDataAccess.createAction(action);
        action.id = actionDataAccess.getLastID(row);

        ButtonViewModel button = new ButtonViewModel(getContext(), binding.name.getText().toString(), ButtonGenerator.generateRandomColor(), action);
        buttonDataAccess.addNewButton(button);
        clearEditText();
        adapter.notifyItemInserted((int) row);
        dismiss();
    }

    private void clearEditText() {
        binding.name.setText("");
        binding.messageOff.setText("");
        binding.messageOn.setText("");
        binding.messageResponse.setText("");
    }
}