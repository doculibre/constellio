package com.constellio.app.ui.pages.management.schemas.metadata;

import com.constellio.app.api.extensions.params.GetAvailableExtraMetadataAttributesParam;
import com.constellio.app.api.extensions.params.IsBuiltInMetadataAttributeModifiableParam;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.extensions.params.RMSchemaTypesPageExtensionExclusionByPropertyParams;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.CollectionInfoVO;
import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataAttribute;
import com.constellio.model.entities.schemas.MetadataFilter;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataInSummaryCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.jgoodies.common.base.Strings;
import com.vaadin.ui.UI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataAttribute.REQUIRED;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;

@Slf4j
public class AddEditMetadataPresenter extends SingleSchemaBasePresenter<AddEditMetadataView> {

	private String schemaCode;
	private String schemaTypeCode;
	private Map<String, String> parameters;
	private String metadataCode = "";
	private MetadataSchemaTypes types;
	private Metadata metadata;

	public AddEditMetadataPresenter(AddEditMetadataView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
		this.schemaCode = parameters.get("schemaCode");
		this.schemaTypeCode = parameters.get("schemaTypeCode");
		this.types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public String getParamSchemaCode() {
		return this.schemaCode;
	}

	public FormMetadataSchemaVO getSchemaVO() {
		FormMetadataSchemaVO schemaVO = null;
		MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchema schema = manager.getSchemaTypes(collection).getSchema(schemaCode);
		schemaVO = new MetadataSchemaToFormVOBuilder().build(schema, schema.getLocalCode(),
				view.getSessionContext(), null, true);

		return schemaVO;
	}

	public String getSchemaTypeCode() {
		return schemaTypeCode;
	}

	public void setMetadataCode(String metadataCode) {
		this.metadataCode = metadataCode;
		if (StringUtils.isNotBlank(this.metadataCode)) {
			if (this.metadataCode.split("_").length == 1) {
				this.metadataCode = getSchemaCode() + "_" + metadataCode;
			}
			try {
				this.metadata = types.getMetadata(this.metadataCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isRoleAccessSupportedOnThisMetadata() {
		List<MetadataFilter> metadataThatDontSupportRoleAccessRetValueList = view.getConstellioFactories().getAppLayerFactory()
				.getExtensions().forCollection(view.getCollection()).getMetadataAccessExclusionFilters();

		if (metadata == null) {
			return false;
		}

		boolean isMetadataExcludedByProperty = view.getConstellioFactories().getAppLayerFactory()
				.getExtensions().forCollection(view.getCollection()).isMetadataAccessExclusionByPropertyFilter(new RMSchemaTypesPageExtensionExclusionByPropertyParams(metadata));

		return !isMetadataExcludedByProperty && !metadata.isFilteredByAny(metadataThatDontSupportRoleAccessRetValueList);
	}

	public FormMetadataVO getFormMetadataVO() {
		FormMetadataVO form = null;

		if (metadataCode == null || metadataCode.isEmpty()) {
			return null;
		}

		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		if (types != null) {
			Metadata metadata = types.getMetadata(metadataCode);

			MetadataToFormVOBuilder voBuilder = new MetadataToFormVOBuilder(view.getSessionContext());
			form = voBuilder.build(metadata, displayManager, schemaTypeCode, view.getSessionContext());
			if (isAvailableInSummaryFlagAlwaysTrue(form.getValueType())) {
				form.setAvailableInSummary(true);
			}
		}

		return form;
	}

	public List<RoleVO> getAllCollectionRole() {

		List<RoleVO> result = new ArrayList<>();
		for (Role role : modelLayerFactory.getRolesManager().getAllRoles(view.getCollection())) {
			result.add(new RoleVO(role.getCode(), role.getTitle(), role.getOperationPermissions()));
		}
		return result;
	}

	public FormMetadataVO getParentFormMetadataVO() {
		return getFormMetadataVO().getInheritance();
	}

	public boolean isInherited(String metadataCode) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		try {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			return metadata.inheritDefaultSchema();
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			return false;
		}
	}

	public Metadata getMetadataInheritance(String metadataCode) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		try {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			return metadata.getInheritance();
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			return null;
		}
	}

	public List<String> getMetadataTypesCode() {
		List<String> typeCodes = new ArrayList<>();
		final Map<String, String> typeCodesAndLabels = new HashMap<>();
		for (MetadataSchemaType type : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaTypes()) {
			if (this.isAllowedReferenceType(type)) {
				typeCodes.add(type.getCode());
				Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
				typeCodesAndLabels.put(type.getCode(), type.getLabel(language));
			}
		}
		Collections.sort(typeCodes, new AbstractTextComparator<String>() {
			@Override
			protected String getText(String typeCode) {
				return typeCodesAndLabels.get(typeCode);
			}
		});
		return typeCodes;
	}

	public String getMetadataTypesCaption(String code) {
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(code);
		return type.getLabel(Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage()));
	}

	private boolean isAllowedReferenceType(MetadataSchemaType type) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchemaType collectionType = types.getSchemaType(Collection.SCHEMA_TYPE);
		MetadataSchemaType eventType = types.getSchemaType(Event.SCHEMA_TYPE);

		return !(type.equals(collectionType) || type.equals(eventType));
	}

	public List<String> getAvailableExtraAttributes() {
		return appLayerFactory.getExtensions().forCollection(collection).getAvailableExtraMetadataAttributes(
				new GetAvailableExtraMetadataAttributesParam() {
					@Override
					public Metadata getMetadata() {
						MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
						return StringUtils.isNotBlank(metadataCode) ? types.getMetadata(metadataCode) : null;
					}

					@Override
					public MetadataSchema getSchema() {
						MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
						return types.getSchema(schemaCode);
					}

					@Override
					public MetadataSchemaType getSchemaType() {
						MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
						return types.getSchemaType(schemaTypeCode);
					}
				});
	}

	public boolean isShowUniqueComboBox() {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		LogicalSearchCondition logicalSearchCondition = LogicalSearchQueryOperators.from(types.getSchemaType(schemaTypeCode))
				.returnAll();
		Metadata metadata = types.getMetadata(metadataCode);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setNumberOfRows(0);
		logicalSearchQuery.addFieldFacet(metadata.getDataStoreCode());

		List<FacetValue> response = modelLayerFactory.newSearchServices().query(logicalSearchQuery)
				.getFieldFacetValues(metadata.getDataStoreCode());
		for (FacetValue facetValue : response) {
			if (facetValue.getQuantity() > 1) {
				return false;
			}
		}
		return true;
	}


	public void preSaveButtonClicked(final FormMetadataVO formMetadataVO, final boolean editMode) {

		final String schemaCode = getSchemaCode();
		final MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		final MetadataSchemaTypesBuilder types = schemasManager.modify(collection);
		final String code;
		boolean isSaveButtonClicked = false;
		MetadataBuilder builderDefaultSchema;
		final MetadataBuilder builder;
		final MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder;


		if (!editMode) {
			builder = types.getSchema(schemaCode).create("USR" + formMetadataVO.getLocalcode());
			final MetadataAccessRestrictionBuilder originalMetadataAccessRestrictionBuilder = builder.defineAccessRestrictions();

			builder.setMultivalue(formMetadataVO.isMultivalue());
			builder.setType(formMetadataVO.getValueType());
			builder.setSortable(formMetadataVO.isSortable());
			builder.setSchemaAutocomplete(formMetadataVO.isAutocomplete());
			builder.setSearchable(formMetadataVO.isSearchable());
			builder.setCustomAttributes(formMetadataVO.getCustomAttributes());
			builder.setUniqueValue(formMetadataVO.isUniqueValue());
			builder.setMultiLingual(formMetadataVO.isMultiLingual());

			if (formMetadataVO.getReadAccessRoles() != null) {
				MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(formMetadataVO.getReadAccessRoles(), originalMetadataAccessRestrictionBuilder.getRequiredWriteRoles(),
						originalMetadataAccessRestrictionBuilder.getRequiredModificationRoles(), originalMetadataAccessRestrictionBuilder.getRequiredDeleteRoles());

				metadataAccessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction);
				builder.setAccessRestrictionBuilder(metadataAccessRestrictionBuilder);
			}


			if (formMetadataVO.getValueType().equals(REFERENCE)) {
				MetadataSchemaTypeBuilder refBuilder = types.getSchemaType(formMetadataVO.getReference());
				Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager()
						.getTaxonomyFor(collection, formMetadataVO.getReference());
				if (taxonomy != null) {
					builder.defineTaxonomyRelationshipToType(refBuilder);
				} else {
					builder.defineReferencesTo(refBuilder);
				}
			}
			code = schemaCode + "_" + "USR" + formMetadataVO.getLocalcode();
			saveButtonClicked(formMetadataVO, editMode, schemaCode, schemasManager, types, code, false, false, builder);
		} else {
			builder = types.getSchema(schemaCode).get(formMetadataVO.getCode());
			code = formMetadataVO.getCode();
			final boolean cacheRebuildRequired = !isInherited(code)
												 && !builder.isAvailableInSummary()
												 && formMetadataVO.isAvailableInSummary()
												 && !isAvailableInSummaryFlagAlwaysTrue(metadata.getType());

			if (!isInherited(code)) {

				builder.setCustomAttributes(formMetadataVO.getCustomAttributes());
				final boolean reindexRequired = builder.isSortable() != formMetadataVO.isSortable() ||
												builder.isSearchable() != formMetadataVO.isSearchable();

				final boolean availableInSummaryBuilderValue = !isAvailableInSummaryFlagAlwaysTrue(metadata.getType())
															   && formMetadataVO.isAvailableInSummary();
				builder.setSchemaAutocomplete(formMetadataVO.isAutocomplete());
				builder.setAvailableInSummary(availableInSummaryBuilderValue);

				setReadRoleAccessRestriction(formMetadataVO, builder);

				if (reindexRequired) {
					String confirmDialogMessage = formMetadataVO.getValueType() == REFERENCE ?
												  $("AddEditMetadataPresenter.saveButton.sortableReference") :
												  $("AddEditMetadataPresenter.saveButton.sortable");

					if (builder.isSearchable() != formMetadataVO.isSearchable()) {
						confirmDialogMessage = $("AddEditMetadataPresenter.saveButton.searchable");
					}

					ConfirmDialog.show(UI.getCurrent(), $("AddEditMetadataPresenter.saveButton.title"), confirmDialogMessage,
							$("confirm"), $("cancel"), new ConfirmDialog.Listener() {
								@Override
								public void onClose(ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										builder.setSearchable(formMetadataVO.isSearchable());
										builder.setSortable(formMetadataVO.isSortable());

										saveButtonClicked(formMetadataVO, editMode, schemaCode,
												schemasManager, types, code, reindexRequired, cacheRebuildRequired, builder);
									}
								}
							});

				} else if (cacheRebuildRequired) {
					String confirmDialogMessage = $("AddEditMetadataPresenter.saveButton.cacheRebuildRequired");

					ConfirmDialog.show(UI.getCurrent(), $("AddEditMetadataPresenter.saveButton.cacheRebuildRequiredTitle"), confirmDialogMessage,
							$("confirm"), $("cancel"), new ConfirmDialog.Listener() {
								@Override
								public void onClose(ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										saveButtonClicked(formMetadataVO, editMode, schemaCode,
												schemasManager, types, code, reindexRequired, cacheRebuildRequired, builder);
									}
								}
							});

				} else {
					isSaveButtonClicked = true;
				}
			} else {
				isSaveButtonClicked = true;
			}

			if (isSaveButtonClicked) {
				saveButtonClicked(formMetadataVO, editMode, schemaCode,
						schemasManager, types, code, false, false, builder);
			}
		}
	}

	private void setReadRoleAccessRestriction(FormMetadataVO formMetadataVO, MetadataBuilder builder) {
		MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder;
		if (formMetadataVO.getReadAccessRoles() != null) {
			MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(formMetadataVO.getReadAccessRoles(), new ArrayList<String>(),
					new ArrayList<String>(), new ArrayList<String>());
			metadataAccessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction);
			builder.setAccessRestrictionBuilder(metadataAccessRestrictionBuilder);
		}
	}

	private void saveButtonClicked(FormMetadataVO formMetadataVO, boolean editMode, String schemaCode,
								   MetadataSchemasManager schemasManager, MetadataSchemaTypesBuilder types, String code,
								   boolean reindexRequired, boolean cacheRebuildRequired,
								   MetadataBuilder builder) {
		builder.setDefaultValue(formMetadataVO.getDefaultValue());
		builder.setInputMask(formMetadataVO.getInputMask());
		builder.setEnabled(formMetadataVO.isEnabled());

		for (Entry<String, String> entry : formMetadataVO.getLabels().entrySet()) {
			builder.addLabel(Language.withCode(entry.getKey()), entry.getValue());
		}
		builder.setDefaultRequirement(formMetadataVO.isRequired());
		builder.setDuplicable(formMetadataVO.isDuplicable());

		MetadataBuilder builderDefaultSchema = null;

		if (schemaCode.endsWith("_default")) {
			builderDefaultSchema = builder;
		} else {
			try {
				builderDefaultSchema = types.getSchema(schemaCode).get(formMetadataVO.getCode());
			} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e) {
				// error
			} catch (MetadataSchemaBuilderRuntimeException.InvalidAttribute e) {
				// error take provided schema
			}

			if (builderDefaultSchema != null && builderDefaultSchema.getInheritance() != null) {
				builderDefaultSchema = types.getSchema(schemaCode).getDefaultSchema().get(formMetadataVO.getCode());
			}

			if (builderDefaultSchema == null) {
				builderDefaultSchema = builder;
			}
		}

		builderDefaultSchema.setMultiLingual(formMetadataVO.isMultiLingual());

		if (isInherited(code)) {
			MetadataSchemaBuilder defaultSchemaBuilder = types
					.getSchema(schemaCode.substring(0, schemaCode.lastIndexOf('_')) + "_default");
			String localCode = code.substring(code.lastIndexOf("_") + 1);
			if (defaultSchemaBuilder.hasMetadata(localCode)) {
				defaultSchemaBuilder.getMetadata(localCode).setInputMask(formMetadataVO.getInputMask());
				defaultSchemaBuilder.get(localCode).setSearchable(formMetadataVO.isSearchable());
				defaultSchemaBuilder.get(localCode).setUniqueValue(formMetadataVO.isUniqueValue());
				setReadRoleAccessRestriction(formMetadataVO, defaultSchemaBuilder.get(localCode));
			}
		} else {
			builder.setUniqueValue(formMetadataVO.isUniqueValue());
		}

		try {
			schemasManager.saveUpdateSchemaTypes(types);
		} catch (OptimisticLocking optimistickLocking) {
			// TODO exception gestion
			throw new RuntimeException(optimistickLocking);
		} catch (EssentialMetadataCannotBeDisabled | EssentialMetadataInSummaryCannotBeDisabled e) {
			view.showErrorMessage($("AddEditMetadataView.essentialMetadataCannotBeDisabled"));
			return;
		}

		saveDisplayConfig(formMetadataVO, code, schemasManager, editMode);

		if (reindexRequired) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
			view.showMessage($("AddEditMetadataView.reindexRequired"));

		} else if (cacheRebuildRequired) {
			appLayerFactory.getSystemGlobalConfigsManager().markLocalCachesAsRequiringRebuild();
			view.showMessage($("AddEditMetadataView.cacheRebuildRequired"));

		}

		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_METADATA, parameters);
		view.navigate().to().listSchemaMetadata(params);
	}

	private void saveDisplayConfig(FormMetadataVO formMetadataVO, String code, MetadataSchemasManager schemasManager,
								   boolean editMode) {
		SchemasDisplayManager displayManager = schemasDisplayManager();
		MetadataInputType type = formMetadataVO.getInput();
		MetadataDisplayType displayType = formMetadataVO.getDisplayType();

		if (type == null) {
			type = MetadataInputType.FIELD;
		}
		if (displayType == null || (!MetadataInputType.CHECKBOXES.equals(type) && !MetadataInputType.RADIO_BUTTONS
				.equals(type))) {
			displayType = MetadataDisplayType.VERTICAL;
		}

		MetadataDisplayConfig displayConfig = displayManager.getMetadata(collection, code);
		if (displayConfig == null) {
			displayConfig = new MetadataDisplayConfig(collection, code,
					!formMetadataVO.isInheritance() && formMetadataVO.isAdvancedSearch(),
					type, formMetadataVO.isHighlight(), formMetadataVO.getMetadataGroup(), displayType);
		} else {
			displayConfig = displayConfig.withHighlightStatus(formMetadataVO.isHighlight())
					.withVisibleInAdvancedSearchStatus(!formMetadataVO.isInheritance() && formMetadataVO.isAdvancedSearch())
					.withInputType(type)
					.withDisplayType(displayType).withMetadataGroup(formMetadataVO.getMetadataGroup());
		}

		displayManager.saveMetadata(displayConfig);

		if (formMetadataVO.isInheritance()) {
			String codeInDefaultSchema = schemaTypeCode + "_default_" + new SchemaUtils().getLocalCodeFromMetadataCode(code);
			displayConfig = displayManager.getMetadata(collection, codeInDefaultSchema)
					.withVisibleInAdvancedSearchStatus(formMetadataVO.isAdvancedSearch());
			displayManager.saveMetadata(displayConfig);
		}

		this.saveFacetDisplay(schemasManager, displayManager, code, formMetadataVO.isFacet());
		if (!editMode) {
			this.saveSchemaDisplay(schemasManager, displayManager, code);
		}
	}

	private void saveSchemaDisplay(MetadataSchemasManager schemasManager, SchemasDisplayManager displayManager,
								   String code) {

		SchemaTypesDisplayTransactionBuilder transactionBuilder = displayManager.newTransactionBuilderFor(collection);

		SchemaUtils schemaUtils = new SchemaUtils();

		String schemaCode = schemaUtils.getSchemaCode(code);
		String typeCode = schemaUtils.getSchemaTypeCode(schemaCode);
		String localCode = schemaUtils.getLocalCode(code, schemaCode);

		List<String> hugeMetadatas = schemasManager.getSchemaTypes(collection).getSchema(schemaCode).getMetadatas()
				.onlyWithType(MetadataValueType.STRUCTURE, MetadataValueType.TEXT).toLocalCodesList();

		transactionBuilder.in(typeCode)
				.addToDisplay(localCode)
				.beforeTheHugeCommentMetadata();

		transactionBuilder.in(typeCode)
				.addToForm(localCode)
				.beforeMetadatas(hugeMetadatas);

		displayManager.execute(transactionBuilder.build());

	}

	private void saveFacetDisplay(MetadataSchemasManager schemasManager, SchemasDisplayManager displayManager,
								  String code,
								  boolean isFacet) {
		Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(code);

		boolean isGlobal = false;
		for (Metadata global : Schemas.getAllGlobalMetadatas()) {
			if (metadata.getCode().equals(global.getCode())) {
				isGlobal = true;
				break;
			}
		}

		SchemaTypesDisplayConfig typesConfig = displayManager.getTypes(collection);
		List<String> facets = new ArrayList<>(typesConfig.getFacetMetadataCodes());

		if (isFacet) {
			if (isGlobal) {
				if (!facets.contains(metadata.getLocalCode())) {
					facets.add(metadata.getLocalCode());
				}
			} else {
				if (!facets.contains(metadata.getCode())) {
					facets.add(metadata.getCode());
				}
			}
		} else {
			if (facets.contains(metadata.getLocalCode())) {
				facets.remove(metadata.getLocalCode());
			} else if (facets.contains(metadata.getCode())) {
				facets.remove(metadata.getCode());
			}
		}

		typesConfig = typesConfig.withFacetMetadataCodes(facets);
		displayManager.saveTypes(typesConfig);
	}

	public List<String> getMetadataGroupList() {
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		return new ArrayList<>(schemaConfig.getMetadataGroup().keySet());
	}

	public String getGroupLabel(String code) {
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		return schemaConfig.getMetadataGroup().get(code).get(language);
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA, parameters);
		view.navigate().to().listSchemaMetadata(params);
	}

	public boolean isMetadataEnableStatusModifiable() {
		return metadataCode.isEmpty() || !getMetadata(metadataCode).isEssential();
	}

	private boolean isBuiltInMetadataStatusModifiable(final MetadataAttribute attribute) {
		boolean builtInMetadataModifiable = false;
		if (!metadataCode.startsWith("USR")) {
			final MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			final MetadataSchema schema = types.getSchema(schemaCode);
			if (schema.hasMetadataWithCode(metadataCode)) {
				final Metadata metadata = schema.getMetadata(metadataCode);

				builtInMetadataModifiable = appLayerFactory.getExtensions().forCollection(collection)
						.isMetadataRequiredStatusModifiable(
								new IsBuiltInMetadataAttributeModifiableParam() {

									@Override
									public Metadata getMetadata() {
										return metadata;
									}

									@Override
									public MetadataSchema getSchema() {
										return schema;
									}

									@Override
									public MetadataSchemaType getSchemaType() {
										return types.getSchemaType(schemaTypeCode);
									}

									@Override
									public MetadataAttribute getMetadataAttribute() {
										return attribute;
									}
								});
			}

		}
		return builtInMetadataModifiable;
	}

	public boolean isMetadataRequiredStatusModifiable() {
		return metadataCode == null || metadataCode.isEmpty()
			   || !getMetadata(metadataCode).isEssential()
			   || getMetadata(metadataCode).hasSameCode(LEGACY_ID)
			   || isBuiltInMetadataStatusModifiable(REQUIRED);
	}

	public boolean isFolderMediumTypes() {
		return metadataCode.startsWith(Folder.SCHEMA_TYPE) && metadataCode.endsWith(Folder.MEDIUM_TYPES);
	}

	public void inputTypeValueChanged(FormMetadataVO formMetadataVO) {
		boolean noReferenceType = formMetadataVO.getValueType() == REFERENCE && StringUtils
				.isBlank(formMetadataVO.getReference());
		if (!noReferenceType) {
			view.reloadForm();
		}
	}

	public void displayTypeValueChanged() {
		view.reloadForm();
	}

	public void valueTypeValueChanged() {
		view.reloadForm();
	}

	public void multivalueValueChanged(FormMetadataVO formMetadataVO) {
		boolean noReferenceType = formMetadataVO.getValueType() == REFERENCE && StringUtils
				.isBlank(formMetadataVO.getReference());
		if (!noReferenceType) {
			view.reloadForm();
		}
	}

	public void refTypeValueChanged() {
		view.reloadForm();
	}

	public MetadataVO getDefaultValueMetadataVO(FormMetadataVO formMetadataVO, boolean editMode) {

		try {
			MetadataInputType inputType = formMetadataVO.getInput();
			MetadataDisplayType displayType = formMetadataVO.getDisplayType();
			Class<? extends Enum<?>> enumClass = null;
			if (formMetadataVO.getValueType() == REFERENCE) {
				inputType = MetadataInputType.LOOKUP;
			} else if (formMetadataVO.getValueType() == BOOLEAN) {
				inputType = MetadataInputType.FIELD;
			}
			if (inputType != null && !inputType.equals(MetadataInputType.CHECKBOXES) && !inputType.equals(MetadataInputType.RADIO_BUTTONS)) {
				displayType = MetadataDisplayType.VERTICAL;
			}
			if (formMetadataVO.getValueType() == ENUM && editMode) {
				enumClass = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(formMetadataVO.getCode()).getEnumClass();
			}

			CollectionInfo collectionInfo = defaultSchema().getCollectionInfo();
			CollectionInfoVO collectionInfoVO = new CollectionInfoVO(collectionInfo.getMainSystemLanguage(), collectionInfo.getCode(), collectionInfo.getCollectionLanguages(),
					collectionInfo.getMainSystemLocale(), collectionInfo.getSecondaryCollectionLanguesCodes(), collectionInfo.getCollectionLanguesCodes(), collectionInfo.getCollectionLocales());

			MetadataVO metadataVO = new MetadataVO(formMetadataVO.getId(), formMetadataVO.getCode(), formMetadataVO.getLocalcode(), formMetadataVO.getValueType(), collection,
					formMetadataVO.getSchema(), formMetadataVO.isRequired(), formMetadataVO.isMultivalue(), false,
					new HashMap<Locale, String>(), enumClass, new String[]{}, formMetadataVO.getReference(), inputType, displayType,
					new AllowedReferences(formMetadataVO.getReference(), null), formMetadataVO.getMetadataGroup(),
					formMetadataVO.getDefaultValue(), false, formMetadataVO.getCustomAttributes(),
					formMetadataVO.isMultiLingual(), getCurrentLocale(), new HashMap<String, Object>(), collectionInfoVO, formMetadataVO.isSortable(), true);
			return metadataVO;
		} catch (Exception ex) {
			log.error("error", ex);
			return null;
		}
	}

	public boolean isDefaultValuePossible(FormMetadataVO formMetadataVO) {
		MetadataValueType valueType = formMetadataVO.getValueType();
		return valueType != null && !valueType.equals(MetadataValueType.CONTENT);
	}

	public boolean isMetadataSystemReserved() {
		return !metadataCode.isEmpty() && getMetadata(metadataCode).isSystemReserved();
	}

	public List<String> getMetadataReadRole() {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

		if (types != null && Strings.isNotBlank(metadataCode)) {
			Metadata metadata = types.getMetadata(metadataCode);
			if (metadata.getAccessRestrictions() != null) {
				return metadata.getAccessRestrictions().getRequiredReadRoles();
			}
		}

		return new ArrayList<>();
	}

	public boolean isAvailableInSummaryFlagAlwaysTrue(MetadataValueType type) {
		return type == BOOLEAN || type == INTEGER || type == NUMBER || type == REFERENCE;
	}

	public boolean isAvailableInSummaryFlagButtonVisible() {
		return schemaType(schemaTypeCode).getCacheType().isSummaryCache();
	}


	public boolean isAvailableInSummaryFlagButtonEnabled(MetadataValueType type) {
		return !isAvailableInSummaryFlagAlwaysTrue(type);
	}
}
