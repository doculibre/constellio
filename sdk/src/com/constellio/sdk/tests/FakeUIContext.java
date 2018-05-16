package com.constellio.sdk.tests;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.pages.base.UIContext;

public class FakeUIContext implements UIContext {

	Map<String, Object> values = new HashMap<>();

	@Override
	public <T> T clearAttribute(String key) {
		return (T) values.remove(key);
	}

	@Override
	public <T> T getAttribute(String key) {
		return (T) values.get(key);
	}

	@Override
	public <T> T setAttribute(String key, T value) {
		values.put(key, value);
		return value;
	}
}
