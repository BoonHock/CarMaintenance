package com.incupe.vewec.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VehicleTemplate {
	// id of vehicle in firebase.
	// use it to retrieve stuffs from firebase only.
	// DO NOT store in sql because this might change
	private String firebase_vehicle_id;
	private String brand;
	private String model;
	private String variant;

	public VehicleTemplate() {
	}

	public VehicleTemplate(String brand, String model, String variant) {
		this.brand = brand;
		this.model = model;
		this.variant = variant;
	}

	public String getBrand() {
		return brand.toUpperCase();
	}

	public String getModel() {
		return model.toUpperCase();
	}

	public String getVariant() {
		return variant.toUpperCase();
	}

	public String getFirebase_vehicle_id() {
		return firebase_vehicle_id;
	}

	public static List<String> getBrands(List<VehicleTemplate> vehicleTemplates) {
		List<String> list = new ArrayList<>();
		for (VehicleTemplate vehicleTemplate : vehicleTemplates) {
			if (!list.contains(vehicleTemplate.getBrand())) {
				list.add(vehicleTemplate.getBrand());
			}
		}
		Collections.sort(list);
		return list;
	}

	// get all vehicle models in @vehicleTemplates
	public static List<String> getModels(List<VehicleTemplate> vehicleTemplates) {
		List<String> list = new ArrayList<>();
		for (VehicleTemplate vehicleTemplate : vehicleTemplates) {
			if (!list.contains(vehicleTemplate.getModel())) {
				list.add(vehicleTemplate.getModel());
			}
		}
		Collections.sort(list);
		return list;
	}

	// get vehicle models in @vehicleTemplates by brand
	public static List<String> getModels(List<VehicleTemplate> vehicleTemplates, String brand) {
		List<String> list = new ArrayList<>();
		for (VehicleTemplate vehicleTemplate : vehicleTemplates) {
			if (!list.contains(vehicleTemplate.getModel()) &&
					vehicleTemplate.getBrand().equals(brand.toUpperCase())) {
				list.add(vehicleTemplate.getModel());
			}
		}
		Collections.sort(list);
		return list;
	}

	// get all variants in @vehicleTemplates
	public static List<String> getVariants(List<VehicleTemplate> vehicleTemplates) {
		List<String> list = new ArrayList<>();
		for (VehicleTemplate vehicleTemplate : vehicleTemplates) {
			if (!list.contains(vehicleTemplate.getVariant())) {
				list.add(vehicleTemplate.getVariant());
			}
		}
		Collections.sort(list);
		return list;
	}

	// get all variants in @vehicleTemplates by brand and model
	public static List<String> getVariants(List<VehicleTemplate> vehicleTemplates, String brand, String model) {
		List<String> list = new ArrayList<>();
		for (VehicleTemplate vehicleTemplate : vehicleTemplates) {
			if (!list.contains(vehicleTemplate.getVariant()) &&
					vehicleTemplate.getBrand().equals(brand.toUpperCase()) &&
					vehicleTemplate.getModel().equals(model.toUpperCase())) {
				list.add(vehicleTemplate.getVariant());
			}
		}
		Collections.sort(list);
		return list;
	}

	public static String getFirebaseVehicleIdFromList(
			String findBrand,
			String findModel,
			String findVariant) {
		for (int i = 0, j = FirebaseObj._vehicleTemplates.size(); i < j; i++) {
			VehicleTemplate vehicleTemplate = FirebaseObj._vehicleTemplates.get(i);
			if (vehicleTemplate.getBrand().toUpperCase().equals(findBrand.toUpperCase())
					&& vehicleTemplate.getModel().toUpperCase().equals(findModel.toUpperCase())
					&& vehicleTemplate.getVariant().toUpperCase().equals(findVariant.toUpperCase())) {
				return vehicleTemplate.getFirebase_vehicle_id();
			}
		}
		return "";
	}

	public static class CustomComparator implements Comparator<VehicleTemplate> {
		@Override
		public int compare(VehicleTemplate o1, VehicleTemplate o2) {
			String o1Bmv = o1.getBrand() + " " + o1.getModel() + " " + o1.getVariant();
			String o2Bmv = o2.getBrand() + " " + o2.getModel() + " " + o2.getVariant();

			return o1Bmv.compareToIgnoreCase(o2Bmv);
		}
	}
}
