package com.example.smartinganything.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartinganything.DeviceListFragment;
import com.example.smartinganything.R;

import java.util.List;

public class DeviceListRecyclerAdapter extends RecyclerView.Adapter<DeviceListRecyclerAdapter.ViewHolder> {

    public static final int PAIRED_VIEW_TYPE = 0;
    public static final int DISCOVERED_VIEW_TYPE = 1;
    private final DeviceListFragment fragment;
    private final List<DeviceListFragment.Device> mDevices;

    public DeviceListRecyclerAdapter(DeviceListFragment context, List<DeviceListFragment.Device> mDevices) {
        this.fragment = context;
        this.mDevices = mDevices;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(fragment.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(v);

    }

    public void addDevice(DeviceListFragment.Device device) {
        mDevices.add(device);
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.device = mDevices.get(position);

        holder.deviceName.setText(holder.device.DeviceName);
        holder.deviceAddress.setText(holder.device.MACAddress);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onClickedDevice(holder.device.DeviceName, holder.device.MACAddress);
            }
        });

    }

    @Override
    public int getItemCount() {

        return mDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView deviceName;
        private final TextView deviceAddress;
        private DeviceListFragment.Device device;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.mac_address);

        }
    }


}
