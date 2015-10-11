package com.remoty.common.other;


public class ConfigurationInfo {

	private String name;
	private String file;

	public ConfigurationInfo(String name, String file) {
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
