package com.constellio.dev;

import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListFoldersCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

/**
 * Created by Constelio on 2016-10-31.
 */
public class AdministrativeUnitCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministrativeUnitCleaner.class);

    static public void cleanAllAdministrativeUnits(String collection, AppLayerFactory appLayerFactory) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        List<AdministrativeUnit> administrativeUnitList = rm.searchAdministrativeUnits(ALL);
        for (AdministrativeUnit administrativeUnit : administrativeUnitList) {
            cleanAdministrativeUnit(collection, administrativeUnit, appLayerFactory);
        }
    }

    static public void cleanAdministrativeUnit(String collection, AdministrativeUnit administrativeUnit,
                                                AppLayerFactory appLayerFactory) {
        cleanFoldersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
        cleanContainersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
    }

    static public void cleanAdministrativeUnit(String collection, String administrativeUnitID,
                                               AppLayerFactory appLayerFactory) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        AdministrativeUnit administrativeUnit = rm.wrapAdministrativeUnit(searchServices.
                searchSingleResult(from(rm.administrativeUnit.schema()).where(Schemas.IDENTIFIER)
                        .isEqualTo(administrativeUnitID)));
        cleanFoldersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
        cleanContainersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
    }

    static private void cleanFoldersInAdministrativeUnitRecursively(String collection,
                                                                    AdministrativeUnit administrativeUnit,
                                                                    AppLayerFactory appLayerFactory) {

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

        LogicalSearchQuery queryDocument = new LogicalSearchQuery().setCondition(from(rm.document.schema())
                .where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH);
        LogicalSearchQuery queryFolder = new LogicalSearchQuery().setCondition(from(rm.folder.schema())
                .where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH);



        SearchResponseIterator<Record> documentIterator = searchServices.recordsIterator(queryDocument);
        SearchResponseIterator<Record> folderIterator = searchServices.recordsIterator(queryFolder);

        Set<String> recordIDs = new HashSet<>();
        while (documentIterator.hasNext()) {
            Record document = documentIterator.next();
            List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
                    where(rm.decommissioningList.documents()).isContaining(asList(document.getId())));
            for(DecommissioningList decommissioningList: decommissioningLists) {
                ArrayList<String> decommissioningListDocuments = new ArrayList<>(decommissioningList.getDocuments());
                decommissioningListDocuments.remove(document.getId());
                decommissioningList.setDocuments(decommissioningListDocuments);
                try {
                    recordServices.update(decommissioningList.getWrappedRecord());
                } catch (RecordServicesException e) {
                    LOGGER.info("Could not unlink document from DecommissioningList");
                }
            }
            if(recordIDs.add(document.getId())) {
                try {
                    recordServices.physicallyDeleteNoMatterTheStatus(document, User.GOD, new RecordPhysicalDeleteOptions());
                } catch (Exception e) {
                    LOGGER.info("Could not delete document " + document.getId());
                }
            }
        }
        while (folderIterator.hasNext()) {
            Record folder = folderIterator.next();
            List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
                    where(rm.decommissioningList.folders()).isContaining(asList(folder.getId())));
            for(DecommissioningList decommissioningList: decommissioningLists) {
                decommissioningList.removeFolderDetail(folder.getId());

                try {
                    recordServices.update(decommissioningList.getWrappedRecord());
                } catch (RecordServicesException e) {
                    LOGGER.info("Could not unlink folder from DecommissioningList");
                }
            }
            if(recordIDs.add(folder.getId())) {
                try {
                    recordServices.physicallyDeleteNoMatterTheStatus(folder, User.GOD, new RecordPhysicalDeleteOptions());
                } catch (Exception e) {
                    LOGGER.info("Could not delete folder " + folder.getId());
                }
            }
        }
    }

    static private void cleanContainersInAdministrativeUnitRecursively(String collection,
                                                                       AdministrativeUnit administrativeUnit,
                                                                       AppLayerFactory appLayerFactory) {

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(rm.containerRecord.schema())
                .where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH);

        SearchResponseIterator<Record> containerIterator = searchServices.recordsIterator(query);
        Set<String> recordIDs = new HashSet<>();

        while(containerIterator.hasNext()) {
            Record container = containerIterator.next();
            List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
                    where(rm.decommissioningList.containers()).isContaining(asList(container.getId())));
            for(DecommissioningList decommissioningList: decommissioningLists) {
                decommissioningList.removeContainerDetail(container.getId());

                try {
                    recordServices.update(decommissioningList.getWrappedRecord());
                } catch (RecordServicesException e) {
                    LOGGER.info("Could not unlink container from DecommissioningList");
                }
            }
            if(recordIDs.add(container.getId())) {
                try {
                    recordServices.physicallyDeleteNoMatterTheStatus(container, User.GOD, new RecordPhysicalDeleteOptions());
                } catch (Exception e) {
                    LOGGER.info("Could not delete container " + container.getId());
                }
            }
        }
    }
}
