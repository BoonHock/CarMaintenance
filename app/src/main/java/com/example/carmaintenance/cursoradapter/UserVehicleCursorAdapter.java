package com.example.carmaintenance.cursoradapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.objects.UserVehicle;
import com.example.carmaintenance.utilities.DateUtilities;

public class UserVehicleCursorAdapter extends CursorAdapter {
	public UserVehicleCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.list_user_vehicle, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		UserVehicle userVehicle = new UserVehicle(cursor);

		TextView txtRegNo = view.findViewById(R.id.txt_reg_no);
		TextView txtBrandModel = view.findViewById(R.id.txt_brand_model);
		TextView txtAddedOn = view.findViewById(R.id.txt_added_on);

		String brandModel = userVehicle.get_brand()
				+ " " + userVehicle.get_model()
				+ " " + userVehicle.get_variant();

		txtRegNo.setText(userVehicle.get_regNo());
		txtBrandModel.setText(brandModel);

		txtAddedOn.setText(DateUtilities.dateToStringDateTime(userVehicle.get_addedOn()));
	}
}
