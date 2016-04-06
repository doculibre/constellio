package com.constellio.app.modules.rm.extensions;

import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class RMRecordNavigationExtension implements RecordNavigationExtension {

	@Override
	public void navigateToEdit(NavigationParams navigationParams) {
		RMViews constellioNavigator = navigationParams.navigate().to(RMViews.class);
		String schemaTypeCode = navigationParams.getSchemaTypeCode();
		String recordId = navigationParams.getRecordId();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.editFolder(recordId);
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.editDocument(recordId);
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.editContainer(recordId);
		} else if (RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.editRetentionRule(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}

	@Override
	public void navigateToView(NavigationParams navigationParams) {
		RMViews constellioNavigator = navigationParams.navigate().to(RMViews.class);
		String schemaTypeCode = navigationParams.getSchemaTypeCode();
		String recordId = navigationParams.getRecordId();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.displayFolder(recordId);
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.displayDocument(recordId);
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.displayContainer(recordId);
		} else if (RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode)) {
			constellioNavigator.displayRetentionRule(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}

	@Override
	public boolean isViewForSchemaTypeCode(String schemaTypeCode) {
		boolean viewForSchemaTypeCode;
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewForSchemaTypeCode = true;
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewForSchemaTypeCode = true;
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewForSchemaTypeCode = true;
		} else {
			viewForSchemaTypeCode = RetentionRule.SCHEMA_TYPE.equals(schemaTypeCode);
		}
		return viewForSchemaTypeCode;
	}

	@Override
	public void prepareLinkToView(final NavigationParams navigationParams) {
		if (isViewForSchemaTypeCode(navigationParams.getSchemaTypeCode())) {
			ReferenceDisplay component = (ReferenceDisplay) navigationParams.getComponent();
			ClickListener clickListener = new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					navigateToView(navigationParams);
				}
			};
			component.setEnabled(true);
			component.addClickListener(clickListener);
		}
	}
}
