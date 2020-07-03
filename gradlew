package com.incupe.vewec;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.incupe.vewec.data.FirebaseContract.MaintenanceDetails;
import com.incupe.vewec.data.MaintenanceContract.MaintenanceEntry;
import com.incupe.vewec.data.MaintenanceDetailsContract.MaintenanceDetailsEntry;
import com.incupe.vewec.data.MaintenanceItemContract.MaintenanceItemEntry;
import com.incupe.vewec.data.OdometerContract.OdometerEntry;
import com.incupe.vewec.data.UserVehicleContract.UserVehicleEntry;
import com.incupe.vewec.objects.FirebaseObj;
import com.incupe.vewec.objects.MaintenanceItem;
import com.incupe.vewec.objects.UserVehicle;
import com.incupe.vewec.objects.VehicleTemplate;
import com.incupe.vewec.utilities.DateUtilities;
import com.incupe.vewec.utilities.Misc;
import com.incupe.vewec.utilities.SetupViews;
import com.incupe.vewec.utilities.UserDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MaintenanceEditorActivity extends AppCompatActivity {
	private Uri _currentUri;

	private List<Integer> _vehicleIds;

	private LinearLayout _llMask;
	private LinearLayout _llNewItem;
	private EditText _editNewItemName;
	private EditText _editNewItemPrice;
	private TextView _txtCloseNewItem;

	private ScrollView _viewContent;
	private ProgressBar _progressBar;
	private Spinner _spinnerVehicle;
	private EditText _editDate;
	private EditText _editOdometer;
	private EditText _editRemarks;
	private EditText _editTotal;
	private LinearLayout _llInspectItems;
	private LinearLayout _llReplaceItems;

	private Calendar _calendarOdometer;
	private DecimalFormat _decimalFormat = new DecimalFormat("0.00");

	private List<String> _preCheckInspect = new ArrayList<>();
	private List<String> _preCheckReplace = new ArrayList<>();
	private Map<String, Double> _mapInitInspect = new HashMap<>();
	private Map<String, Double> _mapInitReplace = new HashMap<>();

	private int _editVehicleId = -1;
	private boolean _isAddingReplaceItem = true;

	private View.OnFocusChangeListener _focusChangeFormatMoney =
			new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						EditText editText = (EditText) v;
						String price = editText.getText().toString();
						if (!TextUtils.isEmpty(price)) {
							String formattedPrice = _decimalFormat.format(Double.valueOf(price));
							editText.setText(formattedPrice);
						}
					}
				}
			};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maintenance_editor);

		// Load an ad into the AdMob banner view.
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		Intent intent = getIntent();
		_currentUri = intent.getData();
		_calendarOdometer = DateUtilities
				.getCalendarAtMidnight(Calendar.getInstance()); // default value

		_llMask = findViewById(R.id.ll_mask);
		_llNewItem = findViewById(R.id.ll_custom_item_edit);
		_editNewItemName = findViewById(R.id.edit_new_item_name);
		_editNewItemPrice = findViewById(R.id.edit_new_item_price);
		_txtCloseNewItem = findViewById(R.id.txt_close);

		_viewContent = findViewById(R.id.content);
		_progressBar = findViewById(R.id.progress_bar);
		_spinnerVehicle = findViewById(R.id.spinner_vehicle);
		_editDate = findViewById(R.id.edit_date);
		_editOdometer = findViewById(R.id.edit_odometer);
		_editRemarks = findViewById(R.id.edit_remarks);
		_editTotal = findViewById(R.id.edit_total);
		_llInspectItems = findViewById(R.id.ll_inspect_items);
		_llReplaceItems = findViewById(R.id.ll_replace_items);

		_llMask.setVisibility(View.GONE);
		_llNewItem.setVisibility(View.GONE);
		_viewContent.setVisibility(V