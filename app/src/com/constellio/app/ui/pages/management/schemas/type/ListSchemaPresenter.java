package com.constellio.app.ui.pages.management.schemas.type;

import java.util.Map;

import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.data.SchemaVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class ListSchemaPresenter extends SingleSchemaBasePresenter<ListSchemaView> {

	private Map<String, String> parameters;
	private String schemaTypeCode;

	public ListSchemaPresenter(ListSchemaView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public SchemaVODataProvider getDataProvider() {
		return new SchemaVODataProvider(new MetadataSchemaToVOBuilder(), modelLayerFactory, collection, schemaTypeCode,
				view.getSessionContext());
	}

	public void setSchemaTypeCode(String schemaTypeCode) {
		this.schemaTypeCode = schemaTypeCode;
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public void editButtonClicked(MetadataSchemaVO schemaVO) {
		parameters.put("schemaCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA, parameters);
		view.navigate().to().editSchema(params);
	}

	public void editMetadataButtonClicked(MetadataSchemaVO schemaVO) {
		parameters.put("schemaCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA, parameters);
		view.navigate().to().listSchemaMetadata(params);
	}

	public void addButtonClicked() {
		parameters.put("schemaCode", "");
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA, parameters);
		view.navigate().to().addSchema(params);
	}

	public void formButtonClicked(MetadataSchemaVO schemaVO) {
		parameters.put("schemaCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.EDIT_DISPLAY_FORM, parameters);
		view.navigate().to().editDisplayForm(params);
	}

	public void orderButtonClicked(MetadataSchemaVO schemaVO) {
		parameters.put("schemaCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.FORM_DISPLAY_FORM, parameters);
		view.navigate().to().formDisplayForm(params);
	}

	public void searchButtonClicked(MetadataSchemaVO schemaVO) {
		parameters.put("schemaCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.SEARCH_DISPLAY_FORM, parameters);
		view.navigate().to().searchDisplayForm(params);
	}

	public void tableButtonClicked() {
		parameters.put("schemaCode", schemaTypeCode + "_default");
		String params = ParamUtils.addParams(NavigatorConfigurationService.TABLE_DISPLAY_FORM, parameters);
		view.navigate().to().tableDisplayForm(params);
	}

	public void backButtonClicked() {
		view.navigate().to().listSchemaTypes();
	}
	
	boolean isDeletePossible(String schemaCode) {
		AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
		return appSchemasServices.isSchemaDeletable(collection, schemaCode);
	}

	public void deleteButtonClicked(String schemaCode) {
		if (isDeletePossible(schemaCode)) {
			AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
			appSchemasServices.deleteSchemaCode(collection, schemaCode);
			view.navigate().to().listSchema(ParamUtils.addParams("", parameters));
		}
	}

	public boolean isDeleteButtonVisible(String schemaCode) {
		return isDeletePossible(schemaCode);
	}
	
}
