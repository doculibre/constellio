package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.metadata.MetadataDeletionException;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CalculatedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataReference;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_ExtractedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_FacetMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_InheritedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_PopulatedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_SystemMetadata;
import com.constellio.app.services.metadata.MetadataDeletionService;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class AddEditSchemaMetadataPresenter extends SingleSchemaBasePresenter<AddEditSchemaMetadataView> {
	private transient MetadataDeletionService metadataDeletionService;

	public AddEditSchemaMetadataPresenter(AddEditSchemaMetadataView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	private Map<String, String> parameters;

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
		if (schemaCode != null) {
			MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchema schema = manager.getSchemaTypes(collection).getSchema(schemaCode);
			schemaVO = new MetadataSchemaToFormVOBuilder().build(schema);
		}

		return schemaVO;
	}

	public void addButtonClicked() {
		parameters.put("schemaCode", getSchemaCode());
		parameters.put("metadataCode", "");
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
		view.navigate().to().addMetadata(params);
	}

	public void editButtonClicked(MetadataVO metadataVO) {
		parameters.put("schemaCode", getSchemaCode());
		parameters.put("metadataCode", metadataVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
		view.navigate().to().editMetadata(params);
	}

	public void backButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}

	public void deleteButtonClicked(MetadataVO entity) {
		try {
			metadataDeletionService().deleteMetadata(entity.getCode());
			String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
			view.navigate().to().listSchemaMetadata(params);

		} catch (MetadataDeletionException e) {
			view.showErrorMessage(getAppropriateMessage(e));
		}
	}

	private String getAppropriateMessage(MetadataDeletionException e) {
		if (e instanceof MetadataDeletionException_SystemMetadata) {
			return $("MetadataDeletionException_SystemMetadata");
		} else if (e instanceof MetadataDeletionException_PopulatedMetadata) {
			return $("MetadataDeletionException_PopulatedMetadata");
		} else if (e instanceof MetadataDeletionException_CopiedMetadataSource) {
			return $("MetadataDeletionException_CopiedMetadataSource");
		} else if (e instanceof MetadataDeletionException_CopiedMetadataReference) {
			return $("MetadataDeletionException_CopiedMetadataReference");
		} else if (e instanceof MetadataDeletionException_CalculatedMetadataSource) {
			return $("MetadataDeletionException_CalculatedMetadataSource");
		} else if (e instanceof MetadataDeletionException_ExtractedMetadataSource) {
			return $("MetadataDeletionException_ExtractedMetadataSource");
		} else if (e instanceof MetadataDeletionException_InheritedMetadata) {
			return $("MetadataDeletionException_InheritedMetadata");
		} else if (e instanceof MetadataDeletionException_FacetMetadata) {
			return $("MetadataDeletionException_FacetMetadata");
		}
		return null;
	}

	private MetadataDeletionService metadataDeletionService() {
		if (metadataDeletionService == null) {
			this.metadataDeletionService = new MetadataDeletionService(appLayerFactory, collection);
		}
		return metadataDeletionService;
	}

	public boolean isMetadataDeletable(MetadataVO entity) {
		return metadataDeletionService().isMetadataDeletable(entity.getCode());
	}
}
