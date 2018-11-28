package com.constellio.app.ui.application;

import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.navigator.Navigator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConstellioNavigatorTest extends ConstellioTest {

	@Mock Navigator theVaadinNavigator;
	CoreViews navigator;

	@Before
	public void setup() {
		navigator = new CoreViews(theVaadinNavigator);
	}

	@Test
	public void whenNavigatingToAppManagementThenNavigateToAppManagementURL() {
		navigator.appManagement();
		verify(theVaadinNavigator, times(1)).navigateTo(NavigatorConfigurationService.APP_MANAGEMENT);
	}

	@Test
	public void whenNavigatingToSimpleSearchResultInnerPageThenNavigateToSearchURLWithQueryAndPage() {
		navigator.simpleSearch("query", 42);
		verify(theVaadinNavigator, times(1)).navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/q/query/42");
	}

	@Test
	public void whenNavigatingToSimpleSearchResultPageThenNavigateToSearchURLWithQuery() {
		navigator.simpleSearch("query");
		verify(theVaadinNavigator, times(1)).navigateTo(NavigatorConfigurationService.SIMPLE_SEARCH + "/q/query");
	}
}
