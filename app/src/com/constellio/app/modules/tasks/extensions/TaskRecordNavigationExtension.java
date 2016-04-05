package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class TaskRecordNavigationExtension implements RecordNavigationExtension {

	@Override
	public void navigateToEdit(NavigationParams navigationParams) {
		String schemaTypeCode = navigationParams.getSchemaTypeCode();
		String recordId = navigationParams.getRecordId();
		if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
			navigationParams.navigate().to(TaskViews.class).editTask(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}

	@Override
	public void navigateToView(NavigationParams navigationParams) {
		String schemaTypeCode = navigationParams.getSchemaTypeCode();
		String recordId = navigationParams.getRecordId();
		if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
			navigationParams.navigate().to(TaskViews.class).displayTask(recordId);
		} else {
			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}

	}

	@Override
	public boolean isViewForSchemaTypeCode(String schemaTypeCode) {
		return Task.SCHEMA_TYPE.equals(schemaTypeCode);
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
