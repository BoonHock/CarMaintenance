package com.incupe.vewec.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.incupe.vewec.R;

import java.text.NumberFormat;
import java.util.Locale;

public class Misc {
	private static final String LOG_TAG = Misc.class.getSimpleName();

	public static String getDistanceWithUnit(int distance, Context context) {
		return NumberFormat.getNumberInstance(Locale.getDefault())
				.format(distance) + " " + context.getString(R.string.kilometer);
	}

	/*
	 * https://stackoverflow.com/a/28713754/6039142
	 * when add listview inside another listview. the inner listview will be "collapsed". cant
	 * see full list of items. this will resize it to show full items
	 * */
	public static void setListViewDynamicHeight(ListView mListView) {
		ListAdapter mListAdapter = mListView.getAdapter();
		if (mListAdapter == null) {
			// when adapter is null
			return;
		}
		int height = 0;
		int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
		for (int i = 0; i < mListAdapter.getCount(); i++) {
			View listItem = mListAdapter.getView(i, null, mListView);
			listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
			height += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = mListView.getLayoutParams();
		params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
		mListView.setLayoutParams(params);
		mListView.requestLayout();
	}

	/**
	 * https://stackoverflow.com/questions/1109022/close-hide-android-soft-keyboard
	 */
	public static void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = new View(activity);
		}
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public static void showKeyboard(Activity activity, EditText editText) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
		}
	}
}
