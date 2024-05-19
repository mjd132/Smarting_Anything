package com.example.smartinganything;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.smartinganything.BluetoothHandling.BluetoothService;
import com.example.smartinganything.databinding.FragmentActionBinding;


public class ActionFragment extends Fragment {

    public static StringBuilder messagesReceived = new StringBuilder();
    FragmentActionBinding binding;
    EditText messageEdit;
    Button senderButton;
    TextView console;
    BluetoothService bluetoothService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentActionBinding.inflate(inflater, container, false);
        messageEdit = binding.messageEditText;
        senderButton = binding.sendButton;
        console = binding.consoleTextView;
        console.setText(messagesReceived.toString());
        if (((MainActivity) requireActivity()).bluetoothService != null) {
            bluetoothService = ((MainActivity) requireActivity()).bluetoothService;
        }


        senderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothService.STATE == BluetoothService.STATUS_CONNECTED) {
                    String msg = messageEdit.getText().toString();

                    bluetoothService.sendData(msg);
                    messageEdit.setText("");
                } else {
                    Toast.makeText(getContext(), "Not Connect!", Toast.LENGTH_LONG).show();
                }

            }
        });


        return binding.getRoot();
    }

    public void receiveMessage() {
        console.setText(messagesReceived.toString());
    }
}