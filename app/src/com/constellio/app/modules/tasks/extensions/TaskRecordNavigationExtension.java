package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.RecordNavigationExtensionUtils;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.Language;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class TaskRecordNavigationExtension implements RecordNavigationExtension {

	private AppLayerFactory appLayerFactory;
	private String collection;

	public TaskRecordNavigationExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
	}

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
	public void prepareLinkToView(final NavigationParams navigationParams, final boolean isRecordInTrash,
								  Locale currentLocale) {
		String schemaTypeCode = navigationParams.getSchemaTypeCode();
		if (isViewForSchemaTypeCode(schemaTypeCode)) {
			String schemaTypeLabel = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
					.getSchemaType(schemaTypeCode).getLabel(Language.withLocale(currentLocale)).toLowerCase();
			Map<String, Object> params = new HashMap<>();
			params.put("schemaType", schemaTypeLabel);
			final String errorMessage = $("ReferenceDisplay.cannotDisplayLogicallyDeletedRecord", params);

			Component component = navigationParams.getComponent();
			if (component instanceof ReferenceDisplay) {
				ReferenceDisplay referenceDisplay = (ReferenceDisplay) component;
				ClickListener clickListener = new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if (isRecordInTrash) {
							RecordNavigationExtensionUtils.showMessage(errorMessage);
						} else {
							navigateToView(navigationParams);
						}
					}
				};
				referenceDisplay.setEnabled(true);
				referenceDisplay.addClickListener(clickListener);
			} else if (component instanceof Table) {
				// FIXME Assumes that it is called by an item click listener
				if (isRecordInTrash) {
					RecordNavigationExtensionUtils.showMessage(errorMessage);
				} else {
					navigateToView(navigationParams);
				}
			}
		}
	}

	@Override
	public String getViewHrefTag(NavigationParams navigationParams) {
		return null;
	}
	
}
