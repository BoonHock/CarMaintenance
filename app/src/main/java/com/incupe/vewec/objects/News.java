package com.incupe.vewec.objects;

import com.google.firebase.database.Exclude;

public class News {
	@Exclude
	private String firebaseKey;
	private String updated_by;
	private long updated_on;
	private String url;
	private String title;
	private String img_url;
	private String source;

	public News() {
	}

	public String getFirebaseKey() {
		return firebaseKey;
	}

	public void setFirebaseKey(String firebaseKey) {
		this.firebaseKey = firebaseKey;
	}

	public String getUpdated_by() {
		return updated_by;
	}

	public long getUpdated_on() {
		return updated_on;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public String getImg_url() {
		return img_url;
	}

	public String getSource() {
		return source;
	}
}
