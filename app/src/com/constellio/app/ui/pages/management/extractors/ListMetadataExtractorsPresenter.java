package com.constellio.app.ui.pages.management.extractors;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ListMetadataExtractorsPresenter extends BasePresenter<ListMetadataExtractorsView> {

	private MetadataToVOBuilder metadataToVOBuilder;

	private MetadataSchemaToVOBuilder metadataSchemaToVOBuilder;

	public ListMetadataExtractorsPresenter(ListMetadataExtractorsView view) {
		super(view);

		metadataToVOBuilder = new MetadataToVOBuilder();
		metadataSchemaToVOBuilder = new MetadataSchemaToVOBuilder();

		SessionContext sessionContext = view.getSessionContext();

		MetadataSchemaTypes types = types();
		List<MetadataExtractorVO> metadataExtractorVOs = getMetadataExtractorVOs(sessionContext, types);
		view.setMetadataExtractorVOs(metadataExtractorVOs);
	}

	List<MetadataExtractorVO> getMetadataExtractorVOs(SessionContext sessionContext, MetadataSchemaTypes types) {
		List<MetadataExtractorVO> metadataExtractorVOs = new ArrayList<>();
		for (Metadata metadata : getPopulatedMetadatas()) {
			String schemaCode = metadata.getSchemaCode();
			MetadataSchema schema = types.getSchema(schemaCode);
			MetadataSchemaVO schemaVO = metadataSchemaToVOBuilder.build(schema, VIEW_MODE.TABLE, sessionContext);
			MetadataVO metadataVO = metadataToVOBuilder.build(metadata, schemaVO, sessionContext);
			MetadataPopulateConfigs metadataPopulateConfigs = metadata.getPopulateConfigs();
			metadataExtractorVOs.add(new MetadataExtractorVO(metadataVO, metadataPopulateConfigs));
		}
		return metadataExtractorVOs;
	}

	private List<Metadata> getPopulatedMetadatas() {
		List<Metadata> populatedMetadatas = new ArrayList<>();
		for (MetadataSchemaType type : types().getSchemaTypes()) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getPopulateConfigs().isConfigured()) {
						if (!metadata.inheritDefaultSchema() ||
								!metadata.getPopulateConfigs().equals(metadata.getInheritance().getPopulateConfigs())) {
							populatedMetadatas.add(metadata);
						}
					}
				}
			}
		}

		return populatedMetadatas;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATAEXTRACTOR).globally();
	}

	void addButtonClicked() {
		view.navigate().to().addMetadataExtractor();
	}

	void editButtonClicked(MetadataExtractorVO metadataExtractorVO) {
		MetadataVO metadataVO = metadataExtractorVO.getMetadataVO();
		view.navigate().to().editMetadataExtractor(metadataVO.getCode());
	}

	public void deleteButtonClicked(MetadataExtractorVO metadataExtractorVO) {
		MetadataVO metadataVO = metadataExtractorVO.getMetadataVO();
		final String metadataCode = metadataVO.getCode();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = types.getMetadata(metadataCode);
				metadataBuilder.getPopulateConfigsBuilder().getStyles().clear();
				metadataBuilder.getPopulateConfigsBuilder().getProperties().clear();
				metadataBuilder.getPopulateConfigsBuilder().getRegexes().clear();
			}
		});
		view.removeMetadataExtractorVO(metadataExtractorVO);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

}
