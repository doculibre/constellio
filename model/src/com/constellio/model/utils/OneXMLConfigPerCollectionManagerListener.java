package com.constellio.model.utils;

public interface OneXMLConfigPerCollectionManagerListener<T> {

	void onValueModified(String collection, T newValue);

}
