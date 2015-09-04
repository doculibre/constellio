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
package com.constellio.model.services.emails;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailTemplatesManagerAcceptanceTest extends ConstellioTest{
    private EmailTemplatesManager manager;

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection()
        );

        manager = getModelLayerFactory().getEmailTemplatesManager();
        manager.initialize();
    }

    @Test
    public void whenAddingNewTemplateThenAddedCorrectly()
            throws Exception {
        String templateText ="lol";
        String templateId = "folderReturnReminder";
        InputStream inputStream = new ByteArrayInputStream(templateText.getBytes());
        manager.addCollectionTemplate(templateId, zeCollection, inputStream);
        String text = manager.getCollectionTemplate(templateId, zeCollection);
        assertThat(text).isEqualTo(templateText);
    }
}
