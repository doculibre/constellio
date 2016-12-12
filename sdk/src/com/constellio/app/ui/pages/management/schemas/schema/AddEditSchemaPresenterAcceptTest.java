package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class AddEditSchemaPresenterAcceptTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	AddEditSchemaPresenter presenter;
	MetadataSchemasManager metadataSchemasManager;
	SchemasDisplayManager metadataSchemasDisplayManager;
	Map<String, String> parameters;
	@Mock AddEditSchemaView view;
	MockedNavigation navigator;
	String language;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);

		navigator = new MockedNavigation();

		defineSchemasManager()
				.using(setup.andCustomSchema().withAStringMetadataInCustomSchema(whichIsMultivalue, whichIsSearchable)
						.withAStringMetadata(whichIsSortable, whichIsEnabled).withABooleanMetadata(whichIsEnabled)
						.withADateMetadata(whichIsEnabled));
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		language = FakeSessionContext.adminInCollection(zeCollection).getCurrentLocale().getLanguage();
		presenter = new AddEditSchemaPresenter(view);
		parameters = new HashMap<>();
		parameters.put("schemaTypeCode", setup.zeCustomSchemaTypeCode());
	}

	@Test
	public void givenSchemaCodeWhenSetParametersThenEditMode()
			throws Exception {

		parameters.put("schemaCode", zeSchema.code());
		presenter.setParameters(parameters);
		assertThat(presenter.isEditMode()).isTrue();
	}

	@Test
	public void givenNoSchemaCodeWhenSetParametersThenAddMode()
			throws Exception {

		presenter.setParameters(parameters);
		assertThat(presenter.isEditMode()).isFalse();
	}

	@Test
	public void givenAddModeWhenSaveButtonClickedThenCustomSchema()
			throws Exception {

		presenter.setParameters(parameters);
		
		FormMetadataSchemaVO formMetadataSchemaVO = new FormMetadataSchemaVO(FakeSessionContext.adminInCollection(zeCollection));
		formMetadataSchemaVO.setLocalCode("newSchema");
		formMetadataSchemaVO.addLabel(language, "new schema Label");
		presenter.setSchemaVO(formMetadataSchemaVO);

		presenter.saveButtonClicked();

		assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_USRnewSchema")
				.getLabel(Language.French))
				.isEqualTo(
						"new schema Label");

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigate().to()).listSchema(params);
	}

	@Test(expected=MetadataSchemasRuntimeException.NoSuchSchema.class)
	public void givenCodeWithSpaceWhenSaveButtonClickedThenErrorAndSchemaNotCreated()
			throws Exception {

		presenter.setParameters(parameters);

		FormMetadataSchemaVO formMetadataSchemaVO = new FormMetadataSchemaVO(FakeSessionContext.adminInCollection(zeCollection));
		formMetadataSchemaVO.setLocalCode("new Schema");
		formMetadataSchemaVO.addLabel(language, "new schema Label");
		presenter.setSchemaVO(formMetadataSchemaVO);

		presenter.saveButtonClicked();

		verify(view).showErrorMessage($("AddEditSchemaView.schemaCodeContainsSpace"));

		metadataSchemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_USRnewSchema");
	}

	@Test(expected=MetadataSchemasRuntimeException.NoSuchSchema.class)
	public void givenCodeStartingWithNumberWhenSaveButtonClickedThenErrorAndSchemaNotCreated()
			throws Exception {

		presenter.setParameters(parameters);

		FormMetadataSchemaVO formMetadataSchemaVO = new FormMetadataSchemaVO(FakeSessionContext.adminInCollection(zeCollection));
		formMetadataSchemaVO.setLocalCode("3newSchema");
		formMetadataSchemaVO.addLabel(language, "new schema Label");
		presenter.setSchemaVO(formMetadataSchemaVO);

		presenter.saveButtonClicked();

		verify(view).showErrorMessage($("AddEditSchemaView.schemaCodeStartsWithNumber"));

		metadataSchemasManager.getSchemaTypes(zeCollection).getSchema("zeSchemaType_USRnewSchema");
	}

	@Test
	public void givenEditModeWhenSaveButtonClickedThenCustomSchema()
			throws Exception {

		parameters.put("schemaCode", zeSchema.code());
		presenter.setParameters(parameters);

		FormMetadataSchemaVO formMetadataSchemaVO = new FormMetadataSchemaVO(zeSchema.code(), zeCollection, new HashMap<String, String>());
		formMetadataSchemaVO.addLabel(language, "new schema Label");
		presenter.setSchemaVO(formMetadataSchemaVO);

		presenter.saveButtonClicked();

		assertThat(metadataSchemasManager.getSchemaTypes(zeCollection).getSchema(zeSchema.code()).getLabel(Language.French))
				.isEqualTo(
						"new schema Label");
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigate().to()).listSchema(params);
	}


	@Test
	public void givenEditModeWhenSaveButtonClickedThenCustomSchemaTypeDisplay()
			throws Exception {

		parameters.put("schemaCode", zeSchema.code());
		parameters.put("schemaTypeCode", zeSchema.type().getCode());
		presenter.setParameters(parameters);

		assertThat(metadataSchemasDisplayManager.getType(zeCollection, zeSchema.type().getCode()).isAdvancedSearch())
				.isEqualTo(
						Boolean.FALSE);

		FormMetadataSchemaVO formMetadataSchemaVO = new FormMetadataSchemaVO(zeSchema.code(), zeCollection, new HashMap<String, String>());
		formMetadataSchemaVO.setAdvancedSearch(true);
		presenter.setSchemaVO(formMetadataSchemaVO);

		presenter.saveButtonClicked();

		SchemaTypeDisplayConfig schemaTypeDisplayConfig = metadataSchemasDisplayManager.getType(zeCollection, zeSchema.type().getCode());

		assertThat(metadataSchemasDisplayManager.getType(zeCollection, zeSchema.type().getCode()).isAdvancedSearch())
				.isEqualTo(
						Boolean.TRUE);

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigate().to()).listSchema(params);
	}

	@Test
	public void whenCancelButtonClickedThenNavigateToListSchemas()
			throws Exception {

		presenter.setParameters(parameters);

		presenter.cancelButtonClicked();

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		verify(view.navigate().to()).listSchema(params);
	}

	@Test
	public void whenAddModeThenCodeEditable()
			throws Exception {

		presenter.setParameters(parameters);
		assertThat(presenter.isCodeEditable()).isTrue();
	}

	@Test
	public void givenCustomSchemaWhenEditModeThenCodeEditable()
			throws Exception {

		parameters.put("schemaCode", zeCustomSchema.code());
		presenter.setParameters(parameters);
		assertThat(presenter.isCodeEditable()).isTrue();
	}

	@Test
	public void givenDefaultSchemaWhenEditModeThenCodeNotEditable()
			throws Exception {

		parameters.put("schemaCode", zeSchema.code());
		presenter.setParameters(parameters);
		assertThat(presenter.isCodeEditable()).isFalse();
	}
	
}
