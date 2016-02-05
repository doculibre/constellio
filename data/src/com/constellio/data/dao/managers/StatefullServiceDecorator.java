package com.constellio.data.dao.managers;

public class StatefullServiceDecorator {

	public <T> T decorate(T service) {
		return service;
	}

}
