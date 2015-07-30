package com.example.dcwa.auxclasses;

public class ServerInfo {

	private String ip;
	private int port;
	private String name;

	public ServerInfo(String ip, int port, String name) {
		this.ip = ip;
		this.port = port;
		this.name = name;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if( object == null || object.getClass() != ServerInfo.class ) {
			return false;
		}
		
		String objectIp = ((ServerInfo) object).ip;
		int objectPort = ((ServerInfo) object).port;
		String objectName = ((ServerInfo) object).name;
		
		if( ip.equals(objectIp) == false ) {
			return false;
		}
		if( port != objectPort ) {
			return false;
		}
		if( name.equals(objectName) == false ) {
			return false;
		}
		
		return true;
	}
}
