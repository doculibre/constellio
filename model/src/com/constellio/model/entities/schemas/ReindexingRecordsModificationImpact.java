package com.constellio.model.entities.schemas;

import com.constellio.model.entities.records.Record;
import com.constellio.model.utils.Lazy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@EqualsAndHashCode
public class ReindexingRecordsModificationImpact implements ModificationImpact {

	@Getter final MetadataSchemaType impactedSchemaType;
	final Lazy<List<String>> idsSupplier;

	@Getter final Lazy<List<Record>> recordsSupplier;
	@Getter final List<Metadata> metadataToReindex;
	@Getter final boolean handledNow;

	@Override
	public String toString() {
		return "ReindexingRecordsModificationImpact{" +
			   "impactedSchemaType=" + impactedSchemaType.getCode() +
			   "ids=" + idsSupplier.get() +
			   "metadataToReindex=" + metadataToReindex +
			   '}';
	}

	public int getPotentialImpactsCount() {
		return recordsSupplier.isLoaded() ? recordsSupplier.get().size() : idsSupplier.get().size();
	}

	public List<String> getIds() {
		if (recordsSupplier.isLoaded()) {
			return recordsSupplier.get().stream().map(Record::getId).collect(Collectors.toList());
		} else {
			return idsSupplier.get();
		}
	}

	@Override
	public String getCollection() {
		return impactedSchemaType.getCollection();
	}

	@Override
	public List<ModificationImpactDetail> getDetails() {
		return Collections.singletonList(new ModificationImpactDetail(impactedSchemaType, getPotentialImpactsCount()));
	}
}
