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
import com.example.carmaintenance.objects.OdometerHistory;
import com.example.carmaintenance.utilities.DateUtilities;
import com.example.carmaintenance.utilities.Misc;

import java.util.ArrayList;

public class OdometerAdapter extends ArrayAdapter<OdometerHistory> {
	public OdometerAdapter(Context context, ArrayList<OdometerHistory> odometerHistories) {
		super(context, 0, odometerHistories);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).
					inflate(R.layout.list_odometer, parent, false);
		}

		OdometerHistory currentHistory = getItem(position);

		TextView txtRegNo = convertView.findViewById(R.id.txt_reg_no);
		txtRegNo.setText(currentHistory.get_regNo());

		TextView txtDate = convertView.findViewById(R.id.txt_date);
		txtDate.setText(DateUtilities.dateToStringDate(currentHistory.get_date()));

		TextView txtDistance = convertView.findViewById(R.id.txt_distance);
		txtDistance.setText(Misc.getDistanceWithUnit(currentHistory
				.get_distance(), getContext()));

		return convertView;
	}
}
