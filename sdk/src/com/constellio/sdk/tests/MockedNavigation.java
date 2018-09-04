package com.constellio.sdk.tests;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

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
