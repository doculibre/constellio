package com.constellio.sdk.tests;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.CoreViews;

public class MockedNavigation extends Navigation {
	Map<Class<? extends CoreViews>, CoreViews> mocks = new HashMap<>();

	@Override
	public <T extends CoreViews> T to(Class<T> navigatorClass) {
		if (mocks.containsKey(navigatorClass)) {
			return (T) mocks.get(navigatorClass);
		} else {
			T mock = mock(navigatorClass, navigatorClass.getName());
			mocks.put(navigatorClass, mock);
			return mock;
		}
	}
}
