package com.constellio.app.extensions.records;

import com.constellio.app.extensions.records.params.NavigationParams;

public interface RecordNavigationExtension {

	void navigateToEdit(NavigationParams navigationParams);

	void navigateToView(NavigationParams navigationParams);

	boolean isViewForSchemaTypeCode(String schemaTypeCode);

	void prepareLinkToView(NavigationParams navigationParams);
}
