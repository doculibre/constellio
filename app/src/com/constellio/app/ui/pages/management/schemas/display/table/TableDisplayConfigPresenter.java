package com.constellio.app.ui.pages.management.schemas.display.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

public class TableDisplayConfigPresenter extends SingleSchemaBasePresenter<TableDisplayConfigView> {

	private Map<String, String> parameters;

	public TableDisplayConfigPresenter(TableDisplayConfigView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public MetadataVODataProvider getDataProvider() {
		return new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection, getSchemaCode());
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public List<FormMetadataVO> getMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataList list = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadatas();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
		for (Metadata metadata : list) {
			FormMetadataVO metadataVO = builder.build(metadata, displayManager, parameters.get("schemaTypeCode"));
			if (this.isAllowedMetadata(metadataVO)) {
				formMetadataVOs.add(metadataVO);
			}
		}

		return formMetadataVOs;
	}

	public List<FormMetadataVO> getValueMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		SchemasDisplayManager displayManager = schemasDisplayManager();
		List<String> codeList = displayManager.getSchema(collection, getSchemaCode()).getTableMetadataCodes();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
		for (String metadataCode : codeList) {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			formMetadataVOs.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode")));
		}

		return formMetadataVOs;
	}

	private boolean isAllowedMetadata(FormMetadataVO metadataVO) {
		boolean result;
		List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
				Schemas.PARENT_PATH, Schemas.AUTHORIZATIONS, Schemas.REMOVED_AUTHORIZATIONS, Schemas.INHERITED_AUTHORIZATIONS,
				Schemas.ALL_AUTHORIZATIONS, Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
				Schemas.FOLLOWERS, Schemas.LOGICALLY_DELETED_STATUS, Schemas.SHARE_DENY_TOKENS, Schemas.SHARE_TOKENS,
				Schemas.DENY_TOKENS, Schemas.SEARCHABLE);

		List<MetadataValueType> restrictedType = Arrays.asList(MetadataValueType.STRUCTURE, MetadataValueType.CONTENT);

		List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);

		result = !restrictedType.contains(metadataVO.getValueType()) && !localCodes.contains(metadataVO.getLocalcode());

		return result;
	}

	public void saveButtonClicked(List<FormMetadataVO> formMetadataVOs) {
		SchemasDisplayManager manager = schemasDisplayManager();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		String schemaCode = getSchemaCode();
		if (schemaCode.contains("default")) {
			SchemaDisplayConfig config = schemasDisplayManager().getSchema(collection, schemaCode);

			List<String> metadataCodes = new ArrayList<>();
			for (FormMetadataVO formMetadataVO : formMetadataVOs) {
				metadataCodes.add(formMetadataVO.getCode());
			}

			final MetadataSchema schema = schemasManager.getSchemaTypes(collection).getSchema(schemaCode);
			metadataCodes.add(0, schema.getMetadata(Schemas.TITLE.getCode()).getCode());
			if (schema.hasMetadataWithCode(Schemas.CODE.getCode())) {
				metadataCodes.add(0, schema.getMetadata(Schemas.CODE.getCode()).getCode());
			}

			config = config.withTableMetadataCodes(metadataCodes);
			manager.saveSchema(config);

			String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
			view.navigate().to().listSchema(params);
		} else {
			throw new RuntimeException("Configuration only allowed for default schema");
		}
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}
}
