package com.constellio.app.ui.pages.management.schemas.display.display;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DisplayConfigPresenterAcceptanceTest extends ConstellioTest {
	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	DisplayConfigPresenter presenter;
	@Mock DisplayConfigView view;
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
							.withAStringMetadata(whichIsSortable, whichIsEnabled).withABooleanMetadata(whichIsEnabled)
							.withADateMetadata(whichIsEnabled));
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);

		presenter = new DisplayConfigPresenter(view);
		Map<String, String> parameters = new HashMap<>();
		parameters.put("schemaTypeCode", setup.zeCustomSchemaTypeCode());
		presenter.setParameters(parameters);
	}

	@Test
	public void givenSchemaWhenSaveDisplayConfigThenConfigSaved()
			throws Exception {
		List<Metadata> metadatas = Arrays.asList(zeSchema.booleanMetadata(), zeSchema.dateMetadata(), zeSchema.stringMetadata());
		List<FormMetadataVO> formMetadataVOs = this.getFormVO(metadatas);

		presenter.setSchemaCode(zeSchema.code());
		presenter.saveButtonClicked(formMetadataVOs);

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig config = manager.getSchema(zeCollection, zeSchema.code());
		assertThat(config.getDisplayMetadataCodes()).hasSize(3);
		assertThat(config.getDisplayMetadataCodes()).containsExactlyElementsOf(getCodeFromMetadata(metadatas));
		assertThat(config.getDisplayMetadataCodes().get(0)).isEqualTo(zeSchema.booleanMetadata().getCode());
		assertThat(config.getDisplayMetadataCodes().get(1)).isEqualTo(zeSchema.dateMetadata().getCode());
		assertThat(config.getDisplayMetadataCodes().get(2)).isEqualTo(zeSchema.stringMetadata().getCode());
	}

	@Test
	public void givenSchemaSavedWhenGetMetadataValueThenCorrect()
			throws Exception {
		List<Metadata> metadatas = Arrays.asList(zeSchema.booleanMetadata(), zeSchema.dateMetadata(), zeSchema.stringMetadata());
		List<FormMetadataVO> formMetadataVOs = this.getFormVO(metadatas);
		presenter.setSchemaCode(zeSchema.code());

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig config = manager.getSchema(zeCollection, zeSchema.code());
		config = config.withDisplayMetadataCodes(getCodeFromMetadata(metadatas));
		manager.saveSchema(config);

		List<FormMetadataVO> result = presenter.getValueMetadatas();
		assertThat(result).hasSize(3);
		assertThat(result).containsExactlyElementsOf(formMetadataVOs);
		assertThat(result.get(0).getCode()).isEqualTo(zeSchema.booleanMetadata().getCode());
		assertThat(result.get(1).getCode()).isEqualTo(zeSchema.dateMetadata().getCode());
		assertThat(result.get(2).getCode()).isEqualTo(zeSchema.stringMetadata().getCode());
	}

	@Test
	public void givenSchemaOrderEditedWhenGetMetadataValueThenCorrectOrder()
			throws Exception {
		presenter.setSchemaCode(zeSchema.code());
		List<FormMetadataVO> formMetadataVOs = presenter.getMetadatas();
		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig config = manager.getSchema(zeCollection, zeSchema.code());

		Collections.shuffle(formMetadataVOs);

		config = config.withDisplayMetadataCodes(getCodeFromVO(formMetadataVOs));
		manager.saveSchema(config);

		List<FormMetadataVO> result = presenter.getValueMetadatas();
		assertThat(result).containsExactlyElementsOf(formMetadataVOs);

		formMetadataVOs = presenter.getMetadatas();
		Collections.shuffle(formMetadataVOs);
		config = config.withDisplayMetadataCodes(getCodeFromVO(formMetadataVOs));
		manager.saveSchema(config);

		result = presenter.getValueMetadatas();
		assertThat(result).containsExactlyElementsOf(formMetadataVOs);
	}

	private List<String> getCodeFromMetadata(List<Metadata> metadatas) {
		List<String> codeList = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			codeList.add(metadata.getCode());
		}
		return codeList;
	}

	private List<String> getCodeFromVO(List<FormMetadataVO> metadataVOs) {
		List<String> codeList = new ArrayList<>();
		for (FormMetadataVO metadata : metadataVOs) {
			codeList.add(metadata.getCode());
		}
		return codeList;
	}

	private List<FormMetadataVO> getFormVO(List<Metadata> metadatas) {
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder(FakeSessionContext.adminInCollection(zeCollection));
		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			formMetadataVOs.add(builder.build(metadata, getAppLayerFactory().getMetadataSchemasDisplayManager(),
					setup.zeCustomSchemaTypeCode(), view.getSessionContext()));
		}

		return formMetadataVOs;
	}
}
