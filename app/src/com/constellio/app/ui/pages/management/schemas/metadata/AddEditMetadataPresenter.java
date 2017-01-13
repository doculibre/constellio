package com.constellio.app.ui.pages.management.schemas.metadata;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataInSummaryCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditMetadataPresenter extends SingleSchemaBasePresenter<AddEditMetadataView> {
	private Map<String, String> parameters;
	private String metadataCode;

	public AddEditMetadataPresenter(AddEditMetadataView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public void setMetadataCode(String metadataCode) {
		this.metadataCode = metadataCode;
	}

	public FormMetadataVO getFormMetadataVO() {
		FormMetadataVO found = null;

		if (metadataCode == null || metadataCode.isEmpty()) {
			return found;
		}

		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		if (types != null) {
			Metadata metadata = types.getMetadata(metadataCode);

			MetadataToFormVOBuilder voBuilder = new MetadataToFormVOBuilder(view.getSessionContext());
			found = voBuilder.build(metadata, displayManager, parameters.get("schemaTypeCode"), view.getSessionContext());
		}

		return found;
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

	public List<String> getMetadataTypesCode() {
		List<String> typeCodes = new ArrayList<>();
		final Map<String, String> typeCodesAndLabels = new HashMap<>();
		for (MetadataSchemaType type : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes()) {
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

	public void saveButtonClicked(FormMetadataVO formMetadataVO, boolean editMode) {
		String schemaCode = getSchemaCode();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemasManager.modify(collection);
		String code;
		boolean reindexRequired = false;

		MetadataBuilder builder;
		if (!editMode) {
			builder = types.getSchema(schemaCode).create("USR" + formMetadataVO.getLocalcode());
			builder.setMultivalue(formMetadataVO.isMultivalue());
			builder.setType(formMetadataVO.getValueType());
			builder.setSortable(formMetadataVO.isSortable());
			builder.setSchemaAutocomplete(formMetadataVO.isAutocomplete());
			builder.setSearchable(formMetadataVO.isSearchable());
			if (formMetadataVO.getValueType().equals(MetadataValueType.REFERENCE)) {
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
		} else {
			builder = types.getSchema(schemaCode).get(formMetadataVO.getCode());
			code = formMetadataVO.getCode();
			if (!isInherited(code)) {
				reindexRequired = builder.isSortable() != formMetadataVO.isSortable() ||
						builder.isSearchable() != formMetadataVO.isSearchable();
				builder.setSortable(formMetadataVO.isSortable());
				builder.setSchemaAutocomplete(formMetadataVO.isAutocomplete());
				builder.setSearchable(formMetadataVO.isSearchable());
			}
		}
		builder.setDefaultValue(formMetadataVO.getDefaultValue());
		builder.setInputMask(formMetadataVO.getInputMask());
		builder.setEnabled(formMetadataVO.isEnabled());
		for (Entry<String, String> entry : formMetadataVO.getLabels().entrySet()) {
			builder.addLabel(Language.withCode(entry.getKey()), entry.getValue());
		}
		builder.setDefaultRequirement(formMetadataVO.isRequired());
		builder.setDuplicable(formMetadataVO.isDuplicable());

		if (isInherited(code)) {
			MetadataSchemaBuilder defaultSchemaBuilder = types
					.getSchema(schemaCode.substring(0, schemaCode.lastIndexOf('_')) + "_default");
			String localCode = code.substring(code.lastIndexOf("_") + 1);
			if (defaultSchemaBuilder.hasMetadata(localCode)) {
				defaultSchemaBuilder.getMetadata(localCode).setInputMask(formMetadataVO.getInputMask());
			}
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
		if (displayType == null || (!MetadataInputType.CHECKBOXES.equals(type) && !MetadataInputType.RADIO_BUTTONS.equals(type))) {
			displayType = MetadataDisplayType.VERTICAL;
		}

		MetadataDisplayConfig displayConfig = displayManager.getMetadata(collection, code);
		if (displayConfig == null) {
			displayConfig = new MetadataDisplayConfig(collection, code, formMetadataVO.isAdvancedSearch(),
					type, formMetadataVO.isHighlight(), formMetadataVO.getMetadataGroup(), displayType);
		} else {
			displayConfig = displayConfig.withHighlightStatus(formMetadataVO.isHighlight())
					.withVisibleInAdvancedSearchStatus(formMetadataVO.isAdvancedSearch()).withInputType(type)
					.withDisplayType(displayType).withMetadataGroup(formMetadataVO.getMetadataGroup());
		}

		displayManager.saveMetadata(displayConfig);

		this.saveFacetDisplay(schemasManager, displayManager, code, formMetadataVO.isFacet());
		if (!editMode) {
			this.saveSchemaDisplay(schemasManager, displayManager, code);
		}
	}

	private void saveSchemaDisplay(MetadataSchemasManager schemasManager, SchemasDisplayManager displayManager, String code) {

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

	private void saveFacetDisplay(MetadataSchemasManager schemasManager, SchemasDisplayManager displayManager, String code,
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
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, parameters.get("schemaTypeCode"));
		return new ArrayList<>(schemaConfig.getMetadataGroup().keySet());
	}

	public String getGroupLabel(String code) {
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, parameters.get("schemaTypeCode"));
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

	public boolean isMetadataRequiredStatusModifiable() {
		return metadataCode.isEmpty() || !getMetadata(metadataCode).isEssential();
	}

	public void inputTypeValueChanged(FormMetadataVO formMetadataVO) {
		boolean noReferenceType = formMetadataVO.getValueType() == MetadataValueType.REFERENCE && StringUtils
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
		boolean noReferenceType = formMetadataVO.getValueType() == MetadataValueType.REFERENCE && StringUtils
				.isBlank(formMetadataVO.getReference());
		if (!noReferenceType) {
			view.reloadForm();
		}
	}

	public void refTypeValueChanged() {
		view.reloadForm();
	}

	public MetadataVO getDefaultValueMetadataVO(FormMetadataVO formMetadataVO) {

		try {
			MetadataInputType inputType = formMetadataVO.getInput();
			MetadataDisplayType displayType = formMetadataVO.getDisplayType();
			if (formMetadataVO.getValueType() == MetadataValueType.REFERENCE) {
				inputType = MetadataInputType.LOOKUP;
			}
			if (!inputType.equals(MetadataInputType.CHECKBOXES) && !inputType.equals(MetadataInputType.RADIO_BUTTONS)){
				displayType = MetadataDisplayType.VERTICAL;
			}
			MetadataVO metadataVO = new MetadataVO(formMetadataVO.getCode(), formMetadataVO.getValueType(), collection,
					formMetadataVO.getSchema(), formMetadataVO.isRequired(), formMetadataVO.isMultivalue(), false,
					new HashMap<Locale, String>(), null, new String[] {}, formMetadataVO.getReference(), inputType, displayType,
					new AllowedReferences(formMetadataVO.getReference(), null), formMetadataVO.getMetadataGroup(),
					formMetadataVO.getDefaultValue(), false);
			return metadataVO;
		} catch (Exception ex) {
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
}
