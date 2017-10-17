package com.constellio.app.modules.rm.extensions;

import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.RecordNavigationExtensionUtils;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

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
	public void prepareLinkToView(final NavigationParams navigationParams, final boolean isRecordInTrash) {
		if (isViewForSchemaTypeCode(navigationParams.getSchemaTypeCode())) {
			Component component = navigationParams.getComponent();
			if (component instanceof ReferenceDisplay) {
				ReferenceDisplay referenceDisplay = (ReferenceDisplay) component;
				ClickListener clickListener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if(isRecordInTrash) {
							RecordNavigationExtensionUtils.showMessage("un test");
						} else {
							navigateToView(navigationParams);
						}
					}
				};
				referenceDisplay.setEnabled(true);
				referenceDisplay.addClickListener(clickListener);
			} else if (component instanceof Table) {
				// FIXME Assumes that it is called by an item click listener
				if(isRecordInTrash) {
					RecordNavigationExtensionUtils.showMessage("un test");
				} else {
					navigateToView(navigationParams);
				}
			}
		}
	}
}
