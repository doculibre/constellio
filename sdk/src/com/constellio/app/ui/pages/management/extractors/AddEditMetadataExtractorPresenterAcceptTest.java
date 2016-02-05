package com.constellio.app.ui.pages.management.extractors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.extractors.builders.RegexConfigToVOBuilder;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class AddEditMetadataExtractorPresenterAcceptTest extends ConstellioTest {

	@Mock AddEditMetadataExtractorView view;
	@Mock ConstellioNavigator navigator;

	AddEditMetadataExtractorPresenter presenter;
	SessionContext sessionContext;
	RMTestRecords rmTestRecords = new RMTestRecords(zeCollection);
	MetadataSchemasManager metadataSchemasManager;
	//	MetadataSchemaTypes types;

	MetadataSchemaTypeToVOBuilder metadataSchemaTypeToVOBuilder = new MetadataSchemaTypeToVOBuilder();
	MetadataSchemaToVOBuilder metadataSchemaToVOBuilder = new MetadataSchemaToVOBuilder();
	MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
	RegexConfigToVOBuilder regexConfigToVOBuilder = new RegexConfigToVOBuilder();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(rmTestRecords).withAllTestUsers()
		);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = new AddEditMetadataExtractorPresenter(view);
	}

	@Test
	public void givenUserWithPageAccessWhenHasPageAccessThenReturnFalse()
			throws Exception {

		assertThat(presenter.hasPageAccess("", rmTestRecords.getAdmin())).isTrue();
	}

	@Test
	public void givenUserWithoutPageAccessWhenHasPageAccessThenReturnFalse()
			throws Exception {
		assertThat(presenter.hasPageAccess("", rmTestRecords.getBob_userInAC())).isFalse();
	}

	@Test
	public void whenBackButtonClickedThenOk()
			throws Exception {

		presenter.backButtonClicked();

		verify(view.navigateTo()).listMetadataExtractors();
	}

	@Test
	public void whenCancelButtonClickedThenOk()
			throws Exception {

		presenter.cancelButtonClicked();

		verify(view.navigateTo()).listMetadataExtractors();
	}

	@Test
	public void givenAddActionMetadataExtractorWhenSaveButtonClickedThenAdded()
			throws Exception {

		presenter.forParams("");

		MetadataExtractorVO metadataExtractorVO = presenter.getMetadataExtractorVO();

		MetadataSchemaType schemaType = types().getSchemaType(Email.SCHEMA_TYPE);
		MetadataSchemaTypeVO metadataSchemaTypeVO = metadataSchemaTypeToVOBuilder.build(schemaType, sessionContext);

		MetadataSchema schema = types().getSchema(Email.SCHEMA);
		MetadataSchemaVO schemaVo = metadataSchemaToVOBuilder.build(schema, VIEW_MODE.FORM, sessionContext);

		presenter.schemaTypeSelected(metadataSchemaTypeVO);
		presenter.schemaSelected(schemaVo);

		Metadata metadata = types()
				.getMetadata(schemaVo.getCode() + "_" + Email.DESCRIPTION);
		MetadataVO metadataVO = metadataToVOBuilder.build(metadata, schemaVo, sessionContext);

		metadataExtractorVO.setMetadataVO(metadataVO);
		RegexConfigVO regexConfigVO = new RegexConfigVO("inputMetadata", "regex", "value",
				RegexConfigType.TRANSFORMATION);
		metadataExtractorVO.setProperties(Arrays.asList("property1"));
		metadataExtractorVO.setStyles(Arrays.asList("style1"));
		metadataExtractorVO.setRegexes(Arrays.asList(regexConfigVO));

		presenter.saveButtonClicked();

		verify(view.navigateTo()).listMetadataExtractors();

		assertThat(types().getMetadata(schemaVo.getCode() + "_" + Email.DESCRIPTION).getPopulateConfigs().getStyles())
				.isEqualTo(Arrays.asList("style1"));
		assertThat(types().getMetadata(schemaVo.getCode() + "_" + Email.DESCRIPTION).getPopulateConfigs().getProperties())
				.isEqualTo(Arrays.asList("property1"));
		assertThat(types().getMetadata(schemaVo.getCode() + "_" + Email.DESCRIPTION).getPopulateConfigs().getRegexes())
				.hasSize(1);
		RegexConfig regexConfig = types().getMetadata(schemaVo.getCode() + "_" + Email.DESCRIPTION).getPopulateConfigs()
				.getRegexes().get(0);
		assertThat(regexConfig.getInputMetadata()).isEqualTo(regexConfigVO.getInputMetadata());
		assertThat(regexConfig.getValue()).isEqualTo(regexConfigVO.getValue());
		assertThat(regexConfig.getRegex().toString()).isEqualTo(Pattern.compile(regexConfigVO.getRegex()).toString());
		assertThat(regexConfig.getRegexConfigType()).isEqualTo(regexConfigVO.getRegexConfigType());
	}

	@Test
	public void givenEditActionWhenSaveButtonClickedThenUpdate()
			throws Exception {

		String metadataCode = Document.DEFAULT_SCHEMA + "_" + Document.TITLE;
		presenter.forParams(metadataCode);
		MetadataExtractorVO metadataExtractorVO = presenter.getMetadataExtractorVO();
		metadataExtractorVO.setProperties(Arrays.asList("property1"));
		metadataExtractorVO.setStyles(Arrays.asList("style1"));
		RegexConfigVO regexConfigVO = new RegexConfigVO("inputMetadata", "regex", "value",
				RegexConfigType.SUBSTITUTION);
		metadataExtractorVO.setRegexes(Arrays.asList(regexConfigVO));

		presenter.saveButtonClicked();

		verify(view.navigateTo()).listMetadataExtractors();
		assertThat(types().getMetadata(metadataCode).getPopulateConfigs().getStyles()).isEqualTo(Arrays.asList("style1"));
		assertThat(types().getMetadata(metadataCode).getPopulateConfigs().getProperties()).isEqualTo(Arrays.asList("property1"));
		assertThat(types().getMetadata(metadataCode).getPopulateConfigs().getRegexes()).hasSize(1);
		RegexConfig regexConfig = types().getMetadata(metadataCode).getPopulateConfigs().getRegexes().get(0);
		assertThat(regexConfig.getInputMetadata()).isEqualTo(regexConfigVO.getInputMetadata());
		assertThat(regexConfig.getValue()).isEqualTo(regexConfigVO.getValue());
		assertThat(regexConfig.getRegex().toString()).isEqualTo(Pattern.compile(regexConfigVO.getRegex()).toString());
		assertThat(regexConfig.getRegexConfigType()).isEqualTo(regexConfigVO.getRegexConfigType());
	}

	@Test
	public void givenEditActionThenNoSameMetadataInMetadataRegexList()
			throws Exception {
		String metadataCode = Document.DEFAULT_SCHEMA + "_" + Document.TITLE;
		presenter.forParams(metadataCode);

		assertThat(presenter.getMetadataVOsForRegexes(Document.DEFAULT_SCHEMA, presenter.getMetadataVO())).extracting("code")
				.doesNotContain(metadataCode);

	}

	private MetadataSchemaTypes types() {
		return metadataSchemasManager.getSchemaTypes(zeCollection);
	}
}