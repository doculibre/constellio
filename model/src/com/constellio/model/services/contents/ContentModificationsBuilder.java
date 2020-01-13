package com.constellio.model.services.contents;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContentModificationsBuilder {

	MetadataSchemaTypes metadataSchemaTypes;

	public ContentModificationsBuilder(MetadataSchemaTypes metadataSchemaTypes) {
		this.metadataSchemaTypes = metadataSchemaTypes;
	}

	public ContentModifications buildForModifiedRecords(List<Record> records) {
		Set<String> originalRecordHashes = new HashSet<>();
		Set<String> recordHashes = new HashSet<>();

		for (Record record : records) {
			if (record.isSaved()) {
				originalRecordHashes.addAll(getContentHashesOf(record.getCopyOfOriginalRecord()));
			}
			recordHashes.addAll(getContentHashesOf(record));
		}

		ListComparisonResults<String> comparisonResults = LangUtils.compare(originalRecordHashes, recordHashes);
		return new ContentModifications(comparisonResults.getRemovedItems(), comparisonResults.getNewItems());
	}

	public List<String> buildForDeletedRecords(List<Record> records) {

		Set<String> allHashes = new HashSet<>();
		for (Record record : records) {
			if (record.isSaved()) {
				allHashes.addAll(getContentHashesOf(record.getCopyOfOriginalRecord()));
			}
			allHashes.addAll(getContentHashesOf(record));
		}

		return new ArrayList<>(allHashes);
	}

	List<Content> getAllContentsOfRecords(Record record) {
		List<Content> contents = new ArrayList<>();
		MetadataSchema schema = metadataSchemaTypes.getSchemaOf(record);
		for (Metadata metadata : new MetadataList(schema.getContentMetadatasForPopulate())) {
			if (metadata.isMultivalue()) {
				List<ContentImpl> metadataContents = record.getList(metadata);
				for (ContentImpl metadataContent : metadataContents) {
					contents.add(metadataContent);
				}
			} else {
				ContentImpl metadataContent = record.get(metadata);
				if (metadataContent != null) {
					contents.add(metadataContent);
				}
			}
		}
		return contents;
	}
	//
	//	List<Metadata> getModifiedContentMetadatas(Record record) {
	//		RecordImpl recordImpl = (RecordImpl) record;
	//		List<Metadata> schemaMetadatas = metadataSchematypes.getSchemaOf(record).getMetadatas();
	//		List<Metadata> modifiedContentMetadatas = new ArrayList<>();
	//		Map<String, Object> modifiedMetadatas = recordImpl.getModifiedValues();
	//		for (Metadata schemaMetadata : schemaMetadatas) {
	//			if (schemaMetadata.getType() == MetadataValueType.CONTENT && modifiedMetadatas
	//					.containsKey(schemaMetadata.getDataStoreCode())) {
	//				modifiedContentMetadatas.add(schemaMetadata);
	//			}
	//		}
	//		return modifiedContentMetadatas;
	//	}
	//
	//	List<Content> getModifiedContentsOfModifiedRecords(Record record) {
	//		List<Content> modifiedContents = new ArrayList<>();
	//		for (Metadata metadata : getModifiedContentMetadatas(record)) {
	//			if (metadata.isMultivalue()) {
	//				List<ContentImpl> metadataContents = record.getList(metadata);
	//				for (ContentImpl metadataContent : metadataContents) {
	//					if (metadataContent.getNextContentVersionInputStream() != null) {
	//						modifiedContents.add(metadataContent);
	//					}
	//				}
	//			} else {
	//				ContentImpl metadataContent = record.get(metadata);
	//				if (metadataContent != null && metadataContent.getNextContentVersionInputStream() != null) {
	//					modifiedContents.add(metadataContent);
	//				}
	//			}
	//		}
	//		return modifiedContents;
	//	}
	//
	//	List<String> getRemovedContentsOfModifiedRecords(RecordImpl record) {
	//		List<String> removedContentHashes = new ArrayList<>();
	//		List<Content> previousDeletedMetadataContents = new ArrayList<>();
	//		for (Metadata metadata : getModifiedContentMetadatas(record)) {
	//			if (metadata.isMultivalue()) {
	//				List<Content> metadataContents = record.getList(metadata);
	//				for (Content metadataContent : metadataContents) {
	//					if (metadataContent.isDirty() && metadataContent.getCurrentCheckedOutVersion() != null) {
	//						removedContentHashes.add(metadataContent.getCurrentCheckedOutVersion().getHash());
	//					}
	//				}
	//				if (record.getLoadedStructuredValues() != null) {
	//					List<Content> previousMetadataContents = (List<Content>) record.getLoadedStructuredValues()
	//							.get(metadata.getDataStoreCode());
	//					previousDeletedMetadataContents.addAll(getPreviousDeleteContents(previousMetadataContents,
	//							metadataContents));
	//				}
	//
	//			} else {
	//				Content metadataContent = record.get(metadata);
	//				if (metadataContent != null && metadataContent.isDirty()
	//						&& metadataContent.getCurrentCheckedOutVersion() != null && !metadataContent.getCurrentCheckedOutVersion()
	//						.hasSameHash(metadataContent.getCurrentVersion())) {
	//					removedContentHashes.add(metadataContent.getCurrentCheckedOutVersion().getHash());
	//				}
	//				if (record.getLoadedStructuredValues() != null) {
	//					Content previousContent = (Content) record.getLoadedStructuredValues().get(metadata.getDataStoreCode());
	//					if (previousContent != null && (metadataContent == null || !metadataContent.getId().equals(
	//							previousContent.getId()))) {
	//						previousDeletedMetadataContents.add(previousContent);
	//					}
	//				}
	//			}
	//		}
	//		for (Content previousContent : previousDeletedMetadataContents) {
	//			addAllVersionHashes(removedContentHashes, previousContent);
	//		}
	//		return removedContentHashes;
	//	}

	private void addAllVersionHashes(List<String> removedContentHashes, Content previousContent) {

		removedContentHashes.add(previousContent.getCurrentVersion().getHash());
		if (previousContent.getCurrentCheckedOutVersion() != null) {
			removedContentHashes.add(previousContent.getCurrentCheckedOutVersion().getHash());
		}
		for (ContentVersion historyVersion : previousContent.getHistoryVersions()) {
			removedContentHashes.add(historyVersion.getHash());
		}
	}

	private List<Content> getPreviousDeleteContents(List<Content> previousMetadataContents,
													List<Content> metadataContents) {
		List<Content> removedContents = new ArrayList<>();
		for (Content previousMetadataContent : previousMetadataContents) {
			if (!hasContentWithId(metadataContents, previousMetadataContent.getId())) {
				removedContents.add(previousMetadataContent);
			}
		}

		return removedContents;
	}

	private boolean hasContentWithId(List<Content> contents, String id) {
		for (Content current : contents) {
			if (id.equals(current.getId())) {
				return true;
			}
		}
		return false;
	}

	List<String> getContentHashesOf(Record record) {
		List<String> hashes = new ArrayList<>();

		for (Content content : getAllContentsOfRecords(record)) {
			addAllVersionHashes(hashes, content);
		}

		return hashes;
	}

}
