/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
	ConstellioNavigator navigator;

	@Before
	public void setup() {
		navigator = new ConstellioNavigator(theVaadinNavigator);
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
