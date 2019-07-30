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

	private boolean onlySummary;

	private Set<String> acceptedFields;

	private ReturnedMetadatasFilter() {
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
		ReturnedMetadatasFilter filter = new ReturnedMetadatasFilter();
		filter.acceptedFields = fields;
		return filter;
	}

	public static ReturnedMetadatasFilter onlySummaryFields() {
		ReturnedMetadatasFilter filter = new ReturnedMetadatasFilter();
		filter.onlySummary = true;
		filter.acceptedFields = null;
		filter.includeLargeText = false;
		return filter;
	}

	public static ReturnedMetadatasFilter onlyMetadatas(List<Metadata> metadatas) {
		Set<String> datastorecodes = new HashSet<>();
		for (Metadata metadata : metadatas) {
			datastorecodes.add(metadata.getDataStoreCode());
		}
		ReturnedMetadatasFilter filter = new ReturnedMetadatasFilter();
		filter.acceptedFields = datastorecodes;
		return filter;
	}

	public static ReturnedMetadatasFilter allExceptContentAndLargeText() {
		return new ReturnedMetadatasFilter();
	}

	public static ReturnedMetadatasFilter all() {
		ReturnedMetadatasFilter filter = new ReturnedMetadatasFilter();
		filter.includeLargeText = true;
		return filter;
	}

	public static ReturnedMetadatasFilter allAndWithIncludedFields(Set<String> acceptedFields) {
		ReturnedMetadatasFilter filter = new ReturnedMetadatasFilter();
		filter.onlySummary = false;
		filter.acceptedFields = acceptedFields;
		filter.includeLargeText = true;
		return filter;
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
			ReturnedMetadatasFilter filter = new ReturnedMetadatasFilter();
			filter.acceptedFields = acceptedFields;
			return filter;
		}
	}

	public boolean isOnlySummary() {
		return onlySummary;
	}

	public boolean isFullyLoaded() {
		return acceptedFields == null && includeLargeText;
	}
}
