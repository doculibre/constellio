package com.constellio.app.ui.pages.management.schemas.schema;

import java.util.Map;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class AddEditSchemaPresenter extends SingleSchemaBasePresenter<AddEditSchemaView> {

	private Map<String, String> parameters;

	public AddEditSchemaPresenter(AddEditSchemaView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public MetadataVODataProvider getDataProvider() {
		String schemaCode = getSchemaCode();
		return new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection, schemaCode);
	}

	public FormMetadataSchemaVO getSchemaVO() {
		String schemaCode = getSchemaCode();
		FormMetadataSchemaVO schemaVO = null;
		if (!schemaCode.isEmpty()) {
			MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchema schema = manager.getSchemaTypes(collection).getSchema(schemaCode);
			schemaVO = new MetadataSchemaToFormVOBuilder().build(schema, view.getSessionContext());
		}
		return schemaVO;
	}

	public void saveButtonClicked(FormMetadataSchemaVO schemaVO, boolean editMode) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);

		String code;
		if (!editMode) {
			code = "USR" + schemaVO.getLocalCode();
			types.getSchemaType(parameters.get("schemaTypeCode")).createCustomSchema(code, schemaVO.getLabels());
		} else {
			code = schemaVO.getCode();
			MetadataSchemaBuilder builder = types.getSchema(code);
			Map<Language, String> newLabels = MetadataSchemaTypeBuilder.configureLabels(schemaVO.getLabels());
			builder.setLabels(newLabels);
		}

		try {
			schemasManager.saveUpdateSchemaTypes(types);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigateTo().listSchema(params);
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigateTo().listSchema(params);
	}
}
