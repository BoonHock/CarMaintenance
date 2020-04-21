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
import com.example.carmaintenance.objects.History;
import com.example.carmaintenance.objects.Part;
import com.example.carmaintenance.utilities.DateUtilities;
import com.example.carmaintenance.utilities.Misc;

import java.util.List;

public class HistoryAdapter extends ArrayAdapter<History> {
	public HistoryAdapter(Context context, List<History> histories) {
		super(context, 0, histories);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).
					inflate(R.layout.list_history, parent, false);
		}

//		History currentHistory = getItem(position);
//
//		TextView txtDate = convertView.findViewById(R.id.txt_date);
//		txtDate.setText(DateUtilities.dateToStringDate(currentHistory.get_date()));
//
//		LinearLayout llDetails = convertView.findViewById(R.id.);
//
//		for (int i = 0, j = currentHistory.get_carServices().size(); i < j; i++) {
//			UserVehicleService currentCarService = currentHistory.get_carServices().get(i);
//
//			View newView = LayoutInflater.from(getContext()).inflate(R.layout.car_n_parts, null);
//
//			// programatically set item as clickable, focusable and have ripple effect
//			// but not set straight in xml first. see if future need or not
////			LinearLayout llCars = newView.findViewById(R.id.ll_cars);
//
////			llCars.setClickable(true);
////			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////				llCars.setFocusable(View.FOCUSABLE);
////			}
////			TypedValue outValue = new TypedValue();
////			getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
////			llCars.setBackgroundResource(outValue.resourceId);
//
//			TextView txtRegNo = newView.findViewById(R.id.txt_reg_no);
//			TextView txtBrandModel = newView.findViewById(R.id.txt_brand_model);
//			TextView txtDistance = newView.findViewById(R.id.txt_distance);
//
//			txtRegNo.setText(currentCarService.get_regNo());
//			txtBrandModel.setText(currentCarService.get_brandModel());
//			txtDistance.setText(Misc.getDistanceWithUnit(currentCarService.get_serviceDistance(), getContext()));
//
//			txtRegNo.setTypeface(null, Typeface.BOLD);
//
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//				// API 23 and above
//				txtRegNo.setTextAppearance(android.R.style.TextAppearance_Medium);
//			} else {
//				txtRegNo.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
//			}
//
//			LinearLayout llParts = newView.findViewById(R.id.ll_parts);
//
//			for (int iPart = 0, jPart = currentCarService.get_parts().size(); iPart < jPart; iPart++) {
//				Part part = currentCarService.get_parts().get(iPart);
//
//				TextView txtPart = new TextView(getContext());
//				txtPart.setText(part.get_partName());
//				llParts.addView(txtPart);
//			}
//
//			llDetails.addView(newView);
//		}

//		HistoryDetailsAdapter historyDetailsAdapter =
//				new HistoryDetailsAdapter(getContext(), currentHistory.get_carServices());
//		ListView llDetails = convertView.findViewById(R.id.lv_history_details);
//		llDetails.setAdapter(historyDetailsAdapter);

//		Misc.setListViewDynamicHeight(llDetails);

		return convertView;
	}
}
