package com.constellio.app.servlet;

public class HttpConfigurations {
	private String constellioUrl;
	private String token;
	private String serviceKey;
	private String username;
	private String collection;

	public HttpConfigurations(String constellioUrl) {
		this.constellioUrl = constellioUrl;
	}

	public String getAdress() {
		return constellioUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getToken() {
		return token;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getCollection() {
		return collection;
	}
}
