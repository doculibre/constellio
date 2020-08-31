package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.vaadin.ui.OptionGroup;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionSelectOptionField extends OptionGroup {
	protected AppLayerFactory appLayerFactory;
	protected List<Record> records;

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records) {
		this(appLayerFactory, records, $("CollectionSecurityManagement.selectCollections"));
	}

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records, String title) {
		super(title);
		this.appLayerFactory = appLayerFactory;
		this.records = records;
		build();
	}

	public List<String> getSelectedValues() {
		return new ArrayList<>((java.util.Collection<? extends String>) this.getValue());
	}

	private void build() {
		this.addStyleName("collections");
		this.addStyleName("collections-username");
		this.setId("collections");
		this.setMultiSelect(true);
		addCollections();
	}

	private void addCollections() {
		List<String> availableCollections = getAvailableCollections();
		for (String collection : availableCollections) {
			if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
				String collectionName = appLayerFactory.getCollectionsManager().getCollection(collection).getTitle();
				this.addItem(collection);
				this.setItemCaption(collection, collectionName);
				if (isCommonCollection(collection)) {
					processCommonCollection(collection);
				}
			}
		}
	}

	protected List<String> getAvailableCollections() {
		return appLayerFactory.getCollectionsManager().getCollectionCodes();
	}

	protected boolean isCommonCollection(String collection) {
		return records.stream().allMatch(record -> record.getCollection().equals(collection));
	}

	protected void processCommonCollection(String collection) {
		this.select(collection);
	}
}
