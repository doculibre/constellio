package com.constellio.app.ui.pages.base;

import java.util.Map;

public interface UnauthenticatedView extends BaseView {

	public boolean hasAccess();

	public void forParams(Map<String, String> params);
}
