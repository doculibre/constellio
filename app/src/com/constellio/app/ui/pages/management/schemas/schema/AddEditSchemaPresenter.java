package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import org.apache.commons.lang.StringUtils;

import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class AddEditSchemaPresenter extends SingleSchemaBasePresenter<AddEditSchemaView> {

	private Map<String, String> parameters;
	
	private String initialSchemaCode;
	
	private FormMetadataSchemaVO schemaVO;
	
	private boolean editMode;
	
	public AddEditSchemaPresenter(AddEditSchemaView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
		
		String schemaCode = params.get("schemaCode");
		editMode = StringUtils.isNotBlank(schemaCode);
		
		MetadataSchemaTypes types = types();
		MetadataSchema schema;

		SessionContext sessionContext = view.getSessionContext();
		String schemaTypeCode = params.get("schemaTypeCode");
		if (editMode) {
			schema = types.getSchema(schemaCode);
		} else {
			schema = types.getSchema(schemaTypeCode + "_default");
		}
		initialSchemaCode = schema.getCode();

		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypeDisplayConfig schemaTypeDisplayConfig = metadataSchemasDisplayManager.getType(collection, schemaTypeCode);

		FormMetadataSchemaVO schemaVO = new MetadataSchemaToFormVOBuilder().build(schema, sessionContext, schemaTypeDisplayConfig);
		setSchemaVO(schemaVO);
	}

	public MetadataVODataProvider getDataProvider() {
		String schemaCode = schemaVO.getCode();
		return new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection, schemaCode);
	}

	public void saveButtonClicked() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);

		boolean modifyExistingSchemaCode;

		String schemaTypeCode = parameters.get("schemaTypeCode");

		String code;
		if (isCodeEditable()) {
			String localCode = schemaVO.getLocalCode();
			if (StringUtils.startsWithAny(localCode,new String[]{"0","1","2","3","4","5","6","7","8","9"})) {
				code = null;
				modifyExistingSchemaCode = false;
				view.showErrorMessage($("AddEditSchemaView.schemaCodeStartsWithNumber"));
			} else if(StringUtils.contains(localCode," ")) {
				code = null;
				modifyExistingSchemaCode = false;
				view.showErrorMessage($("AddEditSchemaView.schemaCodeContainsSpace"));
			} else {
				if (isEditMode()) {
					code = schemaTypeCode + "_" + localCode;
					modifyExistingSchemaCode = !code.equals(initialSchemaCode);
				} else {
					modifyExistingSchemaCode = false;
					code = "USR" + localCode;
					types.getSchemaType(schemaTypeCode).createCustomSchema(code, schemaVO.getLabels());
				}
			}
		} else {
			code = schemaVO.getCode();
			modifyExistingSchemaCode = false;
		}

		if (code != null) {
			if (editMode){
				MetadataSchemaBuilder builder = types.getSchema(initialSchemaCode);
				Map<Language, String> newLabels = MetadataSchemaTypeBuilder.configureLabels(schemaVO.getLabels());
				builder.setLabels(newLabels);
			}
			try {
				schemasManager.saveUpdateSchemaTypes(types);
			} catch (OptimisticLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
			
			if (modifyExistingSchemaCode) {
				AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
				appSchemasServices.modifySchemaCode(collection, initialSchemaCode, code);
			}
			
			String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
			view.navigate().to().listSchema(params);
		}

		if (schemaVO.getAdvancedSearch() != null) {
			boolean advancedSearch = schemaVO.getAdvancedSearch();
			SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			SchemaTypeDisplayConfig schemaTypeDisplayConfig = metadataSchemasDisplayManager.getType(collection, schemaTypeCode).withAdvancedSearchStatus(advancedSearch);
			metadataSchemasDisplayManager.saveType(schemaTypeDisplayConfig);
		}
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}

	public boolean isEditMode() {
		return editMode;
	}

	public boolean isCodeEditable() {
		String schemaLocalCode = schemaVO.getLocalCode();
		return !isEditMode() || !"default".equals(schemaLocalCode);
	}

	// Test methods	
	void setSchemaVO(FormMetadataSchemaVO schemaVO) {
		this.schemaVO = schemaVO;
		view.setSchemaVO(schemaVO);
	}
	
}
