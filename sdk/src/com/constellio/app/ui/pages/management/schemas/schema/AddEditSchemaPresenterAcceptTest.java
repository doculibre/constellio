package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class AddEditSchemaPresenterAcceptTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	AddEditSchemaPresenter presenter;
	MetadataSchemasManager metadataSchemasManager;
	Map<String, String> parameters;
	@Mock AddEditSchemaView view;
	@Mock CoreViews navigator;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);
		defineSchemasManager()
				.using(setup.andCustomSchema().withAStringMetadataInCustomSchema(whichIsMultivalue, whichIsSearchable)
						.withAStringMetadata(whichIsSortable, whichIsEnabled).withABooleanMetadata(whichIsEnabled)
						.withADateMetadata(whichIsEnabled));
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = new AddEditSchemaPresenter(view);
		parameters = new HashMap<>();
		parameters.put("schemaTypeCode", setup.zeCustomSchemaTypeCode());
		presenter.setParameters(parameters);
	}

	@Test
	public void givenAddModeWhenSaveButtonClickedThenCustomSchema()
			throws Exception {

		FormMetadataSchemaVO formMetadataSchemaVO = new FormMetadataSchemaVO();
		formMetadataSchemaVO.setLocalCode("newSchema");
		formMetadataSchemaVO.setLabel("new schema Label");

		presenter.saveButtonClicked(formMetadataSchemaVO, false);

		assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_USRnewSchema")
				.getLabel(Language.French))
				.isEqualTo(
						"new schema Label");

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigateTo()).listSchema(params);
	}

	@Test
	public void givenEditModeWhenSaveButtonClickedThenCustomSchema()
			throws Exception {

		presenter.setSchemaCode(zeSchema.code());
		FormMetadataSchemaVO formMetadataSchemaVO = presenter.getSchemaVO();
		formMetadataSchemaVO.setLabel("new schema Label");

		presenter.saveButtonClicked(formMetadataSchemaVO, true);

		assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getSchema(zeSchema.code()).getLabel(Language.French))
				.isEqualTo(
						"new schema Label");
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigateTo()).listSchema(params);
	}

	@Test
	public void whenCancelButtonClickedThenNavigateToListSchemas()
			throws Exception {

		presenter.cancelButtonClicked();

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigateTo()).listSchema(params);
	}
}
