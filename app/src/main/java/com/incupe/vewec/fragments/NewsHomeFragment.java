package com.incupe.vewec.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.incupe.vewec.NewsViewActivity;
import com.incupe.vewec.R;
import com.incupe.vewec.arrayadapter.NewsAdapter;
import com.incupe.vewec.data.FirebaseContract;
import com.incupe.vewec.objects.News;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewsHomeFragment extends Fragment {
	private NewsAdapter _newsAdapter;

	private ProgressBar _progressBar;
	private long _lastSelectedUpdatedOn = 0;
	private final int _selectNewsCount = 15;
	private boolean _isQuerying = false;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.listview_with_empty_view,
				container, false);

		_progressBar = rootView.findViewById(R.id.progress_bar);

		ListView listView = rootView.findViewById(R.id.item_list);
		setupEmptyView(rootView, listView);

		_newsAdapter = new NewsAdapter(requireContext(),
				R.layout.list_news,
				new ArrayList<News>());
		listView.setAdapter(_newsAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				News news = _newsAdapter.getItem(position);
				if (news != null) {
					Intent intent = new Intent(requireActivity(), NewsViewActivity.class);
					intent.putExtra(NewsViewFragment.EXTRA_NEWS_URL, news.getUrl());
					requireActivity().startActivity(intent);
				}
			}
		});

		FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
		final DatabaseReference databaseReference = firebaseDatabase.getReference()
				.child(FirebaseContract.News.NEWS_KEY);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// if previous query still running, don't query anymore first
				if (_isQuerying) {
					return;
				}

				Query query = null;
				if (totalItemCount == 0) {
					query = databaseReference
							.orderByChild(FirebaseContract.News.ITEM_UPDATED_ON)
							.limitToLast(_selectNewsCount);
				} else if (firstVisibleItem + visibleItemCount == totalItemCount - 4) {
					query = databaseReference
							.orderByChild(FirebaseContract.News.ITEM_UPDATED_ON)
							.endAt(_lastSelectedUpdatedOn - 1)
							.limitToLast(_selectNewsCount);
				}
				if (query != null) {
					_isQuerying = true;
					query.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot snapshot) {
							List<News> newsList = new ArrayList<>();
							for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
								News news = dataSnapshot.getValue(News.class);
								if (news != null) {
									news.setFirebaseKey(dataSnapshot.getKey());
									newsList.add(news);

									if (_lastSelectedUpdatedOn == 0 ||
											news.getUpdated_on() < _lastSelectedUpdatedOn) {
										_lastSelectedUpdatedOn = news.getUpdated_on();
									}
								}
							}
							Collections.reverse(newsList);
							_newsAdapter.addAll(newsList);
							_progressBar.setVisibility(View.GONE);
							_isQuerying = false;
						}

						@Override
						public void onCancelled(@NonNull DatabaseError error) {
						}
					});
				}
			}
		});
		return rootView;
	}

	private void setupEmptyView(View rootView, ListView listView) {
		View emptyView = rootView.findViewById(R.id.empty_view);
		((TextView) rootView.findViewById(R.id.empty_title_text))
				.setText(getString(R.string.no_news));
		((ImageView) rootView.findViewById(R.id.empty_image))
				.setImageResource(R.drawable.ic_menu_book_grey_24);

		listView.setEmptyView(emptyView);
	}
}
