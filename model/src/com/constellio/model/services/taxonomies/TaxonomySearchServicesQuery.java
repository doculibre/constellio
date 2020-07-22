package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import lombok.Getter;
import lombok.Setter;

public class TaxonomySearchServicesQuery {

	@Getter
	User user;

	@Getter
	Record record;

	@Getter
	Taxonomy taxonomy;

	@Getter
	MetadataSchemaType forSelectionOfType;

	@Getter
	@Setter
	TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();

	private TaxonomySearchServicesQuery(User user, Record record, Taxonomy taxonomy,
										MetadataSchemaType forSelectionOfType) {
		this.user = user;
		this.record = record;
		this.taxonomy = taxonomy;
		this.forSelectionOfType = forSelectionOfType;
	}

	public static TaxonomySearchServicesQuery rootNodesOfTaxonomyForSelectionOfSchemaType(Taxonomy taxonomy,
																						  MetadataSchemaType schemaType,
																						  User user) {
		return new TaxonomySearchServicesQuery(user, null, taxonomy, schemaType);
	}

	public static TaxonomySearchServicesQuery childrenNodesForSelectionOfSchemaType(Record record,
																					MetadataSchemaType schemaType,
																					User user) {
		return new TaxonomySearchServicesQuery(user, record, null, schemaType);
	}

	public static TaxonomySearchServicesQuery rootNodesOfTaxonomyForDisplayingClassifiedRecords(Taxonomy taxonomy,
																								User user) {
		return new TaxonomySearchServicesQuery(user, null, taxonomy, null);
	}

	public static TaxonomySearchServicesQuery childrenNodesForDisplayingClassifiedRecords(Record record,
																						  User user) {
		return new TaxonomySearchServicesQuery(user, record, null, null);
	}
}
