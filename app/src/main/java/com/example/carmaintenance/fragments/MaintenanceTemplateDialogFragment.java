package com.example.carmaintenance.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.carmaintenance.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MaintenanceTemplateDialogFragment extends DialogFragment {
	static final int BY_DISTANCE = 0;
	static final int BY_DURATION = 1;

	private static final String ARG_LIST = "list";
	private static final String ARG_TYPE = "type";

	static final String EXTRA_TYPE = ARG_TYPE;
	static final String EXTRA_RESULT = "result";

	static MaintenanceTemplateDialogFragment newInstance(
			ArrayList<Integer> list, int template_type) {
		Bundle bundle = new Bundle();
		bundle.putIntegerArrayList(ARG_LIST, list);
		bundle.putInt(ARG_TYPE, template_type);

		MaintenanceTemplateDialogFragment dialogFragment = new MaintenanceTemplateDialogFragment();

		dialogFragment.setArguments(bundle);

		return dialogFragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		View v = View.inflate(requireContext(),
				R.layout.dialog_maintenance_template, null);

		ListView listView = (ListView) v;
		ArrayList<Integer> list = new ArrayList<>();
		int type = BY_DISTANCE;

		if (getArguments() != null) {
			list = getArguments().getIntegerArrayList(ARG_LIST);
			if (list == null) {
				list = new ArrayList<>();
			}
			String append = getString(R.string.kilometer);
			if (getArguments().getInt(ARG_TYPE) == BY_DURATION) {
				type = BY_DURATION;
				append = getString(R.string.months);
			}

			List<String> strings = new ArrayList<>();
			for (int i : list) {
				strings.add(String.format(Locale.getDefault(), "%,d", i)
						+ " " + append);
			}
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(),
					android.R.layout.simple_list_item_1, strings);
			listView.setAdapter(arrayAdapter);
		}

		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setView(v)
				.setNegativeButton(android.R.string.cancel, null)
				.create();

		final ArrayList<Integer> finalList = list;
		final int finalType = type;

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				sendResults(finalList.get(position), finalType);
				Log.v("TAG_CHECK", "" + finalList.get(position));
				dialog.dismiss();
			}
		});

		return dialog;
	}

	private void sendResults(int selected, int type) {
		if (getTargetFragment() == null) return;

		Intent intent = new Intent();
		intent.putExtra(EXTRA_RESULT, selected);
		intent.putExtra(EXTRA_TYPE, type);

		getTargetFragment().onActivityResult(getTargetRequestCode(),
				Activity.RESULT_OK, intent);
	}
}
