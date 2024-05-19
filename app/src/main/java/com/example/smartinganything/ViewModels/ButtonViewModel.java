package com.example.smartinganything.ViewModels;

import android.content.Context;
import android.widget.Toast;

import com.example.smartinganything.BluetoothHandling.BluetoothService;
import com.example.smartinganything.Database.ActionDataAccess;
import com.example.smartinganything.Database.ButtonDataAccess;
import com.example.smartinganything.RecyclerView.ButtonListRecyclerAdapter;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ButtonViewModel {

    public static final String[] STATUS = {"OFF", "ON"};


    public int id;
    public String name;
    public String state = STATUS[0];
    public int backgroundColor;
    public Action action;
    public ButtonListRecyclerAdapter adapter;
    public int action_id;
    public int position;
    Context context;

    //data to db
    public ButtonViewModel(String name, int backgroundColor) {
        this.name = name;
        this.state = STATUS[0];
        this.backgroundColor = backgroundColor;

    }

    public ButtonViewModel(Context context, String name, int backgroundColor, Action action) {
        this.name = name;
        this.state = STATUS[0];
        this.backgroundColor = backgroundColor;
        this.action = action;
        this.context = context;
    }

    //from db to model
    public ButtonViewModel(int id, String name, String state, int backgroundColor) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.backgroundColor = backgroundColor;
    }

    public ButtonViewModel(int id, String name, String state, int backgroundColor, Action action) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.backgroundColor = backgroundColor;
        this.action = action;
    }

    //from db to model
    public ButtonViewModel(Context context, int id, String name, String state, int backgroundColor, int action_id) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.backgroundColor = backgroundColor;
        this.action_id = action_id;
        this.context = context;
    }

    public static void ReceivedResponseMessageBT(String msg) {
        AtomicReference<ButtonViewModel> button = new AtomicReference<>();

        msg = msg.toLowerCase().replaceAll("\r\n", "");
        String btMsg = msg;

        Optional<ButtonViewModel> optional = ButtonDataAccess.buttonsWaitingResponseMessage.stream().filter(b -> Objects.equals(b.action.btRMSG, btMsg)).findFirst();
        optional.ifPresent(button::set);
        if (button.get() == null) return;
        ButtonViewModel btn = button.get();
        btn.changeState();
        new ButtonDataAccess(btn.context).updateButton(btn);
        btn.adapter.notifyItemChanged(btn.position);
        btn.adapter = null;
        ButtonDataAccess.buttonsWaitingResponseMessage.remove(btn);
    }

    public ButtonListRecyclerAdapter getAdapter() {
        return adapter;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Action getAction() {
        action = new ActionDataAccess(context).getAction(this.action_id);
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getOnMessage() {
        if (action != null) {
            return action.btOnMSG;
        }

        return "";
    }

    public String getOffMessage() {
        if (action != null) {
            return action.btOffMSG;
        }
        return "";
    }

    public boolean isState(String state) {
        return state.equals(this.state);
    }

    public boolean changeState() {
        if (state.equals(STATUS[0])) {
            state = STATUS[1];

            return true;
        } else if (state.equals(STATUS[1])) {
            state = STATUS[0];
            return true;
        }

        return false;
    }

    public void SendSwitchingMessage(BluetoothService bluetoothService, ButtonListRecyclerAdapter adapter, int position) {
        if (BluetoothService.STATE != BluetoothService.STATUS_CONNECTED) {
            Toast.makeText(bluetoothService.context, "Not Connected!", Toast.LENGTH_LONG).show();
            return;
        }
        boolean isSent = false;

        context = bluetoothService.context;
        this.action = new ActionDataAccess(bluetoothService.context).getAction(this.action_id);
        if (this.state.equals(STATUS[0])) {
            bluetoothService.sendData(getOnMessage());
        } else if (this.state.equals(STATUS[1])) {
            bluetoothService.sendData(getOffMessage());
        }

        this.adapter = adapter;
        this.position = position;
        ButtonDataAccess.buttonsWaitingResponseMessage.add(this);
    }

    public boolean editButton(String name, int backgroundColor) {
        if (name != null && backgroundColor != 0) {
            this.name = name;
            this.backgroundColor = backgroundColor;
            return true;
        }
        return false;
    }

    public boolean editAction(Action action) {
        if (action != null) {
            this.action = action;
            return true;
        }
        return false;
    }

    public boolean editAllProps(String name, int backgroundColor, Action action, String state) {
        if (name != null && backgroundColor != 0 && action != null && state != null) {
            if (state == STATUS[0]) this.state = STATUS[0];
            else if (state == STATUS[1]) this.state = STATUS[1];
            else return false;

            this.name = name;
            this.action = action;
            this.backgroundColor = backgroundColor;
        }
        return false;

    }

}
