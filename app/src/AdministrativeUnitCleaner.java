import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

/**
 * Created by Constelio on 2016-10-31.
 */
public class AdministrativeUnitCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministrativeUnitCleaner.class);

    static public void clean(String collection, AppLayerFactory appLayerFactory) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        List<AdministrativeUnit> administrativeUnitList = rm.searchAdministrativeUnits(ALL);
        for (AdministrativeUnit administrativeUnit : administrativeUnitList) {
            cleanAdministrativeUnit(collection, administrativeUnit, appLayerFactory);
        }
    }

    private static void cleanAdministrativeUnit(String collection, AdministrativeUnit administrativeUnit,
                                                AppLayerFactory appLayerFactory) {
        cleanFoldersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
        cleanContainersInAdministrativeUnitRecursively(collection, administrativeUnit, appLayerFactory);
    }

    private static void cleanFoldersInAdministrativeUnitRecursively(String collection,
                                                                    AdministrativeUnit administrativeUnit,
                                                                    AppLayerFactory appLayerFactory) {

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        LogicalSearchQuery query = new LogicalSearchQuery().setCondition(where(Schemas.PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PATH);

        SearchResponseIterator<Record> folderIterator = searchServices.recordsIterator(query);

        while(folderIterator.hasNext()) {
            Record folder = folderIterator.next();
            try {
                recordServices.physicallyDeleteNoMatterTheStatus(folder, User.GOD, new RecordPhysicalDeleteOptions());
            } catch (Exception e) {
                LOGGER.info("Could not delete folder " + folder.getTitle());
            }
        }
    }

    private static void cleanContainersInAdministrativeUnitRecursively(String collection,
                                                                       AdministrativeUnit administrativeUnit,
                                                                       AppLayerFactory appLayerFactory) {

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        LogicalSearchQuery query = new LogicalSearchQuery().setCondition(where(Schemas.PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PATH);

        SearchResponseIterator<Record> containerIterator = searchServices.recordsIterator(query);

        while(containerIterator.hasNext()) {
            Record container = containerIterator.next();
            try {
                recordServices.physicallyDeleteNoMatterTheStatus(container, User.GOD, new RecordPhysicalDeleteOptions());
            } catch (Exception e) {
                LOGGER.info("Could not delete container " + container.getTitle());
            }
        }
    }
}
