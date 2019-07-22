package com.constellio.model.services.search.query;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class ReturnedMetadatasFilter {

	private boolean includeLargeText;

	private Set<String> acceptedFields;

	public ReturnedMetadatasFilter(boolean includeLargeText) {
		this.includeLargeText = includeLargeText;
	}

	public ReturnedMetadatasFilter(boolean includeLargeText, Set<String> acceptedFields) {
		this.includeLargeText = includeLargeText;
		this.acceptedFields = acceptedFields;
	}

	public ReturnedMetadatasFilter(Set<String> acceptedFields) {
		this.acceptedFields = acceptedFields;
	}

	public static ReturnedMetadatasFilter idVersionSchema() {
		return ReturnedMetadatasFilter.onlyMetadatas(new ArrayList<Metadata>());
	}

	public static ReturnedMetadatasFilter idVersionSchemaTitle() {
		return ReturnedMetadatasFilter.onlyMetadatas(asList(Schemas.TITLE));
	}

	public static ReturnedMetadatasFilter idVersionSchemaTitlePath() {
		return ReturnedMetadatasFilter.onlyMetadatas(asList(Schemas.TITLE, Schemas.PATH));
	}

	public static ReturnedMetadatasFilter onlyMetadatas(Metadata... metadatas) {
		return ReturnedMetadatasFilter.onlyMetadatas(asList(metadatas));
	}

	public static ReturnedMetadatasFilter onlyFields(Set<String> fields) {
		return new ReturnedMetadatasFilter(fields);
	}

	public static ReturnedMetadatasFilter onlySummaryFields(Set<String> fields) {
		return new ReturnedMetadatasFilter(fields);
	}

	public static ReturnedMetadatasFilter onlyMetadatas(List<Metadata> metadatas) {
		Set<String> datastorecodes = new HashSet<>();
		for (Metadata metadata : metadatas) {
			datastorecodes.add(metadata.getDataStoreCode());
		}
		return new ReturnedMetadatasFilter(datastorecodes);
	}

	public static ReturnedMetadatasFilter allExceptContentAndLargeText() {
		return new ReturnedMetadatasFilter(false);
	}

	public static ReturnedMetadatasFilter allExceptLarge() {
		return new ReturnedMetadatasFilter(true);
	}

	public static ReturnedMetadatasFilter all() {
		return new ReturnedMetadatasFilter(true);
	}

	public static ReturnedMetadatasFilter allAndWithIncludedFields(Set<String> acceptedFields) {
		return new ReturnedMetadatasFilter(true, acceptedFields);
	}

	public Set<String> getAcceptedFields() {
		return acceptedFields;
	}

	public ReturnedMetadatasFilter withIncludedMetadata(Metadata metadata) {
		return withIncludedFields(asList(metadata.getDataStoreCode()));
	}

	public ReturnedMetadatasFilter withIncludedMetadatas(Metadata... metadatas) {
		List<String> fields = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			fields.add(metadata.getDataStoreCode());
		}

		return withIncludedFields(fields);
	}

	public ReturnedMetadatasFilter withIncludedFields(List<String> fields) {
		if (this.acceptedFields == null) {
			return this;
		} else {
			Set<String> acceptedFields = new HashSet<>(this.acceptedFields);
			acceptedFields.addAll(fields);
			return new ReturnedMetadatasFilter(acceptedFields);
		}
	}

	public boolean isFullyLoaded() {
		return acceptedFields == null && includeLargeText;
	}
}
