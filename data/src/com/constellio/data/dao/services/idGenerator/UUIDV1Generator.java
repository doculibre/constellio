package com.constellio.data.dao.services.idGenerator;

import com.fasterxml.uuid.Generators;

public class UUIDV1Generator implements UniqueIdGenerator {
	public static String newRandomId() {
		return Generators.timeBasedGenerator().generate().toString();
	}

	@Override
	public String next() {
		return newRandomId();
	}
}
