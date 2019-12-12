package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.administration.plan.AvailableSpaceReportModel.AvailableSpaceReportModelNode;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

/**
 * Created by Charles Blanchette on 2017-02-20.
 */
public class AvailableSpaceReportPresenter {

	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices taxonomiesSearchServices;
	private SearchServices searchServices;
	private RMSchemasRecordsServices rm;
	private RecordHierarchyServices recordHierarchyServices;
	private MetadataSchemaTypes types;
	private boolean showFullSpaces;

	public AvailableSpaceReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public AvailableSpaceReportPresenter(String collection, ModelLayerFactory modelLayerFactory,
										 boolean showFullSpaces) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.showFullSpaces = showFullSpaces;
	}

	public AvailableSpaceReportModel build() {
		init();

		AvailableSpaceReportModel model = new AvailableSpaceReportModel();
		model.setShowFullSpaces(showFullSpaces);
		List<Record> rootStorageSpaces = recordHierarchyServices.getRootConcept(collection, RMTaxonomies.STORAGES, searchOptions.setRows(10000));
		searchOptions.setReturnedMetadatasFilter(searchOptions.getReturnedMetadatasFilter()
				.withIncludedMetadatas(rm.storageSpace.capacity(), rm.storageSpace.availableSize()));

		if (rootStorageSpaces != null) {
			for (Record rootRecord : rootStorageSpaces) {
				AvailableSpaceReportModelNode parent = new AvailableSpaceReportModelNode();
				StorageSpace storageSpace = new StorageSpace(rootRecord, types);
				parent.setCode(storageSpace.getCode()).setTitle(rootRecord.getTitle()).setImage("etagere")
						.setCapacity(storageSpace.getCapacity() != null ? storageSpace.getCapacity() : 0)
						.setAvailableSpace(storageSpace.getAvailableSize() != null ? storageSpace.getAvailableSize() : 0);
				List<Record> childStorageSpaces = recordHierarchyServices.getChildConcept(rootRecord, searchOptions.setRows(10000));
				if (childStorageSpaces != null) {
					createChildRow(parent, childStorageSpaces);
				}

				LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace);
				LogicalSearchQuery query = new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
				List<ContainerRecord> containerRecords = rm.searchContainerRecords(query);
				if (containerRecords != null) {
					createContainerRecordRow(parent, containerRecords);
				}

				if (showFullSpaces || !(parent.getChildrenNodes() == null || parent.getChildrenNodes().isEmpty()) || parent.getAvailableSpace() > 0.0) {
					model.getRootNodes().add(parent);
				}
			}
		}

		return model;
	}

	private void createChildRow(AvailableSpaceReportModelNode parent, List<Record> childStorageSpaces) {
		for (Record childRecord : childStorageSpaces) {
			if (StorageSpace.SCHEMA_TYPE.equals(childRecord.getTypeCode())) {
				AvailableSpaceReportModelNode child = new AvailableSpaceReportModelNode();
				StorageSpace storageSpace = new StorageSpace(childRecord, types);
				child.setCode(storageSpace.getCode()).setImage("etagere")
						.setCapacity(storageSpace.getCapacity() != null ? storageSpace.getCapacity() : 0)
						.setTitle(childRecord.getTitle()).setAvailableSpace(storageSpace.getAvailableSize() != null ? storageSpace.getAvailableSize() : 0);
				List<Record> subChildStorageSpaces = recordHierarchyServices.getChildConcept(childRecord, searchOptions.setRows(10000));
				if (subChildStorageSpaces != null) {
					createChildRow(child, subChildStorageSpaces);
				}

				LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace);
				LogicalSearchQuery query = new LogicalSearchQuery(condition).sortAsc(Schemas.TITLE);
				List<ContainerRecord> containerRecords = rm.searchContainerRecords(query);
				if (containerRecords != null) {
					createContainerRecordRow(child, containerRecords);
				}

				if (showFullSpaces || !(child.getChildrenNodes() == null || child.getChildrenNodes().isEmpty()) || child.getAvailableSpace() > 0.0) {
					parent.getChildrenNodes().add(child);
				}
			}
		}
	}

	private void createContainerRecordRow(AvailableSpaceReportModelNode parent,
										  List<ContainerRecord> containerRecords) {
		for (ContainerRecord boite : containerRecords) {
			AvailableSpaceReportModelNode childBox = new AvailableSpaceReportModelNode();
			childBox.setTitle(boite.getTitle()).setCode(boite.getTitle()).setImage("boite")
					.setCapacity(boite.getCapacity() != null ? boite.getCapacity() : 0)
					.setAvailableSpace(boite.getAvailableSize() != null ? boite.getAvailableSize() : 0);

			if (showFullSpaces || childBox.getAvailableSpace() > 0.0) {
				parent.getChildrenNodes().add(childBox);
			}
		}
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		Set<String> acceptedFields = new HashSet<>(asList(rm.storageSpace.capacity().getDataStoreCode(), rm.storageSpace.availableSize().getDataStoreCode()));
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.allAndWithIncludedFields(acceptedFields));
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		searchServices = modelLayerFactory.newSearchServices();
		recordHierarchyServices = new RecordHierarchyServices(modelLayerFactory);
	}


	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}
