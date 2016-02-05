package com.constellio.app.services.migrations.scripts;

import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreMigrationTo_5_0_7AcceptanceTest extends ConstellioTest {
    @Test
    public void whenInCurrentVersionThenReportSchema_TypeAndLinesCountRequired() {
        MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

        assertThat(metadataSchemaTypes.getSchema(Report.DEFAULT_SCHEMA).getMetadata(Report.SCHEMA_TYPE_CODE)
                .isDefaultRequirement()).isTrue();
        assertThat(metadataSchemaTypes.getSchema(Report.DEFAULT_SCHEMA).getMetadata(Report.LINES_COUNT)
                .isDefaultRequirement()).isTrue();
    }

    @Before
    public void setUp()
            throws Exception {
        givenCollection(zeCollection);
    }
}
