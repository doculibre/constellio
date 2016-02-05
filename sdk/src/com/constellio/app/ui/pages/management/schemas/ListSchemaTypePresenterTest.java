package com.constellio.app.ui.pages.management.schemas;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mock;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedFactories;

public class ListSchemaTypePresenterTest extends ConstellioTest {

	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypeVO schemaVO;
	@Mock RecordServices recordServices;
	ListSchemaTypePresenter presenter;
	MockedFactories mockedFactories;
	@Mock ListSchemaTypeViewImpl view;
	@Mock MetadataSchema aSchema;
	@Mock MetadataSchemaTypes types;

	@Before
	public void setUp() {
		mockedFactories = new MockedFactories();
		presenter = spy(new ListSchemaTypePresenter(view));
	}

	//@Test
	public void whenEditButtonClickedThenNavigateToEditSchemas()
			throws Exception {
		when(schemaVO.getCode()).thenReturn("zeId");
		presenter.editButtonClicked(schemaVO);
		//verify(view.navigateTo(), times(1)).editSchema("zeId");
	}
}
