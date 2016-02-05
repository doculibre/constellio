package com.constellio.model.utils;

import org.jdom2.Document;

public interface XMLConfigReader<T> {

	T read(String collection, Document document);

}
