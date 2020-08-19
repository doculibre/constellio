package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupCollectionSelectOptionField extends CollectionSelectOptionField {

	public GroupCollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records) {
		super(appLayerFactory, records);
	}

	public GroupCollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records,
											boolean restrictCollectionToRecords) {
		super(appLayerFactory, records, restrictCollectionToRecords);
	}

	public GroupCollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records, String title) {
		super(appLayerFactory, records, title);
	}

	public GroupCollectionSelectOptionField(AppLayerFactory appLayerFactory, List<Record> records, String title,
											boolean restrictCollectionToRecords) {
		super(appLayerFactory, records, title, restrictCollectionToRecords);
	}

	@Override
	protected List<String> getAvailableCollections() {
		if (!restrictCollectionToRecords) {
			return appLayerFactory.getCollectionsManager().getCollectionCodes();
		}

		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		SchemasRecordsServices schemasRecordsServices =
				new SchemasRecordsServices(records.get(0).getCollection(), appLayerFactory.getModelLayerFactory());

		Set<String> collections = new HashSet<>();
		List<Group> groups = schemasRecordsServices.wrapGroups(records);
		for (Group group : groups) {
			collections.addAll(userServices.getGroup(group.getCode()).getCollections());
		}
		return new ArrayList<>(collections);
	}
}
