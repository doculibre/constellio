package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServicesException.CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.constellio.model.services.taxonomies.TaxonomyVisitingStatus.CONTINUE;
import static com.constellio.model.services.taxonomies.TaxonomyVisitingStatus.CONTINUE_NO_DEEPER_IN_THIS_NODE;
import static com.constellio.model.services.taxonomies.TaxonomyVisitingStatus.STOP;

public class CacheBasedTaxonomyVisitingServices {

	ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager schemasManager;
	RecordsCaches caches;

	public CacheBasedTaxonomyVisitingServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.caches = modelLayerFactory.getRecordsCaches();
	}

	public static class CacheBasedTaxonomyVisitingServicesParams {

		@Getter
		@Setter
		Comparator<Record> optionalChildrenComparator;

	}

	@AllArgsConstructor
	public static class VisitedTaxonomyRecord {

		@Getter
		Record record;

		@Getter
		int level;

		@Getter
		Stack<Record> ancestors;

	}

	public Stream<Record> streamAllIn(Supplier<Record> record)
			throws CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded {
		List<Record> records = new ArrayList<>();
		TaxonomyVisitor visitor = new TaxonomyVisitor() {

			@Override
			public TaxonomyVisitingStatus visit(VisitedTaxonomyRecord item) {
				records.add(item.getRecord());
				return CONTINUE;
			}
		};
		visit(record, visitor, new CacheBasedTaxonomyVisitingServicesParams());
		return records.stream();
	}

	public TaxonomyVisitingStatus visit(Supplier<Record> record, TaxonomyVisitor visitor)
			throws CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded {
		return visit(record, visitor, new CacheBasedTaxonomyVisitingServicesParams());
	}

	public TaxonomyVisitingStatus visit(Supplier<Record> record, TaxonomyVisitor visitor,
										CacheBasedTaxonomyVisitingServicesParams params)
			throws CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded {

		if (!caches.areSummaryCachesInitialized()) {
			throw new CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded();
		}

		return visitWithStatus(new VisitedTaxonomyRecord(record.get(), 0, new Stack<>()), visitor, params);
	}

	public TaxonomyVisitingStatus visitWithStatus(VisitedTaxonomyRecord item, TaxonomyVisitor visitor,
												  CacheBasedTaxonomyVisitingServicesParams params) {

		TaxonomyVisitingStatus visitingStatus = visitor.visit(item);

		if (visitingStatus == STOP || visitingStatus == CONTINUE_NO_DEEPER_IN_THIS_NODE) {
			return visitingStatus;
		} else {
			MetadataSchemaType schemaType = schemasManager.getSchemaTypeOf(item.record);
			for (MetadataSchemaType hierarchyType : schemaType.getSchemaTypes().getClassifiedSchemaTypesIncludingSelfIn(schemaType.getCode())) {

				if (hierarchyType.getCacheType().hasPermanentCache()) {

					for (Metadata metadata : hierarchyType.getAllParentOrTaxonomyReferencesTo(schemaType.getCode())) {
						if (metadata.getDataEntry().getType() == DataEntryType.MANUAL) {

							Iterator<Record> iterator = caches.getRecordsByIndexedMetadata(hierarchyType, metadata, item.getRecord().getId()).iterator();

							if (params.optionalChildrenComparator != null) {
								List<Record> allRecords = IteratorUtils.toList(iterator);
								allRecords.sort(params.optionalChildrenComparator);
								iterator = allRecords.iterator();
							}

							item.ancestors.add(item.record);
							while (iterator.hasNext()) {
								VisitedTaxonomyRecord childItem = new VisitedTaxonomyRecord(iterator.next(), item.level + 1, item.ancestors);
								TaxonomyVisitingStatus childVisitStatus = visitWithStatus(childItem, visitor, params);
								if (childVisitStatus == STOP) {
									return STOP;
								}
							}
							item.ancestors.pop();
						}

					}
				}

			}

			return CONTINUE;
		}

	}
}
