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
package com.constellio.app.ui.pages.setup;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
@InDevelopmentTest
public class ConstellioSetupPresenterAcceptanceTest extends ConstellioTest {

	@Mock ConstellioSetupView view;

	@Test
	public void testName()
			throws Exception {
		when(view.getSessionContext()).thenReturn(FakeSessionContext.noUserNoCollection());
		ConstellioFactories constellioFactories = getConstellioFactories();
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		ConstellioSetupPresenter presenter = new ConstellioSetupPresenter(view);
		presenter.saveRequested("fr", Arrays.asList("rm"), "ZerCollection", "zeColl", "supertimor");
		newWebDriver();
		waitUntilICloseTheBrowsers();
	}
}
