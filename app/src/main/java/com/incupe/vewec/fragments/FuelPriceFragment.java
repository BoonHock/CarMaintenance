package com.incupe.vewec.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FuelPriceFragment extends Fragment {
	private static final int REQUEST_STORAGE_PERMISSION = 1;
	private static final String FILE_PROVIDER_AUTHORITY = "com.incupe.vewec.fileProvider";

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

	private Button _btnShare;

	private LinearLayout _llMask;
	private ProgressBar _progressBar;

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
		_btnShare = rootView.findViewById(R.id.btn_share);

		if (container != null) {
			_llMask = container.getRootView().findViewById(R.id.ll_mask);
			_progressBar = container.getRootView().findViewById(R.id.progress_bar);
		}
		// display loading. will remove after all loaded
		if (_llMask != null && _progressBar != null) {
			_llMask.setVisibility(View.VISIBLE);
			_progressBar.setVisibility(View.VISIBLE);
		}

		_firebaseDatabase = FirebaseDatabase.getInstance();
		_dbActualReference = _firebaseDatabase.getReference()
				.child(FirebaseContract.FuelPrice.FUEL_PRICE_KEY)
				.child(FirebaseContract.FuelPrice.ACTUAL_KEY);
		_dbForecastReference = _firebaseDatabase.getReference()
				.child(FirebaseContract.FuelPrice.FUEL_PRICE_KEY)
				.child(FirebaseContract.FuelPrice.FORECAST_KEY);

		_btnShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ContextCompat.checkSelfPermission(requireActivity(),
						Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
									Manifest.permission.READ_EXTERNAL_STORAGE},
							REQUEST_STORAGE_PERMISSION);
				} else {
					takeScreenshotNShare();
				}
			}
		});

		return rootView;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		if (requestCode == REQUEST_STORAGE_PERMISSION) {
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				takeScreenshotNShare();
			}
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
					_llMask.setVisibility(View.GONE);
					_progressBar.setVisibility(View.GONE);
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

	private void takeScreenshotNShare() {
		Date now = new Date();
		android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

		try {
			// create bitmap screen capture
			View v1 = requireActivity().getWindow().getDecorView().getRootView();
			v1.setDrawingCacheEnabled(true);
			Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
			v1.setDrawingCacheEnabled(false);

			saveImage(requireContext(), bitmap);
		} catch (Throwable e) {
			// Several error may come out with file handling or DOM
			e.printStackTrace();
		}
	}

	private void saveImage(Context context, Bitmap image) {
		String savedImagePath = null;

		Calendar calendar = Calendar.getInstance();
		// Create the new file in the external storage
		String imageFileName = "IMG_" + calendar.get(Calendar.YEAR)
				+ calendar.get(Calendar.MONTH)
				+ calendar.get(Calendar.DAY_OF_MONTH) + "_"
				+ calendar.get(Calendar.HOUR_OF_DAY)
				+ calendar.get(Calendar.MINUTE)
				+ calendar.get(Calendar.SECOND) + ".jpg";

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
			values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
			values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Vewec");
			values.put(MediaStore.Images.Media.IS_PENDING, true);
			// RELATIVE_PATH and IS_PENDING are introduced in API 29.

			Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			if (uri != null) {
				try {
					OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

					if (outputStream != null) {
						try {
							image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
							outputStream.close();
							shareImage(context, uri);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				values.put(MediaStore.Images.Media.IS_PENDING, false);
				context.getContentResolver().update(uri, values, null, null);
			}

		} else {
			File storageDir = new File(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
							+ "/Vewec");
			boolean success = true;
			if (!storageDir.exists()) {
				success = storageDir.mkdirs();
			}

			// Save the new Bitmap
			if (success) {
				File imageFile = new File(storageDir, imageFileName);
				savedImagePath = imageFile.getAbsolutePath();
				try {
					OutputStream fOut = new FileOutputStream(imageFile);
					image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
					fOut.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Add the image to the system gallery
				galleryAddPic(context, savedImagePath);

				// TODO: share image
				shareImage(requireContext(), savedImagePath);
			}
		}
	}

	/**
	 * Helper method for adding the photo to the system photo gallery so it can be accessed
	 * from other apps.
	 * ONLY FOR API < 29
	 *
	 * @param imagePath The path of the saved image
	 */
	private void galleryAddPic(Context context, String imagePath) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(imagePath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		context.sendBroadcast(mediaScanIntent);
	}

	// this is called by API < 29. will take image path and convert to uri
	// then call shareImage() which take uri param
	private void shareImage(Context context, String imagePath) {
		// Create the share intent and start the share activity
		File imageFile = new File(imagePath);
		Uri imageUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile);
		shareImage(context, imageUri);
	}

	private void shareImage(Context context, Uri imageUri) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/*");
		shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
		shareIntent.putExtra(Intent.EXTRA_TEXT,
				"Check out the latest fuel prices.\n" +
						"Get your latest weekly fuel prices updates from Vewec:\n" +
						"https://bit.ly/2Z4SidX");
		context.startActivity(Intent.createChooser(shareIntent, "Share image"));
	}
}
