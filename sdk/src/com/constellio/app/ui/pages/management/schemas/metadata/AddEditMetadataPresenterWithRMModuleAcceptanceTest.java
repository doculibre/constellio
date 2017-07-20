package com.constellio.app.ui.pages.management.schemas.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;

public class AddEditMetadataPresenterWithRMModuleAcceptanceTest extends ConstellioTest {

	AddEditMetadataPresenter presenter;
	@Mock AddEditMetadataViewImpl view;
	MockedNavigation navigator;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		navigator = new MockedNavigation();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		presenter = new AddEditMetadataPresenter(view);
	}

	@Test
	public void givenNewMetadataWhenGetFormThenNullForm()
			throws Exception {

		Map<String, String> parameters = new HashMap<>();
		parameters.put("schemaTypeCode", ContainerRecord.SCHEMA_TYPE);
		parameters.put("schemaCode", ContainerRecord.DEFAULT_SCHEMA);
		presenter.setParameters(parameters);

		presenter.setSchemaCode(ContainerRecord.DEFAULT_SCHEMA);
		presenter.setMetadataCode(ContainerRecord.ADMINISTRATIVE_UNITS);
		assertThat(presenter.isMetadataRequiredStatusModifiable()).isTrue();

		presenter.setMetadataCode(ContainerRecord.DECOMMISSIONING_TYPE);
		assertThat(presenter.isMetadataRequiredStatusModifiable()).isTrue();

		presenter.setMetadataCode(ContainerRecord.FULL);
		assertThat(presenter.isMetadataRequiredStatusModifiable()).isFalse();

	}

}
