package com.constellio.app.services.trash;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.Set;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class TrashServices {
	private final AppLayerFactory appLayerFactory;
	private final String collection;

	public TrashServices(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
	}

	public LogicalSearchQuery getTrashRecordsQueryForType(String selectedType, User currentUser) {
		MetadataSchemaType schema = appLayerFactory.getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(selectedType);
		LogicalSearchCondition condition = from(schema).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getTrashRecordsQueryForCollection(String collection, User currentUser) {
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		return new LogicalSearchQuery(condition).filteredWithUserDelete(currentUser).sortAsc(Schemas.TITLE);
	}

	public void deleteAll(String selectedType, User currentUser) {
		//TODO
	}

	public void restoreSelection(Set<String> selectedRecords, User currentUser) {
		//TODO
	}

	public void deleteSelection(Set<String> selectedRecords, User currentUser) {
		//TODO
	}
}
