package com.example.carmaintenance.objects;

public class Part {
	private String _partName;

	public Part(String partName) {
		_partName = partName;
	}

	public String get_partName() {
		return _partName;
	}

	public void set_partName(String _partName) {
		this._partName = _partName;
	}
}
