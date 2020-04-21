package com.example.carmaintenance.arrayadapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
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
import com.example.carmaintenance.utilities.Misc;

import java.util.List;

public class HistoryDetailsAdapter extends ArrayAdapter<UserVehicleService> {
	public HistoryDetailsAdapter(Context context, List<UserVehicleService> carServices) {
		super(context, 0, carServices);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext())
					.inflate(R.layout.car_n_parts, parent, false);
		}

		UserVehicleService currentCarService = getItem(position);

		TextView txtRegNo = convertView.findViewById(R.id.txt_reg_no);
		TextView txtBrandModel = convertView.findViewById(R.id.txt_brand_model);
		TextView txtDistance = convertView.findViewById(R.id.txt_distance);

		txtRegNo.setText(currentCarService.get_regNo());
		txtBrandModel.setText(currentCarService.get_brandModel());
		txtDistance.setText(Misc.getDistanceWithUnit(currentCarService.get_serviceDistance(), getContext()));

		txtRegNo.setTypeface(null, Typeface.BOLD);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// API 23 and above
			txtRegNo.setTextAppearance(android.R.style.TextAppearance_Medium);
		} else {
			txtRegNo.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
		}

		LinearLayout llParts = convertView.findViewById(R.id.ll_parts);

		for (int i = 0, j = currentCarService.get_parts().size(); i < j; i++) {
			TextView txt = new TextView(getContext());
			txt.setText(currentCarService.get_parts().get(i).get_partName());
			llParts.addView(txt);
		}

		return convertView;
	}
}
