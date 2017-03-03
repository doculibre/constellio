package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.administration.plan.AvailableSpaceReportModel.AvailableSpaceReportModelNode;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Charles Blanchette on 2017-02-20.
 */
public class AvailableSpaceReportPresenter {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AvailableSpaceReportPresenter.class);

    private String collection;
    private ModelLayerFactory modelLayerFactory;
    private TaxonomiesSearchOptions searchOptions;
    private TaxonomiesSearchServices taxonomiesSearchServices;
    private SearchServices searchServices;
    private RMSchemasRecordsServices rm;
    private ConceptNodesTaxonomySearchServices conceptNodesTaxonomySearchServices;
    private MetadataSchemaTypes types;
    private boolean showFullSpaces;

    public AvailableSpaceReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
        this.collection = collection;
        this.modelLayerFactory = modelLayerFactory;
    }

    public AvailableSpaceReportPresenter(String collection, ModelLayerFactory modelLayerFactory, boolean showFullSpaces) {
        this.collection = collection;
        this.modelLayerFactory = modelLayerFactory;
        this.showFullSpaces = showFullSpaces;
    }

    public AvailableSpaceReportModel build() {
        init();

        AvailableSpaceReportModel model = new AvailableSpaceReportModel();
        model.setShowFullSpaces(showFullSpaces);
        List<Record> rootStorageSpaces = conceptNodesTaxonomySearchServices
                .getRootConcept(collection, RMTaxonomies.STORAGES, searchOptions.setRows(10000));

        if (rootStorageSpaces != null) {
            for (Record rootRecord : rootStorageSpaces) {
                AvailableSpaceReportModelNode parent = new AvailableSpaceReportModelNode();
                StorageSpace storageSpace = new StorageSpace(rootRecord, types);
                parent.setCode(rootRecord.getSchemaCode()).setTitle(rootRecord.getTitle()).setAvailableSpace(storageSpace.getAvailableSize() != null ? storageSpace.getAvailableSize() : 0);
                List<Record> childStorageSpaces = conceptNodesTaxonomySearchServices.getChildConcept(rootRecord, searchOptions.setRows(10000));
                if (childStorageSpaces != null) {
                    createChildRow(parent, childStorageSpaces);
                }
                model.getRootNodes().addAll(Arrays.asList(parent));
            }
        }
        return model;
    }

    private void createChildRow(AvailableSpaceReportModelNode parent, List<Record> childStorageSpaces) {
        for (Record childRecord : childStorageSpaces) {
            AvailableSpaceReportModelNode child = new AvailableSpaceReportModelNode();
            StorageSpace storageSpace = new StorageSpace(childRecord, types);
            child.setCode(childRecord.getSchemaCode()).setTitle(childRecord.getTitle()).setAvailableSpace(storageSpace.getAvailableSize() != null ? storageSpace.getAvailableSize() : 0);
            List<Record> subChildStorageSpaces = conceptNodesTaxonomySearchServices.getChildConcept(childRecord, searchOptions.setRows(10000));
            if (subChildStorageSpaces != null) {
                createChildRow(child, subChildStorageSpaces);
            }

            LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(rm.containerRecord.storageSpace()).isEqualTo(storageSpace);
            LogicalSearchQuery query = new LogicalSearchQuery(condition);
            List<ContainerRecord> containerRecords = rm.searchContainerRecords(query);
            if (containerRecords != null) {
                createContainerRecordRow(child, containerRecords);
            }
            parent.getChildrenNodes().add(child);
        }
    }

    private void createContainerRecordRow(AvailableSpaceReportModelNode parent, List<ContainerRecord> containerRecords) {
        for (ContainerRecord boite : containerRecords) {
            AvailableSpaceReportModelNode childBox = new AvailableSpaceReportModelNode();
            childBox.setTitle(boite.getTitle()).setCode(boite.getId()).setAvailableSpace(boite.getAvailableSize() != null ? boite.getAvailableSize() : 0);
            parent.getChildrenNodes().add(childBox);
        }
    }

    private void init() {
        types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
        searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
        taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
        rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
        searchServices = modelLayerFactory.newSearchServices();
        conceptNodesTaxonomySearchServices = new ConceptNodesTaxonomySearchServices(modelLayerFactory);
    }

    private List<AvailableSpaceReportModelNode> getCategoriesForRecord(Record record) {
        List<AvailableSpaceReportModelNode> modelCategories = new ArrayList<>();

        List<TaxonomySearchRecord> children = taxonomiesSearchServices.getLinkableChildConcept(User.GOD, record,
                RMTaxonomies.STORAGES, StorageSpace.SCHEMA_TYPE, searchOptions);

        if (children != null) {
            for (TaxonomySearchRecord child : children) {
                if (child != null) {
                    try {
                        Record childRecord = child.getRecord();
                        if (childRecord != null) {
                            Category recordCategory = new Category(childRecord, types);

                            if (recordCategory != null) {
                                AvailableSpaceReportModelNode modelNode = new AvailableSpaceReportModelNode();

                                String categoryCode = StringUtils.defaultString(recordCategory.getCode());
                                modelNode.setCode(categoryCode);

                                String categoryTitle = StringUtils.defaultString(recordCategory.getTitle());
                                modelNode.setTitle(categoryTitle);

                                Record childChildRecord = child.getRecord();

                                modelNode.setChildrenNodes(getCategoriesForRecord(childChildRecord));

                                modelCategories.add(modelNode);
                            }
                        }
                    } catch (Exception e) {
                        // throw new RuntimeException(e);
                        LOGGER.info("This is not a space. It's a " + child.getRecord().getSchemaCode());
                    }
                }
            }
        }
        return modelCategories;
    }

    public FoldersLocator getFoldersLocator() {
        return modelLayerFactory.getFoldersLocator();
    }
}
