package com.constellio.model.services.search.query;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;

public class ReturnedMetadatasFilter {

	private boolean includeParsedContent;

	private boolean includeLargeText;

	private Set<String> acceptedFields;

	public ReturnedMetadatasFilter(boolean includeParsedContent, boolean includeLargeText) {
		this.includeParsedContent = includeParsedContent;
		this.includeLargeText = includeLargeText;
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

	public static ReturnedMetadatasFilter onlyMetadatas(List<Metadata> metadatas) {
		Set<String> datastorecodes = new HashSet<>();
		for (Metadata metadata : metadatas) {
			datastorecodes.add(metadata.getDataStoreCode());
		}
		return new ReturnedMetadatasFilter(datastorecodes);
	}

	public static ReturnedMetadatasFilter allExceptContentAndLargeText() {
		return new ReturnedMetadatasFilter(false, false);
	}

	public static ReturnedMetadatasFilter allExceptLarge() {
		return new ReturnedMetadatasFilter(false, true);
	}

	public static ReturnedMetadatasFilter all() {
		return new ReturnedMetadatasFilter(true, true);
	}

	public boolean isIncludeParsedContent() {
		return includeParsedContent;
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
