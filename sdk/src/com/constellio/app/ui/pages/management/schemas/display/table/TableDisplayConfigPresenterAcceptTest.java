package com.constellio.app.ui.pages.management.schemas.display.table;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

/**
 * Created by Patrick on 2016-01-11.
 */
public class TableDisplayConfigPresenterAcceptTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	TableDisplayConfigPresenter presenter;
	@Mock TableDisplayConfigView view;
	@Mock CoreViews navigator;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);
		defineSchemasManager()
				.using(setup.andCustomSchema()
						.withAStringMetadataInCustomSchema(whichIsMultivalue, whichIsSearchable)
						.withAStringMetadata(whichIsSortable, whichIsEnabled).withABooleanMetadata(whichIsEnabled)
						.withADateMetadata(whichIsEnabled));
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = new TableDisplayConfigPresenter(view);
		Map<String, String> parameters = new HashMap<>();
		parameters.put("schemaTypeCode", setup.zeCustomSchemaTypeCode());
		presenter.setParameters(parameters);
	}

	@Test
	public void givenSchemaWhenSaveTableDisplayConfigThenConfigSaved()
			throws Exception {
		List<Metadata> metadatas = Arrays.asList(zeSchema.booleanMetadata(), zeSchema.dateMetadata(), zeSchema.stringMetadata());
		List<FormMetadataVO> formMetadataVOs = this.getFormVO(metadatas);

		presenter.setSchemaCode(zeSchema.code());
		presenter.saveButtonClicked(formMetadataVOs);

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig config = manager.getSchema(zeCollection, zeSchema.code());
		assertThat(config.getTableMetadataCodes()).hasSize(4);
		assertThat(config.getTableMetadataCodes().get(0)).isEqualTo(zeSchema.code() + "_" + Schemas.TITLE_CODE);
		assertThat(config.getTableMetadataCodes().get(1)).isEqualTo(zeSchema.booleanMetadata().getCode());
		assertThat(config.getTableMetadataCodes().get(2)).isEqualTo(zeSchema.dateMetadata().getCode());
		assertThat(config.getTableMetadataCodes().get(3)).isEqualTo(zeSchema.stringMetadata().getCode());
	}

	@Test
	public void givenDefaultAndCustomSchemasWhenSaveTableDisplayConfigThenEqualsConfigForBoth()
			throws Exception {
		List<Metadata> metadatas = Arrays.asList(zeSchema.booleanMetadata(), zeSchema.dateMetadata(), zeSchema.stringMetadata());
		List<FormMetadataVO> formMetadataVOs = this.getFormVO(metadatas);

		presenter.setSchemaCode(zeSchema.code());
		presenter.saveButtonClicked(formMetadataVOs);

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig config = manager.getSchema(zeCollection, zeSchema.code());
		assertThat(config.getTableMetadataCodes()).hasSize(4);
		assertThat(config.getTableMetadataCodes().get(0)).isEqualTo(zeSchema.code() + "_" + Schemas.TITLE_CODE);
		assertThat(config.getTableMetadataCodes().get(1)).isEqualTo(zeSchema.booleanMetadata().getCode());
		assertThat(config.getTableMetadataCodes().get(2)).isEqualTo(zeSchema.dateMetadata().getCode());
		assertThat(config.getTableMetadataCodes().get(3)).isEqualTo(zeSchema.stringMetadata().getCode());

		SchemaDisplayConfig customConfig = manager.getSchema(zeCollection, zeCustomSchema.code());
		assertThat(customConfig.getTableMetadataCodes()).hasSize(4);
		assertThat(customConfig.getTableMetadataCodes().get(0)).isEqualTo(zeCustomSchema.code() + "_" + Schemas.TITLE_CODE);
		assertThat(customConfig.getTableMetadataCodes().get(1)).isEqualTo(zeCustomSchema.booleanMetadata().getCode());
		assertThat(customConfig.getTableMetadataCodes().get(2)).isEqualTo(zeCustomSchema.dateMetadata().getCode());
		assertThat(customConfig.getTableMetadataCodes().get(3)).isEqualTo(zeCustomSchema.stringMetadata().getCode());
	}

	@Test
	public void givenSchemaSavedWhenGetMetadataValueThenCorrect()
			throws Exception {
		List<Metadata> metadatas = Arrays.asList(zeSchema.booleanMetadata(), zeSchema.dateMetadata(), zeSchema.stringMetadata());
		List<FormMetadataVO> formMetadataVOs = this.getFormVO(metadatas);
		presenter.setSchemaCode(zeSchema.code());

		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig config = manager.getSchema(zeCollection, zeSchema.code());
		config = config.withTableMetadataCodes(getCodeFromMetadata(metadatas));
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

		config = config.withTableMetadataCodes(getCodeFromVO(formMetadataVOs));
		manager.saveSchema(config);

		List<FormMetadataVO> result = presenter.getValueMetadatas();
		assertThat(result).hasSize(14);
		assertThat(result).containsExactlyElementsOf(formMetadataVOs);
		assertThat(result.get(0).getCode()).isEqualTo(formMetadataVOs.get(0).getCode());
		assertThat(result.get(1).getCode()).isEqualTo(formMetadataVOs.get(1).getCode());
		assertThat(result.get(2).getCode()).isEqualTo(formMetadataVOs.get(2).getCode());
		assertThat(result.get(3).getCode()).isEqualTo(formMetadataVOs.get(3).getCode());
		assertThat(result.get(4).getCode()).isEqualTo(formMetadataVOs.get(4).getCode());
		assertThat(result.get(5).getCode()).isEqualTo(formMetadataVOs.get(5).getCode());
		assertThat(result.get(6).getCode()).isEqualTo(formMetadataVOs.get(6).getCode());
		assertThat(result.get(7).getCode()).isEqualTo(formMetadataVOs.get(7).getCode());
		assertThat(result.get(8).getCode()).isEqualTo(formMetadataVOs.get(8).getCode());
		assertThat(result.get(9).getCode()).isEqualTo(formMetadataVOs.get(9).getCode());
		assertThat(result.get(10).getCode()).isEqualTo(formMetadataVOs.get(10).getCode());
		assertThat(result.get(11).getCode()).isEqualTo(formMetadataVOs.get(11).getCode());

		formMetadataVOs = presenter.getMetadatas();
		Collections.shuffle(formMetadataVOs);
		config = config.withTableMetadataCodes(getCodeFromVO(formMetadataVOs));
		manager.saveSchema(config);

		result = presenter.getValueMetadatas();
		assertThat(result).hasSize(14);
		assertThat(result).containsExactlyElementsOf(formMetadataVOs);
		assertThat(result.get(0).getCode()).isEqualTo(formMetadataVOs.get(0).getCode());
		assertThat(result.get(1).getCode()).isEqualTo(formMetadataVOs.get(1).getCode());
		assertThat(result.get(2).getCode()).isEqualTo(formMetadataVOs.get(2).getCode());
		assertThat(result.get(3).getCode()).isEqualTo(formMetadataVOs.get(3).getCode());
		assertThat(result.get(4).getCode()).isEqualTo(formMetadataVOs.get(4).getCode());
		assertThat(result.get(5).getCode()).isEqualTo(formMetadataVOs.get(5).getCode());
		assertThat(result.get(6).getCode()).isEqualTo(formMetadataVOs.get(6).getCode());
		assertThat(result.get(7).getCode()).isEqualTo(formMetadataVOs.get(7).getCode());
		assertThat(result.get(8).getCode()).isEqualTo(formMetadataVOs.get(8).getCode());
		assertThat(result.get(9).getCode()).isEqualTo(formMetadataVOs.get(9).getCode());
		assertThat(result.get(10).getCode()).isEqualTo(formMetadataVOs.get(10).getCode());
		assertThat(result.get(11).getCode()).isEqualTo(formMetadataVOs.get(11).getCode());
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
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			formMetadataVOs.add(builder.build(metadata, getAppLayerFactory().getMetadataSchemasDisplayManager(),
					setup.zeCustomSchemaTypeCode()));
		}

		return formMetadataVOs;
	}
}
