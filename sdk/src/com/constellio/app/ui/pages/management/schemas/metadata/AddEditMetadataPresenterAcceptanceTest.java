package com.constellio.app.ui.pages.management.schemas.metadata;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.model.entities.Language;
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
	Map<String, String> newLabels = new HashMap<>();

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
		assertThat(resultVO.getLabel("fr")).isEqualTo(stringDefault.getLabel(Language.French));
	}

	@Test
	public void givenNewMetadataFormFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeSchema.code());

		newLabels.put("fr", "zeTitle");
		FormMetadataVO newMetadataForm = new FormMetadataVO(zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, false, false, true, "default",
				null, null, false, view.getSessionContext());

		presenter.saveButtonClicked(newMetadataForm, false);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(zeSchema.code() + "_USRzeMetadataCode");

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(zeSchema.code() + "_USRzeMetadataCode");
		assertThat(result.getLabel(Language.French)).isEqualTo("zeTitle");
		assertThat(result.getType()).isEqualTo(MetadataValueType.BOOLEAN);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
		assertThat(result.isDuplicable()).isFalse();
	}

	//TODO Maxime Broken @Test@Test
	public void givenNewMetadataFormFromCustomSchemFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		doNothing().when(navigator).to().listSchema(zeCustomSchema.code());
		presenter.setSchemaCode(zeCustomSchema.code());

		newLabels.put("fr", "zeTitle");
		FormMetadataVO newMetadataForm = new FormMetadataVO(zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, false, false, true, "default",
				null, null, false, view.getSessionContext());

		presenter.saveButtonClicked(newMetadataForm, false);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(zeCustomSchema.code() + "_USRzeMetadataCode");

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(zeCustomSchema.code() + "_USRzeMetadataCode");
		assertThat(result.getLabel(Language.French)).isEqualTo("zeTitle");
		assertThat(result.getType()).isEqualTo(MetadataValueType.BOOLEAN);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
		assertThat(result.isDuplicable()).isFalse();
	}

	@Test
	public void givenEditMetadataFormFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeSchema.code());
		Metadata stringMeta = zeSchema.stringMetadata();

		newLabels.put("fr", "zeTitleChanged");
		FormMetadataVO newMetadataForm = new FormMetadataVO(stringMeta.getCode(), MetadataValueType.STRING, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, false, false, true, "default",
				null, null, false, view.getSessionContext());

		presenter.saveButtonClicked(newMetadataForm, true);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				stringMeta.getCode());

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(stringMeta.getCode());
		assertThat(result.getLabel(Language.French)).isEqualTo("zeTitleChanged");
		assertThat(result.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
		assertThat(result.isDuplicable()).isFalse();
	}

	@Test
	public void givenEditMetadataFormFromCustomSchemaFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeCustomSchema.code());
		Metadata stringMeta = zeCustomSchema.stringMetadata();

		newLabels.put("fr", "zeTitleChanged");
		FormMetadataVO newMetadataForm = new FormMetadataVO(stringMeta.getCode(), MetadataValueType.STRING, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.HORIZONTAL, false, false, true, "default",
				null, null, false, view.getSessionContext());

		presenter.saveButtonClicked(newMetadataForm, true);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(
				stringMeta.getCode());

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(stringMeta.getCode());
		assertThat(result.getLabel(Language.French)).isEqualTo("zeTitleChanged");
		assertThat(result.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isTrue();
		assertThat(result.isDuplicable()).isFalse();
	}

	@Test
	public void givenEditInheritedMetadataInputMaskThenAllSchemaSaved()
			throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		Map<String, String> labels = new HashMap<>();
		labels.put("fr", "zeNewSchema");
		types.getDefaultSchema("zeSchemaType").create("zeMask").setInputMask("9999-9999").addLabel(Language.French, "Mask").setType(MetadataValueType.STRING);
		types.getSchemaType("zeSchemaType").createCustomSchema("zeNewSchema", labels).setDefaultSchema(types.getDefaultSchema("zeSchemaType"));
		schemasManager.saveUpdateSchemaTypes(types);

		Metadata zeNewSchemaMetadata = rm.schema("zeSchemaType_zeNewSchema").getMetadata("zeMask");
		Metadata defaultMetadata = rm.schema("zeSchemaType_default").getMetadata("zeMask");

		assertThat(zeNewSchemaMetadata.getInputMask()).isEqualTo("9999-9999");
		assertThat(defaultMetadata.getInputMask()).isEqualTo("9999-9999");

		presenter.setSchemaCode("zeSchemaType_zeNewSchema");

		FormMetadataVO newMetadataForm = new FormMetadataVO("zeSchemaType_zeNewSchema_zeMask", MetadataValueType.STRING, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, false, false, true, "default",
				null, "AAAA-AAAA", false, view.getSessionContext());

		presenter.saveButtonClicked(newMetadataForm, true);

		zeNewSchemaMetadata = rm.schema("zeSchemaType_zeNewSchema").getMetadata("zeMask");
		defaultMetadata = rm.schema("zeSchemaType_default").getMetadata("zeMask");

		assertThat(zeNewSchemaMetadata.getInputMask()).isEqualTo("AAAA-AAAA");
		assertThat(defaultMetadata.getInputMask()).isEqualTo("AAAA-AAAA");
	}

	@Test
	public void givenEditMetadataFormFromCustomSchemaFilledWhenSaveButtonClickThenDisplayConfigSaved()
			throws Exception {
		presenter.setSchemaCode(zeCustomSchema.code());
		Metadata stringMeta = zeCustomSchema.stringMetadata();

		newLabels.put("fr", "zeTitleChanged");
		FormMetadataVO newMetadataForm = new FormMetadataVO(stringMeta.getCode(), MetadataValueType.REFERENCE, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.RADIO_BUTTONS, MetadataDisplayType.HORIZONTAL, false, false, true, "default",
				null, null, false, view.getSessionContext());

		presenter.saveButtonClicked(newMetadataForm, true);

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager
				.getMetadata(zeCollection, stringMeta.getCode());

		assertThat(metadataDisplayConfig).isNotNull();
		assertThat(metadataDisplayConfig.getInputType()).isEqualTo(MetadataInputType.RADIO_BUTTONS);
		assertThat(metadataDisplayConfig.getDisplayType()).isEqualTo(MetadataDisplayType.HORIZONTAL);
	}
}
