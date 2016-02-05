package com.constellio.data.dao.services.idGenerator;

public class UUIDV1Generator implements UniqueIdGenerator {
	public static String newRandomId() {
		return new com.eaio.uuid.UUID().toString();
	}

	@Override
	public String next() {
		return newRandomId();
	}
}
