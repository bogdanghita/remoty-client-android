package com.remoty.common.datatypes;


public class ConfigurationEntry {

	private String name;
	private String file;

	public ConfigurationEntry(String name, String file) {
		this.name = name;
		this.file = file;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return file;
	}
}
