package com.incupe.vewec.arrayadapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.incupe.vewec.R;
import com.incupe.vewec.objects.News;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {
	public NewsAdapter(@NonNull Context context, int resource, List<News> objects) {
		super(context, resource);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = ((Activity) getContext())
					.getLayoutInflater()
					.inflate(R.layout.list_news, parent, false);
		}

		News news = getItem(position);

		TextView txtNewsTitle = convertView.findViewById(R.id.txt_news_title);
		ImageView imageNews = convertView.findViewById(R.id.image_news);
		TextView txtSourceName = convertView.findViewById(R.id.txt_news_source_name);

		if (news != null) {
			txtNewsTitle.setText(news.getTitle());
			Picasso.get().load(news.getImg_url()).into(imageNews);
			txtSourceName.setText(news.getSource());
		}
		return convertView;
	}
}
