package com.constellio.app.ui.pages.management.schemas.type;

import com.constellio.app.api.extensions.params.ListSchemaExtraCommandParams;
import com.constellio.app.api.extensions.params.ListSchemaExtraCommandReturnParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.data.SchemaVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListSchemaPresenter extends SingleSchemaBasePresenter<ListSchemaView> {

	private Map<String, String> parameters;
	private String schemaTypeCode;
	AppLayerCollectionExtensions collectionExtensions = appLayerFactory.getExtensions().forCollection(collection);

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

	public void formOrderButtonClicked(MetadataSchemaVO schemaVO) {
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

	ValidationErrors isDeletePossible(String schemaCode) {
		AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
		return appSchemasServices.isSchemaDeletable(collection, schemaCode);
	}

	public void deleteButtonClicked(String schemaCode) {
		if (isDeletePossible(schemaCode) == null) {
			AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
			if (collectionExtensions.lockedRecords.contains($("ListSchemaTypeView.schemaCode"), schemaCode.split("_")[1])) {
				view.showMessage($("ListSchemaTypeView.message"));
			} else {
				appSchemasServices.deleteSchemaCode(collection, schemaCode);
				view.navigate().to().listSchema(ParamUtils.addParams("", parameters));
			}
		} else {
			AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
			User user = getCurrentUser(ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory());
			String cannotDeleteMessage = $(isDeletePossible(schemaCode).getValidationErrors().get(0));
			if (isDeletePossible(schemaCode).getValidationErrors().get(0).getValidatorErrorCode()
				== "existingRecordsWithSchema") {
				boolean areAllRecordsVisible = appSchemasServices.areAllRecordsVisible(collection, schemaCode, user);
				CannotDeleteWindow cannotDeleteWindow = new CannotDeleteWindow(cannotDeleteMessage);
				cannotDeleteWindow
						.buildWindowConponentsWithTable(appSchemasServices.getVisibleRecords(collection, schemaCode, user, 25),
								areAllRecordsVisible);
				cannotDeleteWindow.openWindow();
			} else {
				CannotDeleteWindow cannotDeleteWindow = new CannotDeleteWindow(cannotDeleteMessage);
				cannotDeleteWindow.buildWindowConponentsWithoutTable();
				cannotDeleteWindow.openWindow();
			}
		}
	}

	private User getCurrentUser(ModelLayerFactory modelLayerFactory) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();

		return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
	}

	public List<ListSchemaExtraCommandReturnParams> getExtensionMenuBar(MetadataSchemaVO metadataSchemaVO) {
		return appLayerFactory.getExtensions().forCollection(collection)
				.getListSchemaExtraCommandExtensions(new ListSchemaExtraCommandParams(metadataSchemaVO, (BaseViewImpl) view));
	}

	public boolean isDeleteButtonVisible(String schemaCode) {
		ValidationErrors validationErrors = isDeletePossible(schemaCode);
		return validationErrors == null || validationErrors.isEmpty();
	}

	public void closeAllWindows() {
		view.closeAllWindows();
	}

}
