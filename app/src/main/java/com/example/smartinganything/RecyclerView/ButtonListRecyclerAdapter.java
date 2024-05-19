package com.example.smartinganything.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartinganything.AddButtonFormFragment;
import com.example.smartinganything.BluetoothHandling.BluetoothService;
import com.example.smartinganything.ButtonFragment;
import com.example.smartinganything.Database.ButtonDataAccess;
import com.example.smartinganything.R;
import com.example.smartinganything.ViewModels.ButtonViewModel;

import java.util.ArrayList;
import java.util.List;

public class ButtonListRecyclerAdapter extends RecyclerView.Adapter<ButtonListRecyclerAdapter.ButtonViewHolder> {

    List<String> name = new ArrayList<>();
    List<ButtonViewModel> buttonData = new ArrayList<>();
    Context context;
    PopupMenu popupMenu;
    ButtonFragment buttonFragment;
    ButtonViewModel buttonModel;
    ButtonDataAccess buttonDataAccess;
    BluetoothService bluetoothService;

    public ButtonListRecyclerAdapter(ButtonDataAccess buttonDataAccess, BluetoothService bluetoothService, ButtonFragment buttonFragment) {
        this.buttonDataAccess = buttonDataAccess;
        this.bluetoothService = bluetoothService;
        this.buttonFragment = buttonFragment;
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_item, parent, false);
        context = parent.getContext();

        return new ButtonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        buttonModel = buttonData.get(position);
        int color_gray = ContextCompat.getColor(context, R.color.background_gray);
        int color_main = buttonModel.backgroundColor;
        holder.buttonName.setText(buttonModel.name);
        holder.buttonState.setText(buttonModel.state);


        if (buttonModel.isState(ButtonViewModel.STATUS[0])) {
            //holder.buttonContainer.setBackgroundColor(ContextCompat.getColor(context,R.color.background_gray));
            holder.buttonState.setTextColor(Color.WHITE);
            holder.buttonName.setTextColor(Color.WHITE);
            ObjectAnimator animatorReturn = ObjectAnimator.ofInt(holder.buttonContainer, "backgroundColor", color_gray, color_main);
            animatorReturn.setEvaluator(new ArgbEvaluator());
            animatorReturn.setDuration(300);
            animatorReturn.start();
        } else {
            //holder.buttonContainer.setBackgroundColor(buttonModel.backgroundColor);
            holder.buttonState.setTextColor(Color.BLACK);
            holder.buttonName.setTextColor(Color.BLACK);
            ObjectAnimator animator = ObjectAnimator.ofInt(holder.buttonContainer, "backgroundColor", color_main, color_gray);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setDuration(300);
            animator.start();
        }

        holder.buttonContainer.setOnClickListener(clickedButton(position));
        holder.buttonContainer.setOnLongClickListener(onLongClickListener((position)));

    }

    View.OnClickListener clickedButton(int position) {
        return v -> {
            ButtonViewModel buttonModel = buttonData.get(position);
            buttonModel.SendSwitchingMessage(bluetoothService, this, position);

        };
    }

    View.OnLongClickListener onLongClickListener(int position) {
        return v -> {

            popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.edit_button_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {

                int itemId = item.getItemId();
                if (itemId == R.id.edit_button) {
                    AddButtonFormFragment addButtonFormFragment = new AddButtonFormFragment(this, buttonDataAccess, buttonData.get(position));
                    addButtonFormFragment.show(buttonFragment.getParentFragmentManager(), AddButtonFormFragment.TAG);
                    return true;
                } else if (itemId == R.id.delete_button) {
                    buttonDataAccess.deleteButton(buttonData.get(position));
                    this.notifyDataSetChanged();
                    return true;

                }
                return false;
            });
            popupMenu.show();

            return true;
        };
    }

    @Override
    public int getItemCount() {
        buttonData = ButtonDataAccess.buttonDataList;
        return buttonData.size();
    }

    public class ButtonViewHolder extends RecyclerView.ViewHolder {

        public final TextView buttonName;
        public final TextView buttonState;
        public final ConstraintLayout buttonContainer;


        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            buttonName = itemView.findViewById(R.id.button_name);
            buttonState = itemView.findViewById(R.id.button_state);
            buttonContainer = itemView.findViewById(R.id.container_button);
        }
    }
}
