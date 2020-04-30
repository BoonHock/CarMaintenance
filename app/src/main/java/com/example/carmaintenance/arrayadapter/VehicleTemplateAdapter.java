package com.example.carmaintenance.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carmaintenance.R;
import com.example.carmaintenance.objects.VehicleTemplate;

import java.util.List;

public class VehicleTemplateAdapter extends ArrayAdapter<VehicleTemplate> {
	public VehicleTemplateAdapter(Context context, int resource, List<VehicleTemplate> objects) {
		super(context, resource, objects);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_vehicle_template, parent, false);
		}

		TextView txtBrand = convertView.findViewById(R.id.txt_brand);
		TextView txtModel = convertView.findViewById(R.id.txt_model);
		TextView txtVariant = convertView.findViewById(R.id.txt_variant);

		VehicleTemplate currentVehicleTemplate = getItem(position);

		if (currentVehicleTemplate != null) {
			txtBrand.setText(currentVehicleTemplate.getBrand());
			txtModel.setText(currentVehicleTemplate.getModel());
			txtVariant.setText(currentVehicleTemplate.getVariant());
		}

		return convertView;
	}
}
