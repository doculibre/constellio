package com.constellio.data.dao.services.idGenerator;

public class InMemorySequentialGenerator implements UniqueIdGenerator {

	private long current = 0;

	@Override
	public String next() {
		return String.valueOf(++current);
	}
}
