package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.ui.framework.components.converters.CollectionCodeToLabelConverter;

public class CollectionBreadcrumbItem implements BreadcrumbItem {
	
	private String collectionLabel;
    
    private CollectionCodeToLabelConverter collectionCodeToLabelConverter = new CollectionCodeToLabelConverter();

	public CollectionBreadcrumbItem(String collectionCode) {
		this.collectionLabel = collectionCodeToLabelConverter.getCollectionCaption(collectionCode);
	}

	@Override
	public String getLabel() {
		return collectionLabel;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
