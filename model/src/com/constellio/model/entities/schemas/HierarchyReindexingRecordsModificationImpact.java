package com.constellio.model.entities.schemas;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.utils.Lazy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
/*
 * Sometime, a modification is so important that it can affect a large hierarchy of records. When this happen,
 * we prefer to start a background reindexing process for these records than handling this with markers (mark for reindexing)
 */
public class HierarchyReindexingRecordsModificationImpact implements ModificationImpact {

	@Getter final String collection;

	@Getter final RecordId rootIdToReindex;

	final Lazy<List<ModificationImpactDetail>> lazyImpactDetails;

	@Override
	public String toString() {
		return "HierarchyReindexingRecordsModificationImpact{" +
			   "rootIdToReindex=" + rootIdToReindex +
			   '}';
	}


	@Override
	public List<Metadata> getMetadataToReindex() {
		return null;
	}

	@Override
	public List<ModificationImpactDetail> getDetails() {
		return lazyImpactDetails.get();
	}


	@Override
	public boolean isHandledNow() {
		return false;
	}
}
