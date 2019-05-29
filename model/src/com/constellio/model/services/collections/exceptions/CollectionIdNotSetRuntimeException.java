package com.constellio.model.services.collections.exceptions;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class CollectionIdNotSetRuntimeException extends ImpossibleRuntimeException
{
	public CollectionIdNotSetRuntimeException(String collectionCode) {
		super("Collection does'nt have an associated id. Collection code : " + collectionCode);
	}
}
