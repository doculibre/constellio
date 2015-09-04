/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
