package com.constellio.app.ui.pages.management.schemas.metadata;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.components.dialogs.ConfirmDialogProperties;
import com.constellio.app.ui.framework.components.dialogs.ConfirmDialogResults;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class AddEditMetadataPresenterAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	AddEditMetadataPresenter presenter;
	@Mock AddEditMetadataViewImpl view;
	MockedNavigation navigator;
	Map<String, String> newLabels = new HashMap<>();
	UserServices userServices;

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
		userServices = getModelLayerFactory().newUserServices();
		userServices.execute(admin, (req) -> req.addToCollection(zeCollection));

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

	private MetadataDisplayConfig displayConfigOf(String code) {
		return getAppLayerFactory().getMetadataSchemasDisplayManager().getMetadata(zeCollection, code);
	}

	@Test
	public void whenAddEditingMetadataWithoutInheritanceInDefaultSchemaThenAdvancedSearchFlagSaved()
			throws Exception {

		presenter.setSchemaCode(zeSchema.code());
		presenter.setMetadataCode(zeSchema.stringMetadata().getCode());
		assertThat(displayConfigOf("zeSchemaType_default_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		assertThat(displayConfigOf("zeSchemaType_custom_stringMetadata").isVisibleInAdvancedSearch()).isFalse();

		FormMetadataVO formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isFalse();
		formMetadataVO.setAdvancedSearch(true);
		presenter.preSaveButtonClicked(formMetadataVO, true);

		assertThat(displayConfigOf("zeSchemaType_default_stringMetadata").isVisibleInAdvancedSearch()).isTrue();
		assertThat(displayConfigOf("zeSchemaType_custom_stringMetadata").isVisibleInAdvancedSearch()).isTrue();
		formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isTrue();

		formMetadataVO.setAdvancedSearch(false);
		presenter.preSaveButtonClicked(formMetadataVO, true);

		assertThat(displayConfigOf("zeSchemaType_default_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		assertThat(displayConfigOf("zeSchemaType_custom_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isFalse();
	}

	//@Test
	public void whenAddedCustomMetadataDuplicatesAnotherCustomMetadataDuplicationHandlingHappens() {

		doAnswer(invocation -> {
			invocation.getArgumentAt(0, ConfirmDialogProperties.class).getOnCloseListener().accept(ConfirmDialogResults.OK);
			return null;
		}).when(view).showConfirmDialog(any(ConfirmDialogProperties.class));

		String anotherCustomSchemaCode = "anotherCustom";

		String duplicatedMetadataCode = "metadataToDuplicate";
		String usrDuplicatedMetadataCode = "USR" + duplicatedMetadataCode;

		MetadataValueType duplicatedMetadataValueType = MetadataValueType.STRING;
		boolean duplicatedMetadataIsMultivalue = false;

		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		schemasManager.modify(zeCollection, types -> {
			types.getSchemaType("zeSchemaType").createCustomSchema(anotherCustomSchemaCode);

			types.getSchema(zeCustomSchema.code())
					.create(usrDuplicatedMetadataCode)
					.setType(duplicatedMetadataValueType).setMultivalue(duplicatedMetadataIsMultivalue);
		});

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherCustomSchema = types.getSchema("zeSchemaType_" + anotherCustomSchemaCode);


		try {
			assertThat(anotherCustomSchema.getMetadata(usrDuplicatedMetadataCode)).isNotNull();
		} catch (MetadataSchemasRuntimeException metadataNotFoundException) {
			//Expected since the metadata has not been created
		}


		FormMetadataVO formMetadataVO = new FormMetadataVO(view.getSessionContext());
		formMetadataVO.setLocalcode(duplicatedMetadataCode);
		formMetadataVO.setValueType(duplicatedMetadataValueType);
		formMetadataVO.setMultivalue(duplicatedMetadataIsMultivalue);

		presenter.setSchemaCode(anotherCustomSchema.getCode());
		presenter.preSaveButtonClicked(formMetadataVO, false);

		types = schemasManager.getSchemaTypes(zeCollection);

		anotherCustomSchema = types.getSchema(anotherCustomSchema.getCode());
		assertThat(anotherCustomSchema.getMetadata(usrDuplicatedMetadataCode)).isNotNull();
	}

	@Test
	public void whenAddEditingMetadataWithInheritanceThenAdvancedSearchFlagNotSavedInInheritedMetadata()
			throws Exception {
		presenter.setSchemaCode(zeCustomSchema.code());
		presenter.setMetadataCode(zeCustomSchema.stringMetadata().getCode());
		assertThat(displayConfigOf("zeSchemaType_default_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		assertThat(displayConfigOf("zeSchemaType_custom_stringMetadata").isVisibleInAdvancedSearch()).isFalse();

		FormMetadataVO formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isFalse();
		formMetadataVO.setAdvancedSearch(true);
		presenter.preSaveButtonClicked(formMetadataVO, true);

		assertThat(displayConfigOf("zeSchemaType_default_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		assertThat(displayConfigOf("zeSchemaType_custom_stringMetadata").isVisibleInAdvancedSearch()).isTrue();
		formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isTrue();

		formMetadataVO.setAdvancedSearch(false);
		presenter.preSaveButtonClicked(formMetadataVO, true);

		assertThat(displayConfigOf("zeSchemaType_default_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		assertThat(displayConfigOf("zeSchemaType_custom_stringMetadata").isVisibleInAdvancedSearch()).isFalse();
		formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isFalse();
	}

	@Test
	public void whenAddEditingMetadataWithoutInheritanceInCustomSchemaThenAdvancedSearchFlagSaved()
			throws Exception {
		presenter.setSchemaCode(zeCustomSchema.code());
		presenter.setMetadataCode(zeCustomSchema.customStringMetadata().getCode());
		assertThat(displayConfigOf("zeSchemaType_custom_customString").isVisibleInAdvancedSearch()).isFalse();

		FormMetadataVO formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isFalse();
		formMetadataVO.setAdvancedSearch(true);
		presenter.preSaveButtonClicked(formMetadataVO, true);

		assertThat(displayConfigOf("zeSchemaType_custom_customString").isVisibleInAdvancedSearch()).isTrue();
		formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isTrue();

		formMetadataVO.setAdvancedSearch(false);
		presenter.preSaveButtonClicked(formMetadataVO, true);

		assertThat(displayConfigOf("zeSchemaType_custom_customString").isVisibleInAdvancedSearch()).isFalse();
		formMetadataVO = presenter.getFormMetadataVO();
		assertThat(formMetadataVO.isAdvancedSearch()).isFalse();

	}

//	@Test(expected = MetadataBuilderRuntimeException.CannotHaveMaxLengthSpecifiedIfNotOfTypeStringOrText.class)
	//	public void givenNewMetadataFormFilledWithMaxLenghtOnBooleanWhenSaveButtonClickThenThrow()
	//			throws Exception {
	//		presenter.setSchemaCode(zeSchema.code());
	//
	//		newLabels.put("fr", "zeTitle");
	//		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
	//				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, null,
	//				false, false, false, true, "default",
	//				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, 10, null, null);
	//
	//		presenter.preSaveButtonClicked(newMetadataForm, false);
	//	}
	//
	//	@Test(expected = MetadataBuilderRuntimeException.CannotHaveMeasurementUnitSpecifiedIfNotOfTypeIntegerOrNumber.class)
	//	public void givenNewMetadataFormFilledWithMesurementUnitOnBooleanWhenSaveButtonClickThenThow()
	//			throws Exception {
	//		presenter.setSchemaCode(zeSchema.code());
	//
	//		newLabels.put("fr", "zeTitle");
	//		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
	//				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER,
	//				false, false, false, true, "default",
	//				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, null, "em", null);
	//
	//		presenter.preSaveButtonClicked(newMetadataForm, false);
	//	}

	@Test
	public void givenNewMetadataWithMaxLenghtFormFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeSchema.code());

		newLabels.put("fr", "zeTitle");
		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, zeSchema.code() + "_zeMetadataCode", MetadataValueType.TEXT, false,
				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, null,
				false, false, false, true, "default",
				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, 10, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.preSaveButtonClicked(newMetadataForm, false);

		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(zeSchema.code() + "_USRzeMetadataCode");

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo(zeSchema.code() + "_USRzeMetadataCode");
		assertThat(result.getLabel(Language.French)).isEqualTo("zeTitle");
		assertThat(result.getType()).isEqualTo(MetadataValueType.TEXT);
		assertThat(result.isDefaultRequirement()).isFalse();
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.isSchemaAutocomplete()).isFalse();
		assertThat(result.isMultivalue()).isFalse();
		assertThat(result.isSortable()).isFalse();
		assertThat(result.isDuplicable()).isFalse();
		assertThat(result.getMaxLength()).isEqualTo(10);
		assertThat(result.getMeasurementUnit()).isNull();
	}

//	@Test
//	public void givenNewMetadataWithMesurementUnitFormFilledWhenSaveButtonClickThenMetadataSaved()
//			throws Exception {
//		presenter.setSchemaCode(zeSchema.code());
//
//		newLabels.put("fr", "zeTitle");
//		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, zeSchema.code() + "_zeMetadataCode", MetadataValueType.INTEGER, false,
//				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, null,
//				false, false, false, true, "default",
//				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, null, "px", null);
//
//		presenter.preSaveButtonClicked(newMetadataForm, false);
//
//		Metadata result = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
//				.getMetadata(zeSchema.code() + "_USRzeMetadataCode");
//
//		assertThat(result).isNotNull();
//		assertThat(result.getCode()).isEqualTo(zeSchema.code() + "_USRzeMetadataCode");
//		assertThat(result.getLabel(Language.French)).isEqualTo("zeTitle");
//		assertThat(result.getType()).isEqualTo(MetadataValueType.INTEGER);
//		assertThat(result.isDefaultRequirement()).isFalse();
//		assertThat(result.isEnabled()).isTrue();
//		assertThat(result.isSchemaAutocomplete()).isFalse();
//		assertThat(result.isMultivalue()).isFalse();
//		assertThat(result.isSortable()).isFalse();
//		assertThat(result.isDuplicable()).isFalse();
//		assertThat(result.getMaxLength()).isNull();
//		assertThat(result.getMeasurementUnit()).isEqualTo("px");
//	}

	//TODO Maxime Broken @Test@Test
	public void givenNewMetadataFormFromCustomSchemFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		doNothing().when(navigator).to().listSchema(zeCustomSchema.code());
		presenter.setSchemaCode(zeCustomSchema.code());

		newLabels.put("fr", "zeTitle");
		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, zeSchema.code() + "_zeMetadataCode", MetadataValueType.BOOLEAN, false,
				null, "", newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER,
				false, false, false, true, "default",
				null, null, false, false, new HashSet<String>(), view.getSessionContext(), false, null, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.preSaveButtonClicked(newMetadataForm, false);

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
		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, stringMeta.getCode(), MetadataValueType.INTEGER, false, null, "",
				newLabels, false, false, true, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER, false, false, false,
				true, "default",
				null, null, false, false, new HashSet<String>(), view.getSessionContext(), false, 5, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.metadata = stringMeta;
		presenter.preSaveButtonClicked(newMetadataForm, true);

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
		assertThat(result.isUniqueValue()).isFalse();
		assertThat(result.getMaxLength()).isEqualTo(5);
		assertThat(result.getMeasurementUnit()).isNull();

	}

	@Test
	public void givenEditMetadataFormFromCustomSchemaFilledWhenSaveButtonClickThenMetadataSaved()
			throws Exception {
		presenter.setSchemaCode(zeCustomSchema.code());
		Metadata stringMeta = zeCustomSchema.stringMetadata();

		newLabels.put("fr", "zeTitleChanged");
		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, stringMeta.getCode(), MetadataValueType.STRING, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.HORIZONTAL, MetadataSortingType.ENTRY_ORDER, false, false,
				false, true, "default",
				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, null, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.preSaveButtonClicked(newMetadataForm, true);

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
		assertThat(result.isUniqueValue()).isTrue();
		assertThat(result.getMaxLength()).isNull();
		assertThat(result.getMeasurementUnit()).isNull();

	}

	@Test
	public void givenEditInheritedMetadataInputMaskThenAllSchemaSaved()
			throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		Map<String, String> labels = new HashMap<>();
		labels.put("fr", "zeNewSchema");
		types.getDefaultSchema("zeSchemaType").create("zeMask").setInputMask("9999-9999").addLabel(Language.French, "Mask")
				.setType(MetadataValueType.STRING);
		types.getSchemaType("zeSchemaType").createCustomSchema("zeNewSchema", labels)
				.setDefaultSchema(types.getDefaultSchema("zeSchemaType"));
		schemasManager.saveUpdateSchemaTypes(types);

		Metadata zeNewSchemaMetadata = rm.schema("zeSchemaType_zeNewSchema").getMetadata("zeMask");
		Metadata defaultMetadata = rm.schema("zeSchemaType_default").getMetadata("zeMask");

		assertThat(zeNewSchemaMetadata.getInputMask()).isEqualTo("9999-9999");
		assertThat(defaultMetadata.getInputMask()).isEqualTo("9999-9999");

		presenter.setSchemaCode("zeSchemaType_zeNewSchema");

		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, "zeSchemaType_zeNewSchema_zeMask", MetadataValueType.STRING, false,
				null, "",
				newLabels, false, false, false, false, false, MetadataInputType.FIELD, MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER, false, false, false,
				true, "default",
				null, "AAAA-AAAA", false, true, new HashSet<String>(), view.getSessionContext(), false, null, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.preSaveButtonClicked(newMetadataForm, true);

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
		FormMetadataVO newMetadataForm = new FormMetadataVO((short) 0, stringMeta.getCode(), MetadataValueType.REFERENCE, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.RADIO_BUTTONS, MetadataDisplayType.HORIZONTAL, MetadataSortingType.ENTRY_ORDER,
				false, false, false, true, "default",
				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, null, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.preSaveButtonClicked(newMetadataForm, true);

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager
				.getMetadata(zeCollection, stringMeta.getCode());

		assertThat(metadataDisplayConfig).isNotNull();
		assertThat(metadataDisplayConfig.getInputType()).isEqualTo(MetadataInputType.RADIO_BUTTONS);
		assertThat(metadataDisplayConfig.getDisplayType()).isEqualTo(MetadataDisplayType.HORIZONTAL);

		newMetadataForm = new FormMetadataVO((short) 0, stringMeta.getCode(), MetadataValueType.REFERENCE, false, null, "",
				newLabels, false, false, false, false, false, MetadataInputType.RADIO_BUTTONS, MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER,
				false, false, false, true, "default",
				null, null, false, true, new HashSet<String>(), view.getSessionContext(), false, null, null, null,
				DataEntryType.MANUAL, null, null);

		presenter.preSaveButtonClicked(newMetadataForm, true);

		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		metadataDisplayConfig = schemasDisplayManager
				.getMetadata(zeCollection, stringMeta.getCode());

		assertThat(metadataDisplayConfig).isNotNull();
		assertThat(metadataDisplayConfig.getInputType()).isEqualTo(MetadataInputType.RADIO_BUTTONS);
		assertThat(metadataDisplayConfig.getDisplayType()).isEqualTo(MetadataDisplayType.VERTICAL);
	}
}
