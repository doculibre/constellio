package com.constellio.app.extensions.records;

import com.constellio.app.extensions.records.params.NavigationParams;

import java.util.Locale;

public interface RecordNavigationExtension {

	void navigateToEdit(NavigationParams navigationParams);

	void navigateToView(NavigationParams navigationParams);

	boolean isViewForSchemaTypeCode(String schemaTypeCode);

	boolean prepareLinkToView(NavigationParams navigationParams, boolean isRecordInTrash, Locale currentLocale);

	String getViewHrefTag(NavigationParams navigationParams);
	
}
