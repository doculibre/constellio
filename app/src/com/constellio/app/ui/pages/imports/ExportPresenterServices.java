package com.constellio.app.ui.pages.imports;

import com.constellio.app.api.extensions.ExportServicesExtension;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.records.RecordExportOptions;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.RecordsOfSchemaTypesIterator;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

public class ExportPresenterServices {

	String collection;

	AppLayerFactory appLayerFactory;

	RecordServices recordServices;

	SearchServices searchServices;

	public static final String RECORDS_EXPORT_TEMP_DDV = "ddv";

	public ExportPresenterServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}


	public RecordExportOptions buildOptionsForExportingAdministrativeUnitsAndItsContent(boolean isSameCollection,
																						List<String> unitIds,
																						boolean includeAuthorizations) {
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);

		List<String> paths = new ArrayList<>();
		for (String unit : unitIds) {
			paths.add((String) ((List) recordServices.getDocumentById(unit).get(Schemas.PATH)).get(0));
		}

		MetadataSchemaType decommissioningListSchemaType = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchemaType(DecommissioningList.SCHEMA_TYPE);


		SearchResponseIterator<Record> recordsIterator = searchServices.recordsIterator(
				fromAllSchemasIn(collection)
						.where(Schemas.PATH).isStartingWithTextFromAny(paths)
						.orWhere(decommissioningListSchemaType.getDefaultSchema().get(DecommissioningList.ADMINISTRATIVE_UNIT)).isIn(unitIds)
		);


		options.setRecordsToExportIterator(new LazyIterator<Record>() {
			@Override
			protected Record getNextOrNull() {

				while (recordsIterator.hasNext()) {
					Record next = recordsIterator.next();
					if (!next.getSchemaCode().startsWith(Task.SCHEMA_TYPE + "_") &&
						!next.getSchemaCode().startsWith("workflowModel_")) {
						return next;
					}
				}

				return null;
			}
		});
		options.setIncludeAuthorizations(includeAuthorizations);
		return options;
	}


	public RecordExportOptions buildOptionsForExportingTools(boolean isSameCollection, boolean includeAuthorizations,
															 AppLayerCollectionExtensions appCollectionExtentions) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);
		List<Taxonomy> enabledTaxonomies = new ArrayList<>(modelLayerFactory.getTaxonomiesManager().getEnabledTaxonomies(collection));
		removeUnwantedTaxonomiesForExportation(enabledTaxonomies, appCollectionExtentions);
		List<String> exportedSchemaTypes = getExportedSchemaTypes();
		for (Taxonomy taxonomy : enabledTaxonomies) {
			List<String> linkedSchemaTypes = taxonomy.getSchemaTypes();
			for (String schemaType : linkedSchemaTypes) {
				if (!exportedSchemaTypes.contains(schemaType)) {
					exportedSchemaTypes.add(schemaType);
				}
			}
		}

		addValueListsSchemaTypes(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection), exportedSchemaTypes);

		options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(modelLayerFactory, collection, exportedSchemaTypes));
		options.setIncludeAuthorizations(includeAuthorizations);
		return options;
	}

	private List<String> getExportedSchemaTypes() {
		List<String> exportedSchemaTypes = new ArrayList<>(asList(AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE,
				RetentionRule.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE, LegalReference.SCHEMA_TYPE,
				LegalRequirement.SCHEMA_TYPE, LegalRequirementReference.SCHEMA_TYPE,
				RetentionRuleDocumentType.SCHEMA_TYPE));

		List<ExportServicesExtension> extensions = appLayerFactory.getExtensions().forCollection(collection)
				.exportServicesExtensions.getExtensions();
		for (ExportServicesExtension extension : extensions) {
			exportedSchemaTypes.addAll(extension.getExportedSchemaTypes());
		}

		return exportedSchemaTypes;
	}

	private void removeUnwantedTaxonomiesForExportation(List<Taxonomy> taxonomies,
														AppLayerCollectionExtensions appCollectionExtentions) {
		if (taxonomies != null) {
			List<String> unwantedTaxonomies = appCollectionExtentions.getUnwantedTaxonomiesForExportation();
			Iterator<Taxonomy> iterator = taxonomies.iterator();
			while (iterator.hasNext()) {
				if (unwantedTaxonomies.contains(iterator.next().getCode())) {
					iterator.remove();
				}
			}
		}
	}


	private void addValueListsSchemaTypes(MetadataSchemaTypes metadataSchemaTypes, List<String> schemaTypeList) {
		// Code
		// Itération sur les type de schema.
		for (MetadataSchemaType metadata : metadataSchemaTypes.getSchemaTypes()) {
			if (metadata.getCode().toLowerCase().startsWith(RECORDS_EXPORT_TEMP_DDV)) {
				if (!isSchemaCodePresent(schemaTypeList, metadata.getCode())) {
					schemaTypeList.add(metadata.getCode());
				}
			}
		}
	}

	private static boolean isSchemaCodePresent(List<String> schemaCodeList, String schemaCode) {
		boolean isSchemaCodePresent = false;

		for (String currentSchemaCode : schemaCodeList) {
			isSchemaCodePresent = schemaCode.equals(currentSchemaCode);
			if (isSchemaCodePresent) {
				break;
			}
		}

		return isSchemaCodePresent;
	}

}
