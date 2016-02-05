package com.constellio.app.ui.pages.management.schemas;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

import java.util.HashMap;
import java.util.Map;

public class ListSchemaTypePresenter extends SingleSchemaBasePresenter<ListSchemaTypeView> {

	public ListSchemaTypePresenter(ListSchemaTypeView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public SchemaTypeVODataProvider getDataProvider() {
		return new SchemaTypeVODataProvider(new MetadataSchemaTypeToVOBuilder(), appLayerFactory, collection);
	}

	public void editButtonClicked(MetadataSchemaTypeVO schemaTypeVO) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaTypeVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, paramsMap);
		view.navigateTo().listSchema(params);
	}

	public void listGroupButtonClicked(MetadataSchemaTypeVO schemaTypeVO) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaTypeVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.LIST_ONGLET, paramsMap);
		view.navigateTo().listTabDisplayForm(params);
	}

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

	public void reportButtonClicked(MetadataSchemaTypeVO schemaVO) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.REPORT_DISPLAY_FORM, paramsMap);
		view.navigateTo().reportDisplayForm(params);
	}

	public boolean isSearchableSchema(String schemaCode) {
		SchemaTypeDisplayConfig config = schemasDisplayManager().getType(collection, schemaCode);
		if (config.isAdvancedSearch()) {
			return true;
		}
		return false;
	}
}
