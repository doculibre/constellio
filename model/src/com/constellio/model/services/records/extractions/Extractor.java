package com.constellio.model.services.records.extractions;

/**
 * @author Majid
 */
public interface Extractor<T>{
	Object extractFrom(T feed);
}
