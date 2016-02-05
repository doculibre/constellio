package com.constellio.app.client.entities;

import java.util.List;

public class CollectionResource {

	private String collection;

	private List<String> languages;

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

}
