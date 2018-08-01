package com.constellio.data.dao.services.idGenerator;

public class UUIDV1Generator implements UniqueIdGenerator {
	public static String newRandomId() {
		// FIXME
		//return new com.eaio.uuid.UUID().toString();
		return null;
	}

	@Override
	public String next() {
		return newRandomId();
	}
}
