package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.vaadin.ui.OptionGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionSelectOptionField extends OptionGroup {
	protected AppLayerFactory appLayerFactory;
	protected List<Record> records;
	protected boolean restrictCollectionToRecords;

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records) {
		this(appLayerFactory, records, $("CollectionSecurityManagement.selectCollections"), false);
	}

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records,
									   boolean restrictCollectionToRecords) {
		this(appLayerFactory, records, $("CollectionSecurityManagement.selectCollections"), restrictCollectionToRecords);
	}

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records, String title) {
		this(appLayerFactory, records, title, false);
	}

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records, String title,
									   boolean restrictCollectionToRecords) {
		super(title);
		this.appLayerFactory = appLayerFactory;
		this.records = records;
		this.restrictCollectionToRecords = restrictCollectionToRecords;
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
				boolean existsforAll = records.stream().allMatch(record -> record.getCollection().equals(collection));
				if (existsforAll) {
					this.select(collection);
				}
			}
		}
	}

	protected List<String> getAvailableCollections() {
		if (!restrictCollectionToRecords) {
			return appLayerFactory.getCollectionsManager().getCollectionCodes();
		}

		Set<String> collections = new HashSet<>();
		for (Record record : records) {
			collections.add(record.getCollection());
		}
		return new ArrayList<>(collections);
	}
}
