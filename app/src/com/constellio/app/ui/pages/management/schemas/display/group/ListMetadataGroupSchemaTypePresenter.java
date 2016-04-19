package com.constellio.app.ui.pages.management.schemas.display.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;

public class ListMetadataGroupSchemaTypePresenter extends SingleSchemaBasePresenter<ListMetadataGroupSchemaTypeView> {

	public ListMetadataGroupSchemaTypePresenter(ListMetadataGroupSchemaTypeView view) {
		super(view);
	}

	private String schemaTypeCode;

	public void setSchemaTypeCode(String schemaTypeCode) {
		this.schemaTypeCode = schemaTypeCode;
	}

	public List<String> getMetadataGroupList() {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		return new ArrayList<>(typeConfig.getMetadataGroup().keySet());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	//TODO Thiago
	public void addGroupMetadata(String group) {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> labels = typeConfig.getMetadataGroup();

		//TODO Thiago
		if (!labels.keySet().contains(group) && !group.trim().equals("")) {
			//			labels.put(group);
			//			typeConfig = typeConfig.withMetadataGroup(labels);
			//			schemasDisplayManager().saveType(typeConfig);
			//			view.refreshTable();
		} else {
			view.displayAddError();
		}
	}

	public void deleteGroupMetadata(String group) {
		SchemaTypeDisplayConfig typeConfig = schemasDisplayManager().getType(collection, schemaTypeCode);
		//TODO Thiago
		Map<String, Map<Language, String>> labels = typeConfig.getMetadataGroup();
		if (labels.size() > 1) {
			labels.remove(group);
			typeConfig = typeConfig.withMetadataGroup(labels);
			schemasDisplayManager().saveType(typeConfig);
			view.refreshTable();
		} else {
			view.displayDeleteError();
		}
	}

	public void backButtonClicked() {
		view.navigateTo().listSchemaTypes();
	}
}
