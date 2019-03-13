package com.constellio.data.dao.services.cache;

public class ConstellioCacheOptions {

	boolean invalidateRemotelyWhenPutting;

	public boolean isInvalidateRemotelyWhenPutting() {
		return invalidateRemotelyWhenPutting;
	}

	public ConstellioCacheOptions setInvalidateRemotelyWhenPutting(boolean invalidateRemotelyWhenPutting) {
		this.invalidateRemotelyWhenPutting = invalidateRemotelyWhenPutting;
		return this;
	}

}
