package com.constellio.app.ui.pages.management.schemas.schema;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditSchemaPresenter extends SingleSchemaBasePresenter<AddEditSchemaView> {

	private Map<String, String> parameters;

	private String initialSchemaCode;

	private FormMetadataSchemaVO schemaVO;

	private boolean editMode;

	private String schemaTypeCode;

	private String initialCode;

	boolean initialIsCodeEditable;

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

		String codeParam = params.get("code");

		MetadataSchemaTypes types = types();
		MetadataSchema schema;
		String code;
		SessionContext sessionContext = view.getSessionContext();
		schemaTypeCode = params.get("schemaTypeCode");
		if (editMode) {
			schema = types.getSchema(schemaCode);
			code = SchemaUtils.underscoreSplitWithCache(schema.getCode())[1];
			if (codeParam != null) {
				code = codeParam;
			}
		} else {
			schema = types.getSchema(schemaTypeCode + "_default");
			code = "USR";

			if (codeParam != null) {
				code = codeParam;
			}
		}

		initialCode = code;
		initialSchemaCode = schema.getCode();

		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypeDisplayConfig schemaTypeDisplayConfig = metadataSchemasDisplayManager.getType(collection, schemaTypeCode);

		FormMetadataSchemaVO schemaVO = new MetadataSchemaToFormVOBuilder().build(schema, code, sessionContext, schemaTypeDisplayConfig, editMode);
		setSchemaVO(schemaVO);
		initialIsCodeEditable = isCodeEditable();
	}

	public void saveButtonClicked() throws ValidationException {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);

		boolean modifyExistingSchemaCode;

		String schemaTypeCode = parameters.get("schemaTypeCode");

		ValidationErrors validationErrors = new ValidationErrors();
		if (initialIsCodeEditable) {
			if (!schemaTypeCode.toLowerCase().equals("document")
				&& !schemaTypeCode.toLowerCase().equals("folder") || initialCode.startsWith("USR") || !editMode) {
				if (!schemaVO.getLocalCode().startsWith("USR")) {
					validationErrors.add(AddEditSchemaPresenter.class, "startWithUSR");
				}

				if (schemaVO.getLocalCode().length() <= 3) {
					validationErrors.add(AddEditSchemaPresenter.class, "codeToShort");
				}
			} else {
				if (schemaVO.getLocalCode().length() <= 0) {
					validationErrors.add(AddEditSchemaPresenter.class, "codeToShort");
				}
			}
		}

		validationErrors.throwIfNonEmpty();

		String code;
		if (isCodeEditable()) {
			String localCode = schemaVO.getLocalCode();
			if (StringUtils.startsWithAny(localCode.replace("USR", ""), new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"})) {
				code = null;
				modifyExistingSchemaCode = false;
				view.showErrorMessage($("AddEditSchemaView.schemaCodeStartsWithNumber"));
			} else if (StringUtils.contains(localCode, " ")) {
				code = null;
				modifyExistingSchemaCode = false;
				view.showErrorMessage($("AddEditSchemaView.schemaCodeContainsSpace"));
			} else {
				if (isEditMode()) {
					code = schemaTypeCode + "_" + localCode;
					modifyExistingSchemaCode = !code.equals(initialSchemaCode);
				} else {
					modifyExistingSchemaCode = false;
					code = localCode;
					types.getSchemaType(schemaTypeCode).createCustomSchema(code, schemaVO.getLabels());
				}
			}
		} else {
			code = schemaVO.getCode();
			modifyExistingSchemaCode = false;
		}

		if (code != null) {
			if (editMode) {
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
				try {
					if (appSchemasServices.modifySchemaCode(collection, initialSchemaCode, code)) {
						view.showMessage($("AddEditSchemaView.codechangeRequireRecordModification"));
					}
				} catch (RecordServicesException e) {
					if (e instanceof RecordServicesException.ValidationException) {
						MessageUtils.getCannotDeleteWindow(((RecordServicesException.ValidationException) e).getErrors()).openWindow();
					}
				}
			}

			String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
			view.navigate().to().listSchema(params);
		}

		if (schemaVO.getSimpleSearch() != null) {
			boolean simpleSearch = schemaVO.getSimpleSearch();
			SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			SchemaTypeDisplayConfig schemaTypeDisplayConfig = metadataSchemasDisplayManager.getType(collection, schemaTypeCode).withSimpleSearchStatus(simpleSearch);
			metadataSchemasDisplayManager.saveType(schemaTypeDisplayConfig);
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
		return schemaVO.getLocalCode().startsWith("USR")
			   || schemaTypeCode.toLowerCase().equals("document") && !initialSchemaCode.endsWith("default")
			   || schemaTypeCode.toLowerCase().equals("folder") && !initialSchemaCode.endsWith("default");
	}

	// Test methods	
	void setSchemaVO(FormMetadataSchemaVO schemaVO) {
		this.schemaVO = schemaVO;
		view.setSchemaVO(schemaVO);
	}

}
