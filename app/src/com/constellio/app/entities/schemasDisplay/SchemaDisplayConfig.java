package com.constellio.app.entities.schemasDisplay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class SchemaDisplayConfig implements Serializable {

	private final String schemaCode;

	private final String collection;

	private final List<String> displayMetadataCodes;

	private final List<String> formMetadataCodes;

	private final List<String> formHiddenMetadataCodes;

	private final List<String> searchResultsMetadataCodes;

	private final List<String> tableMetadataCodes;

	public SchemaDisplayConfig(String collection, String schemaCode, List<String> displayMetadataCodes,
							   List<String> formMetadataCodes, List<String> formHiddenMetadataCodes,
							   List<String> searchResultsMetadataCodes, List<String> tableMetadataCodes) {
		this.collection = collection;
		this.schemaCode = schemaCode;
		this.displayMetadataCodes = Collections.unmodifiableList(displayMetadataCodes);
		this.formMetadataCodes = Collections.unmodifiableList(formMetadataCodes);
		this.formHiddenMetadataCodes = Collections.unmodifiableList(formHiddenMetadataCodes);;
		this.searchResultsMetadataCodes = Collections.unmodifiableList(searchResultsMetadataCodes);
		this.tableMetadataCodes = Collections.unmodifiableList(tableMetadataCodes);
	}

	public List<String> getDisplayMetadataCodes() {
		return displayMetadataCodes;
	}

	public List<String> getFormMetadataCodes() {
		return formMetadataCodes;
	}

	public List<String> getFormHiddenMetadataCodes() {
		return formHiddenMetadataCodes;
	}

	public List<String> getSearchResultsMetadataCodes() {
		return searchResultsMetadataCodes;
	}

	public List<String> getTableMetadataCodes() {
		return tableMetadataCodes;
	}

	public String getSchemaCode() {
		return schemaCode;
	}

	public String getCollection() {
		return collection;
	}

	public SchemaDisplayConfig withDisplayMetadataCodes(List<String> displayMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchResultsMetadataCodes, tableMetadataCodes);
	}

	public SchemaDisplayConfig withFormMetadataCodes(List<String> formMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchResultsMetadataCodes, tableMetadataCodes);
	}

	public SchemaDisplayConfig withSearchResultsMetadataCodes(List<String> searchResultsMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchResultsMetadataCodes, tableMetadataCodes);
	}

	public SchemaDisplayConfig withTableMetadataCodes(List<String> tableMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchResultsMetadataCodes, tableMetadataCodes);
	}

	public SchemaDisplayConfig withNewSearchResultMetadataCode(String code) {
		List<String> result = new ArrayList<>(searchResultsMetadataCodes);
		result.add(code);
		return withSearchResultsMetadataCodes(result);
	}

	public SchemaDisplayConfig withNewDisplayMetadataBefore(String metadataCode, String before) {
		int index = displayMetadataCodes.indexOf(before);
		if (index < 0 && !displayMetadataCodes.isEmpty()) {
			index = displayMetadataCodes.size() - 1;
		} else if (index < 0) {
			index = 0;
		}
		List<String> displayMetadataCodes = new ArrayList<>();
		displayMetadataCodes.addAll(this.displayMetadataCodes);
		displayMetadataCodes.add(index, metadataCode);
		return withDisplayMetadataCodes(displayMetadataCodes);
	}

	public SchemaDisplayConfig withNewDisplayMetadataQueued(String metadataCode) {
		List<String> displayMetadataCodes = new ArrayList<>();
		displayMetadataCodes.addAll(this.displayMetadataCodes);
		displayMetadataCodes.add(metadataCode);
		return withDisplayMetadataCodes(displayMetadataCodes);
	}

	public SchemaDisplayConfig withNewFormMetadata(String metadataCode) {
		List<String> formMetadatas = new ArrayList<>();
		formMetadatas.addAll(this.formMetadataCodes);
		formMetadatas.add(metadataCode);
		return withFormMetadataCodes(formMetadatas);
	}

	public SchemaDisplayConfig withNewFormAndDisplayMetadatas(String... metadataCodes) {

		List<String> displayMetadatas = new ArrayList<>();
		displayMetadatas.addAll(this.displayMetadataCodes);
		displayMetadatas.addAll(asList(metadataCodes));

		List<String> formMetadatas = new ArrayList<>();
		formMetadatas.addAll(this.formMetadataCodes);
		formMetadatas.addAll(asList(metadataCodes));
		return withFormMetadataCodes(formMetadatas).withDisplayMetadataCodes(displayMetadatas);
	}

	public SchemaDisplayConfig withNewFormMetadatas(String... metadataCodes) {

		List<String> formMetadatas = new ArrayList<>();
		formMetadatas.addAll(this.formMetadataCodes);
		formMetadatas.addAll(asList(metadataCodes));
		return withFormMetadataCodes(formMetadatas);
	}

	public SchemaDisplayConfig withRemovedDisplayMetadatas(String... metadataCodes) {

		List<String> displayMetadatas = new ArrayList<>();
		displayMetadatas.addAll(this.displayMetadataCodes);
		displayMetadatas.removeAll(asList(metadataCodes));
		return withDisplayMetadataCodes(displayMetadatas);
	}

	public SchemaDisplayConfig withRemovedTableMetadatas(String... metadataCodes) {

		List<String> tableMetadatas = new ArrayList<>();
		tableMetadatas.addAll(this.tableMetadataCodes);
		tableMetadatas.removeAll(asList(metadataCodes));
		return withTableMetadataCodes(tableMetadatas);
	}

	public SchemaDisplayConfig withRemovedFormMetadatas(String... metadataCodes) {

		List<String> formMetadatas = new ArrayList<>();
		formMetadatas.addAll(this.formMetadataCodes);
		formMetadatas.removeAll(asList(metadataCodes));
		return withFormMetadataCodes(formMetadatas);
	}

	public SchemaDisplayConfig withNewFormMetadataBefore(String metadataCode, String before) {
		int index = formMetadataCodes.indexOf(before);
		List<String> formMetadatas = new ArrayList<>();
		formMetadatas.addAll(this.formMetadataCodes);
		formMetadatas.add(index, metadataCode);
		return withFormMetadataCodes(formMetadatas);
	}

	public SchemaDisplayConfig withNewTableMetadatas(String... metadataCodes) {
		List<String> tableMetadatas = new ArrayList<>();
		tableMetadatas.addAll(this.tableMetadataCodes);
		tableMetadatas.addAll(asList(metadataCodes));
		return withTableMetadataCodes(tableMetadatas);
	}

	public SchemaDisplayConfig withNewTableMetadatasBefore(String metadataCode, String before) {
		int index = tableMetadataCodes.indexOf(before);
		List<String> tableMetadatas = new ArrayList<>();
		tableMetadatas.addAll(this.tableMetadataCodes);
		tableMetadatas.add(index, metadataCode);
		return withTableMetadataCodes(tableMetadatas);
	}

	public SchemaDisplayConfig withCode(String toCode) {

		List<String> displayMetadataCodes = listForCode(this.displayMetadataCodes, toCode);
		List<String> formMetadataCodes = listForCode(this.formMetadataCodes, toCode);
		List<String> formHiddenMetadataCodes = listForCode(this.formHiddenMetadataCodes, toCode);
		List<String> searchResultsMetadataCodes = listForCode(this.searchResultsMetadataCodes, toCode);
		List<String> tableMetadataCodes = listForCode(this.tableMetadataCodes, toCode);

		return new SchemaDisplayConfig(collection, toCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchResultsMetadataCodes, tableMetadataCodes);
	}

	private List<String> listForCode(List<String> codes, String toCode) {
		List<String> returnedCodes = new ArrayList<>();
		for (String code : codes) {
			returnedCodes.add(code.replace(this.schemaCode, toCode));
		}
		return returnedCodes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SchemaDisplayConfig)) {
			return false;
		}

		SchemaDisplayConfig that = (SchemaDisplayConfig) o;

		if (schemaCode != null ? !schemaCode.equals(that.schemaCode) : that.schemaCode != null) {
			return false;
		}
		if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
			return false;
		}
		if (displayMetadataCodes != null ?
			!displayMetadataCodes.equals(that.displayMetadataCodes) :
			that.displayMetadataCodes != null) {
			return false;
		}
		if (formMetadataCodes != null ? !formMetadataCodes.equals(that.formMetadataCodes) : that.formMetadataCodes != null) {
			return false;
		}
		if (formHiddenMetadataCodes != null ? !formHiddenMetadataCodes.equals(that.formHiddenMetadataCodes) : that.formHiddenMetadataCodes != null) {
			return false;
		}
		if (searchResultsMetadataCodes != null ?
			!searchResultsMetadataCodes.equals(that.searchResultsMetadataCodes) :
			that.searchResultsMetadataCodes != null) {
			return false;
		}
		return tableMetadataCodes != null ? tableMetadataCodes.equals(that.tableMetadataCodes) : that.tableMetadataCodes == null;
	}

	@Override
	public int hashCode() {
		int result = schemaCode != null ? schemaCode.hashCode() : 0;
		result = 31 * result + (collection != null ? collection.hashCode() : 0);
		result = 31 * result + (displayMetadataCodes != null ? displayMetadataCodes.hashCode() : 0);
		result = 31 * result + (formMetadataCodes != null ? formMetadataCodes.hashCode() : 0);
		result = 31 * result + (formHiddenMetadataCodes != null ? formHiddenMetadataCodes.hashCode() : 0);
		result = 31 * result + (searchResultsMetadataCodes != null ? searchResultsMetadataCodes.hashCode() : 0);
		result = 31 * result + (tableMetadataCodes != null ? tableMetadataCodes.hashCode() : 0);
		return result;
	}
}