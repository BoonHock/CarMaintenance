package com.example.carmaintenance.objects;

public class MaintenanceItem {
	private String firebase_item_id;
	private String item;
	private int inspect_replace;
	private int usage;
	private int first_distance;
	private int distance_interval;
	private int first_duration;
	private int duration_interval;

	public MaintenanceItem() {
	}

	public String getFirebase_item_id() {
		return firebase_item_id;
	}

	public void setFirebase_item_id(String firebase_item_id) {
		this.firebase_item_id = firebase_item_id;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public int getInspect_replace() {
		return inspect_replace;
	}

	public void setInspect_replace(int inspect_replace) {
		this.inspect_replace = inspect_replace;
	}

	public int getUsage() {
		return usage;
	}

	public void setUsage(int usage) {
		this.usage = usage;
	}

	public int getFirst_distance() {
		return first_distance;
	}

	public void setFirst_distance(int first_distance) {
		this.first_distance = first_distance;
	}

	public int getDistance_interval() {
		return distance_interval;
	}

	public void setDistance_interval(int distance_interval) {
		this.distance_interval = distance_interval;
	}

	public int getFirst_duration() {
		return first_duration;
	}

	public void setFirst_duration(int first_duration) {
		this.first_duration = first_duration;
	}

	public int getDuration_interval() {
		return duration_interval;
	}

	public void setDuration_interval(int duration_interval) {
		this.duration_interval = duration_interval;
	}
}
