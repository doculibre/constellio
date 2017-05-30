package com.constellio.data.dao.managers;

public class StatefullServiceDecorator {

	public <T> T decorate(T service) {
		return service;
	}

	public <T> void beforeInitialize(T service) {
	}

	public <T> void afterInitialize(T service) {
	}

}
