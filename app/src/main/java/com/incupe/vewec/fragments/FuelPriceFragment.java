package com.incupe.vewec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.incupe.vewec.R;
import com.incupe.vewec.data.FirebaseContract;
import com.incupe.vewec.objects.FuelPrice;
import com.incupe.vewec.utilities.DateUtilities;

import java.util.Date;
import java.util.Locale;

public class FuelPriceFragment extends Fragment {
	private TextView _txtPeriod;
	private TextView _txtUpdatedOn;
	private TextView _txtPriceRon95;
	private TextView _txtPriceRon97;
	private TextView _txtPriceDiesel;
	private TextView _txtPriceChangeRon95;
	private TextView _txtPriceChangeRon97;
	private TextView _txtPriceChangeDiesel;
	private TextView _txtForecastRon95;
	private TextView _txtForecastRon97;
	private TextView _txtForecastDiesel;

	private TextView _txtForecastUnavailable;
	private TextView _txtForecastPeriod;

	private TableLayout _tableForecast;

	private FirebaseDatabase _firebaseDatabase;
	private DatabaseReference _dbActualReference;
	private DatabaseReference _dbForecastReference;
	private ValueEventListener _valueActualEventListener;
	private ValueEventListener _valueForecastEventListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_fuel_price,
				container, false);

		_txtPeriod = rootView.findViewById(R.id.txt_period);
		_txtUpdatedOn = rootView.findViewById(R.id.txt_updated_on);

		_txtPriceRon95 = rootView.findViewById(R.id.txt_price_ron95);
		_txtPriceRon97 = rootView.findViewById(R.id.txt_price_ron97);
		_txtPriceDiesel = rootView.findViewById(R.id.txt_price_diesel);

		_txtPriceChangeRon95 = rootView.findViewById(R.id.txt_price_change_ron95);
		_txtPriceChangeRon97 = rootView.findViewById(R.id.txt_price_change_ron97);
		_txtPriceChangeDiesel = rootView.findViewById(R.id.txt_price_change_diesel);

		_txtForecastRon95 = rootView.findViewById(R.id.txt_forecast_ron95);
		_txtForecastRon97 = rootView.findViewById(R.id.txt_forecast_ron97);
		_txtForecastDiesel = rootView.findViewById(R.id.txt_forecast_diesel);

		_txtForecastUnavailable = rootView.findViewById(R.id.txt_forecast_unavailable);
		_txtForecastPeriod = rootView.findViewById(R.id.txt_forecast_period);

		_tableForecast = rootView.findViewById(R.id.table_forecast);

		_firebaseDatabase = FirebaseDatabase.getInstance();
		_dbActualReference = _firebaseDatabase.getReference()
				.child(FirebaseContract.FuelPrice.FUEL_PRICE_KEY)
				.child(FirebaseContract.FuelPrice.ACTUAL_KEY);
		_dbForecastReference = _firebaseDatabase.getReference()
				.child(FirebaseContract.FuelPrice.FUEL_PRICE_KEY)
				.child(FirebaseContract.FuelPrice.FORECAST_KEY);

		return rootView;
	}

	private void setupPriceChangeTextView(TextView txtPriceChange, double priceChange) {
		txtPriceChange.setText(String.format(Locale.getDefault(), "%.2f", priceChange));

		if (priceChange > 0) {
			txtPriceChange.setTextColor(ContextCompat
					.getColor(requireActivity(), R.color.fuel_up));
			txtPriceChange.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_arrow_drop_up_red_24,
					0,
					0,
					0);
		} else if (priceChange == 0) {
			txtPriceChange.setTextColor(txtPriceChange.getTextColors().getDefaultColor());
		} else {
			txtPriceChange.setTextColor(ContextCompat
					.getColor(requireActivity(), R.color.fuel_down));
			txtPriceChange.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_arrow_drop_down_green_24,
					0,
					0,
					0);
		}
	}

	private void attachDatabaseListener() {
		if (_valueActualEventListener == null) {
			_valueActualEventListener = new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						FuelPrice fuelPrice =
								snapshot.getValue(FuelPrice.class);
						if (fuelPrice != null) {
							String period = DateUtilities.dateToStringDate(new Date(fuelPrice.date_from)) +
									" — " +
									DateUtilities.dateToStringDate(new Date(fuelPrice.date_to));
							String updatedOn = DateUtilities.dateToStringDateTime(new Date(fuelPrice.updated_on));

							_txtPeriod.setText(period);
							_txtUpdatedOn.setText(updatedOn);

							String priceRon95 = getString(R.string.myr) + " " +
									String.format(Locale.getDefault(), "%.2f",
											fuelPrice.getPrice(FirebaseContract.FuelPrice.RON95_KEY)) +
									" / " + getString(R.string.litre);
							String priceRon97 = getString(R.string.myr) + " " +
									String.format(Locale.getDefault(), "%.2f",
											fuelPrice.getPrice(FirebaseContract.FuelPrice.RON97_KEY)) +
									" / " + getString(R.string.litre);
							String priceDiesel = getString(R.string.myr) + " " +
									String.format(Locale.getDefault(), "%.2f",
											fuelPrice.getPrice(FirebaseContract.FuelPrice.DIESEL_KEY)) +
									" / " + getString(R.string.litre);

							_txtPriceRon95.setText(priceRon95);
							_txtPriceRon97.setText(priceRon97);
							_txtPriceDiesel.setText(priceDiesel);

							setupPriceChangeTextView(_txtPriceChangeRon95, fuelPrice
									.getChange(FirebaseContract.FuelPrice.RON95_KEY));
							setupPriceChangeTextView(_txtPriceChangeRon97, fuelPrice
									.getChange(FirebaseContract.FuelPrice.RON97_KEY));
							setupPriceChangeTextView(_txtPriceChangeDiesel, fuelPrice
									.getChange(FirebaseContract.FuelPrice.DIESEL_KEY));

							attachForecastListener(fuelPrice.date_to);

							break; // run just once
						}
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				}
			};
			_dbActualReference
					.orderByChild(FirebaseContract.FuelPrice.DATE_FROM_KEY)
					.limitToLast(1)
					.addListenerForSingleValueEvent(_valueActualEventListener);
		}
	}

	private void detachDatabaseListener() {
		if (_valueActualEventListener != null) {
			_dbActualReference.removeEventListener(_valueActualEventListener);
			_valueActualEventListener = null;
		}
		if (_valueForecastEventListener != null) {
			_dbForecastReference.removeEventListener(_valueForecastEventListener);
			_valueForecastEventListener = null;
		}
	}

	private void attachForecastListener(long actualDateTo) {
		if (_valueForecastEventListener == null) {
			_valueForecastEventListener = new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if (dataSnapshot.getChildrenCount() == 0) {
						_txtForecastUnavailable.setVisibility(View.VISIBLE);
						_txtForecastPeriod.setVisibility(View.GONE);
						_tableForecast.setVisibility(View.GONE);
					} else {
						_txtForecastUnavailable.setVisibility(View.GONE);
						_txtForecastPeriod.setVisibility(View.VISIBLE);
						_tableForecast.setVisibility(View.VISIBLE);

						for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
							FuelPrice fuelPrice = snapshot.getValue(FuelPrice.class);
							if (fuelPrice != null) {
								String period = DateUtilities.dateToStringDate(
										new Date(fuelPrice.date_from)) +
										" — " +
										DateUtilities.dateToStringDate(new Date(fuelPrice.date_to));
								_txtForecastPeriod.setText(period);

								String priceRon95 = getString(R.string.myr) + " " +
										String.format(Locale.getDefault(), "%.2f",
												fuelPrice.getPrice(FirebaseContract.FuelPrice.RON95_KEY)) +
										" ± " +
										String.format(Locale.getDefault(), "%.2f", fuelPrice
												.getChange(FirebaseContract.FuelPrice.RON95_KEY)) +
										" / " + getString(R.string.litre);
								String priceRon97 = getString(R.string.myr) + " " +
										String.format(Locale.getDefault(), "%.2f", fuelPrice
												.getPrice(FirebaseContract.FuelPrice.RON97_KEY)) +
										" ± " +
										String.format(Locale.getDefault(), "%.2f", fuelPrice
												.getChange(FirebaseContract.FuelPrice.RON97_KEY)) +
										" / " + getString(R.string.litre);
								String priceDiesel = getString(R.string.myr) + " " +
										String.format(Locale.getDefault(), "%.2f", fuelPrice
												.getPrice(FirebaseContract.FuelPrice.DIESEL_KEY)) +
										" ± " +
										String.format(Locale.getDefault(), "%.2f", fuelPrice
												.getChange(FirebaseContract.FuelPrice.DIESEL_KEY)) +
										" / " + getString(R.string.litre);
								_txtForecastRon95.setText(priceRon95);
								_txtForecastRon97.setText(priceRon97);
								_txtForecastDiesel.setText(priceDiesel);
							}

						}
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				}
			};
			_dbForecastReference
					.startAt(actualDateTo)
					.orderByChild(FirebaseContract.FuelPrice.DATE_FROM_KEY)
					.limitToLast(1)
					.addListenerForSingleValueEvent(_valueForecastEventListener);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		attachDatabaseListener();
	}

	@Override
	public void onPause() {
		super.onPause();
		detachDatabaseListener();
	}
}
