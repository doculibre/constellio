package com.constellio.model.entities.records;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.MetadataList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class TransactionRecordsReindexation {

	private boolean reindexAllMetadatas;

	private MetadataList reindexMetadatas = new MetadataList();

	public TransactionRecordsReindexation() {
		this.reindexAllMetadatas = false;
	}

	public TransactionRecordsReindexation(MetadataList reindexMetadatas) {
		this.reindexMetadatas = reindexMetadatas;
	}

	public TransactionRecordsReindexation(TransactionRecordsReindexation copy) {
		this.reindexAllMetadatas = copy.reindexAllMetadatas;
		this.reindexMetadatas = copy.reindexMetadatas;
	}

	public boolean isReindexed(Metadata metadata) {
		return reindexAllMetadatas || reindexMetadatas.contains(metadata);
	}

	public void addReindexedMetadata(Metadata metadata) {
		if (!isReindexed(metadata)) {
			this.reindexMetadatas.add(metadata);
		}
	}

	public void addReindexedMetadatas(List<Metadata> metadatasToReindex) {
		for (Metadata metadataToReindex : metadatasToReindex) {
			addReindexedMetadata(metadataToReindex);
		}

	}

	public static TransactionRecordsReindexation ALL() {
		TransactionRecordsReindexation reindexation = new TransactionRecordsReindexation();
		reindexation.reindexAllMetadatas = true;
		return reindexation;
	}

	public static TransactionRecordsReindexation NOTHING() {
		TransactionRecordsReindexation reindexation = new TransactionRecordsReindexation();
		reindexation.reindexAllMetadatas = false;
		return reindexation;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public boolean isReindexAll() {
		return reindexAllMetadatas;
	}
}
