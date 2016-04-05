package com.constellio.app.ui.application;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.navigator.Navigator;

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
	public void whenNavigatingToHomeThenNavigateToHomeURL() {
		navigator.home();
		verify(theVaadinNavigator, times(1)).navigateTo(NavigatorConfigurationService.HOME);
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
