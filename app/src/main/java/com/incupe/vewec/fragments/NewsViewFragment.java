package com.incupe.vewec.fragments;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.incupe.vewec.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsViewFragment extends Fragment {
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	public static final String EXTRA_NEWS_URL = "ARG_NEWS_URL";


	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param newsUrl news url.
	 * @return A new instance of fragment NewsViewFragment.
	 */
	public static NewsViewFragment newInstance(String newsUrl) {
		NewsViewFragment fragment = new NewsViewFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_NEWS_URL, newsUrl);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_news_view,
				container,
				false);

		ProgressBar progressBar = rootView.findViewById(R.id.progress_bar);

		WebView webView = rootView.findViewById(R.id.webView);
		webView.setWebViewClient(new VewecWebViewClient(progressBar));
		if (getArguments() != null) {
			String newsUrl = getArguments().getString(EXTRA_NEWS_URL);
			if (newsUrl != null && Patterns.WEB_URL.matcher(newsUrl).matches()) {
				webView.loadUrl(newsUrl);
			}
		}
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				requireActivity().finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static class VewecWebViewClient extends WebViewClient {
		private ProgressBar _progressBar;

		public VewecWebViewClient(ProgressBar progressBar) {
			this._progressBar = progressBar;
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			return false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			_progressBar.setVisibility(View.GONE);
		}
	}
}