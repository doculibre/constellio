package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.dev.tools.SecurityUtils;
import com.constellio.sdk.tests.ConstellioTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Charles Blanchette on 2017-02-06.
 */
public class CoreMigrationTo_7_1_AcceptanceTest extends ConstellioTest {

    @Test
    public void startApplicationWithSaveStateWithSpecialAuths()
            throws Exception {
        RecordPopulateServices.LOG_CONTENT_MISSING = false;
        givenTransactionLogIsEnabled();

        getCurrentTestSession().getFactoriesTestFeatures()
                .givenSystemInState(getTestResourceFile("savestateWithSpecialAuths.zip")).withPasswordsReset()
                .withFakeEncryptionServices();

        ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
        reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

        User chuck = getModelLayerFactory().newUserServices().getUserInCollection("chuck", zeCollection);
        User alice = getModelLayerFactory().newUserServices().getUserInCollection("alice", zeCollection);

        MetadataSchemaType schema = rm.folder.schemaType();
        LogicalSearchCondition condition = from(schema).where(rm.folder.title()).isEqualTo("Abeille");
        LogicalSearchQuery query = new LogicalSearchQuery(condition);
        query.filteredWithUser(chuck);

        //assertThat(getModelLayerFactory().newSearchServices().search(query)).hasSize(2);
        List<Folder> resultsFolders = new ArrayList<>();

//        resultsFolders = rm.searchFolders(from(schema).where(rm.folder.));
    }
}
