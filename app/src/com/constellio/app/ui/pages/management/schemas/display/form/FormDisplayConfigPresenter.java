package com.constellio.app.ui.pages.management.schemas.display.form;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemaDisplayUtils;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class FormDisplayConfigPresenter extends SingleSchemaBasePresenter<FormDisplayConfigView> {

	private Map<String, String> parameters;

	public FormDisplayConfigPresenter(FormDisplayConfigView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public List<FormMetadataVO> getMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataList list = SchemaDisplayUtils.getAvailableMetadatasInSchemaForm(schemasManager.getSchemaTypes(
				collection).getSchema(getSchemaCode()));
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder(view.getSessionContext());
		for (Metadata metadata : list) {
			//if (this.isAllowedMetadata(metadata)) {
			formMetadataVOs.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode"), view.getSessionContext()));
			//}
		}

		return formMetadataVOs;
	}

	public List<FormMetadataVO> getValueMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		SchemasDisplayManager displayManager = schemasDisplayManager();
		List<String> codeList = displayManager.getSchema(collection, getSchemaCode()).getFormMetadataCodes();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder(view.getSessionContext());
		for (String metadataCode : codeList) {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			formMetadataVOs.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode"), view.getSessionContext()));
		}

		return formMetadataVOs;
	}

	//	private boolean isAllowedMetadata(Metadata metadata) {
	//		List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
	//				Schemas.PARENT_PATH, Schemas.AUTHORIZATIONS, Schemas.REMOVED_AUTHORIZATIONS, Schemas.INHERITED_AUTHORIZATIONS,
	//				Schemas.AURHORIZATIONS_TARGETTING_RECORD, Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
	//				Schemas.FOLLOWERS, Schemas.LOGICALLY_DELETED_STATUS);
	//
	//		List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);
	//
	//		return !localCodes.contains(metadata.getLocalCode());
	//	}

	public void saveButtonClicked(List<FormMetadataVO> schemaVOs) {
		SchemasDisplayManager manager = schemasDisplayManager();
		SchemaDisplayConfig config = schemasDisplayManager().getSchema(collection, getSchemaCode());

		List<String> metadataCode = new ArrayList<>();
		for (FormMetadataVO formMetadataVO : schemaVOs) {
			metadataCode.add(formMetadataVO.getCode());
		}

		config = config.withFormMetadataCodes(metadataCode);

		try {
			manager.saveSchema(config);
			String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
			view.navigate().to().listSchema(params);
		} catch (ValidationRuntimeException e) {
			view.showErrorMessage($(e.getValidationErrors()));
		}

	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}
}
