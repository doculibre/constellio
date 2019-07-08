package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class FolderUtil {
	public static Folder createCopyFrom(Folder folder, AppLayerFactory appLayerFactory) {
		String collection = folder.getCollection();
		Folder copy = createNewFolder(collection, appLayerFactory);

		for (Metadata metadata : folder.getSchema().getMetadatas()) {
			if (!metadata.isSystemReserved() && metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
				copy.set(metadata.getLocalCode(), folder.get(metadata));
			}
		}

		return copy;
	}

	public static Folder createNewFolder(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return rm.newFolder();
	}

	public static Folder getSubFolderWithSameTitle(String collection, AppLayerFactory appLayerFactory, String folderId,
												   String title) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return getSubFolderWithSameTitle(appLayerFactory, rm.getFolder(folderId), title);
	}

	public static Folder getSubFolderWithSameTitle(AppLayerFactory appLayerFactory, Folder folder, String title) {
		String collection = folder.getCollection();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		MetadataSchemaType foldersSchemaType = types.getSchemaType(Folder.SCHEMA_TYPE);
		MetadataSchema foldersSchema = types.getSchema(Folder.DEFAULT_SCHEMA);
		Metadata parentFolderMetadata = foldersSchema.getMetadata(Folder.PARENT_FOLDER);
		Metadata titleFolderMetadata = foldersSchema.getMetadata(Folder.TITLE);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(foldersSchemaType).where(parentFolderMetadata).is(folder.getWrappedRecord()).andWhere(titleFolderMetadata).isEqualTo(title));
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.sortAsc(Schemas.TITLE);

		List<Record> records = searchServices.query(query).getRecords();
		if (CollectionUtils.isNotEmpty(records)) {
			return rm.wrapFolder(records.get(0));
		}

		return null;
	}

	public static List<Folder> getSubFolders(String collection, AppLayerFactory appLayerFactory, String folderId) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return getSubFolders(appLayerFactory, rm.getFolder(folderId));
	}

	public static List<Folder> getSubFolders(AppLayerFactory appLayerFactory, Folder folder) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(folder.getCollection());
		MetadataSchemaType foldersSchemaType = types.getSchemaType(Folder.SCHEMA_TYPE);
		MetadataSchema foldersSchema = types.getSchema(Folder.DEFAULT_SCHEMA);
		Metadata parentFolderMetadata = foldersSchema.getMetadata(Folder.PARENT_FOLDER);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(foldersSchemaType).where(parentFolderMetadata).is(folder.getWrappedRecord()));
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.sortAsc(Schemas.TITLE);

		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(folder.getCollection(), appLayerFactory);
		Function<Record, Folder> recordToFolder = new Function<Record, Folder>() {
			@Override
			public Folder apply(Record input) {
				return rm.wrapFolder(input);
			}
		};

		List<Record> records = searchServices.query(query).getRecords();
		return Lists.transform((List<Record>) CollectionUtils.emptyIfNull(records), recordToFolder);
	}
}
