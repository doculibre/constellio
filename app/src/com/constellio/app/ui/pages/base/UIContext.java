package com.constellio.app.ui.pages.base;

import java.io.Serializable;

public interface UIContext extends Serializable {
	
	void clearAttribute(String key);
	
	<T> T getAttribute(String key);
	
	<T> T setAttribute(String key, T value);

}
