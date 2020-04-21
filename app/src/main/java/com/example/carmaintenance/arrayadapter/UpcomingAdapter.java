package com.example.carmaintenance.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carmaintenance.R;
import com.example.carmaintenance.objects.UserVehicleService;
import com.example.carmaintenance.objects.Part;
import com.example.carmaintenance.utilities.DateUtilities;
import com.example.carmaintenance.utilities.Misc;

import java.util.ArrayList;

public class UpcomingAdapter extends ArrayAdapter<UserVehicleService> {

	public UpcomingAdapter(Context context, ArrayList<UserVehicleService> carServices) {
		super(context, 0, carServices);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).
					inflate(R.layout.list_upcoming, parent, false);
		}
		UserVehicleService currentCarService = getItem(position);

		TextView txtRegNo = (TextView) convertView.findViewById(R.id.txt_reg_no);
		txtRegNo.setText(currentCarService.get_regNo());

		TextView txtModel = (TextView) convertView.findViewById(R.id.txt_brand_model);
		String brandModel = currentCarService.get_brandModel();
		txtModel.setText(brandModel);

		TextView txtDueDate = convertView.findViewById(R.id.txt_due_date);
		txtDueDate.setText(DateUtilities.dateToStringDate(currentCarService.get_nextServiceDate()));

		TextView txtDueDistance = convertView.findViewById(R.id.txt_due_distance);
		txtDueDistance.setText(Misc.getDistanceWithUnit(currentCarService
				.get_nextServiceDistance(), getContext()));

		ArrayList<Part> parts = currentCarService.get_parts();

		LinearLayout llParts = ((LinearLayout) convertView.findViewById(R.id.ll_parts));

		for (int i = 0, j = parts.size(); i < j; i++) {
			TextView txt = new TextView(getContext());
			txt.setText(parts.get(i).get_partName());
			llParts.addView(txt);
		}
		return convertView;
	}
}
