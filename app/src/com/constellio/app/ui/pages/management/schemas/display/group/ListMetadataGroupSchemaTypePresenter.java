package com.constellio.app.ui.pages.management.schemas.display.group;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ListMetadataGroupSchemaTypePresenter extends SingleSchemaBasePresenter<ListMetadataGroupSchemaTypeView> {

	private String schemaTypeCode;

	private List<String> metadataGroups = new ArrayList<>();
	private String defaultMetadataGroup;

	public ListMetadataGroupSchemaTypePresenter(ListMetadataGroupSchemaTypeView view) {
		super(view);
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		schemaTypeCode = paramsMap.get("schemaTypeCode");

		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Set<String> allMetadataGroups = typeConfig.getMetadataGroup().keySet();
		for (String metadataGroup : allMetadataGroups) {
			metadataGroups.add(metadataGroup);
			if (StringUtils.isEmpty(defaultMetadataGroup) && metadataGroup.startsWith("default:")) {
				defaultMetadataGroup = metadataGroup;
			}
		}
		
		view.setMetadataGroups(metadataGroups);
	}

	public List<String> getMetadataGroupList() {
		return new ArrayList<>(metadataGroups);
	}

	public String getGroupLabel(String code, String language) {
		String label = "";
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> group = schemaConfig.getMetadataGroup();
		if (group.containsKey(code) && group.get(code).containsKey(Language.withCode(language))) {
			label = group.get(code).get(Language.withCode(language));
		}
		return label;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public boolean isValidCodeAndLabels(String code, Map<String, String> group) {
		if (StringUtils.isBlank(code)) {
			view.invalidCodeOrLabels();
			return false;
		}
		for (Entry<String, String> entry : group.entrySet()) {
			if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())) {
				view.invalidCodeOrLabels();
				return false;
			}
		}
		return true;
	}

	public List<String> getCollectionLanguages() {
		return collectionsManager().getCollectionLanguages(collection);
	}

	public void backButtonClicked() {
		view.navigate().to().listSchemaTypes();
	}

	private void saveChanges(String metadataGroupCode, boolean metatadaGroupCodeDeleted) {
		SchemasDisplayManager displayManager = schemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		SchemaTypeDisplayConfig typeConfig = displayManager.getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> oldGroups = typeConfig.getMetadataGroup();
		Map<String, Map<Language, String>> newGroups = new LinkedHashMap<>();
		defaultMetadataGroup = null;
		for (String metadataGroup : metadataGroups) {
			if (StringUtils.isEmpty(defaultMetadataGroup) && metadataGroup.startsWith("default:")) {
				defaultMetadataGroup = metadataGroup;
			}

			Map<Language, String> metadataGroupLabels = oldGroups.get(metadataGroup);
			newGroups.put(metadataGroup, metadataGroupLabels);
		}
		typeConfig = typeConfig.withMetadataGroup(newGroups);
		transaction.add(typeConfig);

		if (metatadaGroupCodeDeleted) {
			MetadataSchemaType schemaType = schemaType(schemaTypeCode);
			for (MetadataSchema metadataSchema : schemaType.getAllSchemas()) {
				for (Metadata metadata : metadataSchema.getMetadatas()) {
					MetadataDisplayConfig displayConfig = displayManager.getMetadata(collection, metadata.getCode());
					if (displayConfig.getMetadataGroupCode().equals(metadataGroupCode)) {
						transaction.add(displayConfig.withMetadataGroup(getDefaultMetadataGroupCode()));
					}
				}
			}
		}

		displayManager.execute(transaction);
	}

	public String getDefaultMetadataGroupCode() {
		return defaultMetadataGroup;
	}

	void groupDroppedOn(String code, String targetCode, Boolean above) {
		int targetIndex = above ? metadataGroups.indexOf(targetCode) : metadataGroups.indexOf(targetCode) + 1;
		if (targetIndex >= metadataGroups.size()) {
			targetIndex = metadataGroups.size() - 1;
		}
		metadataGroups.remove(code);
		metadataGroups.add(targetIndex, code);
		saveChanges(code, false);
	}

	void windowClosed() {

	}

	void addButtonClicked() {
		List<String> languageCodes = getCollectionLanguages();
		view.showAddWindow(languageCodes);
	}

	void editButtonClicked(String code) {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> groups = typeConfig.getMetadataGroup();
		Map<Language, String> group = groups.get(code);
		Map<String, String> labels = new LinkedHashMap<>();
		for (Entry<Language, String> entry : group.entrySet()) {
			labels.put(entry.getKey().getCode(), entry.getValue());
		}
		view.showEditWindow(code, labels);
	}

	void deleteButtonClicked(String code) {
		if (metadataGroups.size() > 1) {
			metadataGroups.remove(code);
			saveChanges(code, true);
			view.removeMetadataGroup(code);
		} else {
			view.displayDeleteError();
		}
	}

	void cancelButtonClicked(String code, Map<String, String> labels, boolean adding) {
		view.closeAllWindows();
	}

	void saveButtonClicked(String code, Map<String, String> labels, boolean adding) {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> groups = typeConfig.getMetadataGroup();
		Map<String, Map<Language, String>> newGroups = new LinkedHashMap<>();
		newGroups.putAll(groups);
		Map<Language, String> languageLabels = new LinkedHashMap<>();
		for (Entry<String, String> entry : labels.entrySet()) {
			languageLabels.put(Language.withCode(entry.getKey()), entry.getValue());
		}
		if (isValidCodeAndLabels(code, labels)) {
			if (adding) {
				if (!groups.keySet().contains(code)) {
					newGroups.put(code, languageLabels);
					typeConfig = typeConfig.withMetadataGroup(newGroups);
					schemasDisplayManager().saveType(typeConfig);
					view.closeAllWindows();
					metadataGroups.add(code);
					if (StringUtils.isEmpty(defaultMetadataGroup) && code.startsWith("default:")) {
						defaultMetadataGroup = code;
					}
					view.addMetadataGroup(code, labels);
				} else {
					view.displayAddError();
				}
			} else {
				newGroups.put(code, languageLabels);
				typeConfig = typeConfig.withMetadataGroup(newGroups);
				schemasDisplayManager().saveType(typeConfig);
				view.closeAllWindows();
				view.updateMetadataGroup(code, labels);
			}
		} else {
			view.invalidCodeOrLabels();
		}
	}

	public Map<String, String> getGroupLabels(String group) {
		Map<String, String> result = new LinkedHashMap<>();
		SchemaTypeDisplayConfig schemaConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> groups = schemaConfig.getMetadataGroup();
		Map<Language, String> groupLabels = groups.get(group);
		for (Language language : groupLabels.keySet()) {
			result.put(language.getCode(), groupLabels.get(language));
		}
		return result;
	}

}
