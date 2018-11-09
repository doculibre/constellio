package com.constellio.app.ui.pages.management.schemas;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.framework.data.writter.SchemaTypeExcelReportWriter;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


public class ListSchemaTypePresenter extends SingleSchemaBasePresenter<ListSchemaTypeView> {

	private IOServices ioServices;
	MetadataSchemasManager schemaManager;

	public ListSchemaTypePresenter(ListSchemaTypeView view) {
		super(view);
		ioServices = view.getConstellioFactories().getIoServicesFactory().newIOServices();
		schemaManager = modelLayerFactory.getMetadataSchemasManager();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public SchemaTypeVODataProvider getDataProvider(final String tabId) {
		return new SchemaTypeVODataProvider(new MetadataSchemaTypeToVOBuilder(), appLayerFactory, collection) {
			@Override
			protected boolean isAccepted(MetadataSchemaType type) {
				String schemaTypeDisplayGroup = appLayerFactory.getExtensions().forCollection(collection).getSchemaTypeDisplayGroup(type);
				return tabId.equals(schemaTypeDisplayGroup);
			}
		};
	}

	public void editButtonClicked(MetadataSchemaTypeVO schemaTypeVO) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaTypeVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, paramsMap);
		view.navigate().to().listSchema(params);
	}

	public void listGroupButtonClicked(MetadataSchemaTypeVO schemaTypeVO) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaTypeVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.LIST_ONGLET, paramsMap);
		view.navigate().to().listTabDisplayForm(params);
	}

	public void generateExcelWithMetadataInfo(MetadataSchemaTypeVO schemaTypeVO)
 	{
		String titre = schemaTypeVO.getLabel(Language
				.withCode(view.getSessionContext().getCurrentLocale().getLanguage()));
		SchemaTypeExcelReportWriter schemaTypeExcelGenerator = new SchemaTypeExcelReportWriter(schemaManager.getSchemaTypes(collection).getSchemaType(schemaTypeVO.getCode()), appLayerFactory, getCurrentLocale());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		schemaTypeExcelGenerator.write(byteArrayOutputStream);


		view.startDownload(titre +".xls", new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "xls");
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	public void reportButtonClicked(MetadataSchemaTypeVO schemaVO) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaVO.getCode());
		String params = ParamUtils.addParams(NavigatorConfigurationService.REPORT_DISPLAY_FORM, paramsMap);
		view.navigate().to().reportDisplayForm(params);
	}

	public boolean isSearchableSchema(String schemaCode) {
		SchemaTypeDisplayConfig config = schemasDisplayManager().getType(collection, schemaCode);
		if (config.isAdvancedSearch()) {
			return true;
		}
		return false;
	}

}
