package com.constellio.app.ui.pages.management.schemas.display.group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;

public class ListMetadataGroupSchemaTypePresenter extends SingleSchemaBasePresenter<ListMetadataGroupSchemaTypeView> {

	private String schemaTypeCode;
	
	private List<String> metadataGroups = new ArrayList<>();

	public ListMetadataGroupSchemaTypePresenter(ListMetadataGroupSchemaTypeView view) {
		super(view);
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		schemaTypeCode = paramsMap.get("schemaTypeCode");

		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		metadataGroups.addAll(typeConfig.getMetadataGroup().keySet());
		
		view.setMetadataGroups(metadataGroups);
	}

	public List<String> getMetadataGroupList() {
		return new ArrayList<>();
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
	
	private void saveChanges() {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> oldGroups = typeConfig.getMetadataGroup();
		Map<String, Map<Language, String>> newGroups = new LinkedHashMap<>();
		for (String metadataGroup : metadataGroups) {
			Map<Language, String> metadataGroupLabels = oldGroups.get(metadataGroup);
			newGroups.put(metadataGroup, metadataGroupLabels);
		}
		typeConfig = typeConfig.withMetadataGroup(newGroups);
		schemasDisplayManager().saveType(typeConfig);
	}
	
	void groupDroppedOn(String code, String targetCode, Boolean above) {
		int targetIndex = above ? metadataGroups.indexOf(targetCode) : metadataGroups.indexOf(targetCode) + 1;
		if (targetIndex >= metadataGroups.size()) {
			targetIndex = metadataGroups.size() - 1;
		}
		metadataGroups.remove(code);
		metadataGroups.add(targetIndex, code);
		saveChanges();
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
			saveChanges();
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
