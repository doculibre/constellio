package com.constellio.app.ui.pages.management.schemas.metadata;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class AddEditMetadataPresenterAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	AddEditMetadataPresenter presenter;
	@Mock AddEditMetadataViewImpl view;
	MockedNavigation navigator;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);

		navigator = new MockedNavigation();

		defineSchemasManager()
				.using(setup.andCustomSchema().withAStringMetadataInCustomSchema(whichIsMultivalue, whichIsSearchable)
						.withAStringMetadata(whichIsSortable, whichIsEnabled));
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		presenter = new AddEditMetadataPresenter(view);
		Map<String, String> parameters = new HashMap<>();
		parameters.put("schemaTypeCode", setup.zeCustomSchemaTypeCode());
		presenter.setParameters(parameters);
	}

	@Test
	public void givenNewMetadataWhenGetFormThenNullForm()
			throws Exception {
		FormMetadataVO resultVO = presenter.getFormMetadataVO();
		assertThat(resultVO).isNull();
	}

	//TODO Maxime Broken @Test
	public void givenEditMetadataWhenGetFormThenCorrectForm()
			throws Exception {
		Metadata stringDefault = zeSchema.stringMetadata();
		presenter.setSchemaCode(zeSchema.code());
		presenter.setMetadataCode(stringDefault.getCode());

		FormMetadataVO resultVO = presenter.getFormMetadataVO();

		assertThat(resultVO).isNotNull();
		assertThat(resultVO.getCode()).isEqualTo(stringDefault.getCode());
		assertThat(resultVO.getLabel()).isEqualTo(stringDefault.getLabel());
	}

	@Test
	public void givenNewMetadataFormFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeSchema.code());

		FormMetadataVO newMetadataForm = new FormMetadataVO(zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
				null, "", "zeTitle", false, false, false, false, false, MetadataInputType.FIELD, false, false, true, "default",
				null, null);

		presenter.saveButtonClicked(newMetadataForm, false);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(zeSchema.code() + "_USRzeMetadataCode");

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(zeSchema.code() + "_USRzeMetadataCode");
		assertThat(result.getLabel()).isEqualTo("zeTitle");
		assertThat(result.getType()).isEqualTo(MetadataValueType.BOOLEAN);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
	}

	//TODO Maxime Broken @Test@Test
	public void givenNewMetadataFormFromCustomSchemFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		doNothing().when(navigator).to().listSchema(zeCustomSchema.code());
		presenter.setSchemaCode(zeCustomSchema.code());

		FormMetadataVO newMetadataForm = new FormMetadataVO(zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
				null, "", "zeTitle", false, false, false, false, false, MetadataInputType.FIELD, false, false, true, "default",
				null, null);

		presenter.saveButtonClicked(newMetadataForm, false);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(zeCustomSchema.code() + "_USRzeMetadataCode");

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(zeCustomSchema.code() + "_USRzeMetadataCode");
		assertThat(result.getLabel()).isEqualTo("zeTitle");
		assertThat(result.getType()).isEqualTo(MetadataValueType.BOOLEAN);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
	}

	@Test
	public void givenEditMetadataFormFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeSchema.code());
		Metadata stringMeta = zeSchema.stringMetadata();

		FormMetadataVO newMetadataForm = new FormMetadataVO(stringMeta.getCode(), MetadataValueType.STRING, false, null, "",
				"zeTitleChanged", false, false, false, false, false, MetadataInputType.FIELD, false, false, true, "default",
				null, null);

		presenter.saveButtonClicked(newMetadataForm, true);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				stringMeta.getCode());

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(stringMeta.getCode());
		assertThat(result.getLabel()).isEqualTo("zeTitleChanged");
		assertThat(result.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
	}

	@Test
	public void givenEditMetadataFormFromCustomSchemaFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeCustomSchema.code());
		Metadata stringMeta = zeCustomSchema.stringMetadata();

		FormMetadataVO newMetadataForm = new FormMetadataVO(stringMeta.getCode(), MetadataValueType.STRING, false, null, "",
				"zeTitleChanged", false, false, false, false, false, MetadataInputType.FIELD, false, false, true, "default",
				null, null);

		presenter.saveButtonClicked(newMetadataForm, true);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				stringMeta.getCode());

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(stringMeta.getCode());
		assertThat(result.getLabel()).isEqualTo("zeTitleChanged");
		assertThat(result.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isTrue();
	}

}
