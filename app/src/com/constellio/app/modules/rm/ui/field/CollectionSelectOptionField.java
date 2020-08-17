package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.vaadin.ui.OptionGroup;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CollectionSelectOptionField extends OptionGroup {
	private AppLayerFactory appLayerFactory;
	private OptionGroup collections;
	private List<Record> records;

	public CollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records) {
		super($("CollectionSecurityManagement.selectCollections"));
		this.appLayerFactory = appLayerFactory;
		this.collections = collections;
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
		for (String collection : appLayerFactory.getCollectionsManager().getCollectionCodes()) {
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


}
