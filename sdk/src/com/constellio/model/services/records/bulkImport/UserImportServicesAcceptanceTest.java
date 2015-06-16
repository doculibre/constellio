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
package com.constellio.model.services.records.bulkImport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class UserImportServicesAcceptanceTest extends ConstellioTest {
    RMSchemasRecordsServices rm;
    RMTestRecords records;
    LocalDateTime shishOClock = new LocalDateTime().minusHours(1);

    BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
    UserImportServices importServices;
    SearchServices searchServices;
    UserServices userServices;
    User admin;

    @Before
    public void setUp()
            throws Exception {

        givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

        progressionListener = new LoggerBulkImportProgressionListener();
        importServices = new UserImportServices(getModelLayerFactory(), 100);
        searchServices = getModelLayerFactory().newSearchServices();
        userServices = getModelLayerFactory().newUserServices();

        admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

        rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
        records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
    }

    @Test
    public void whenImportingUsersXMLFileThenImportedCorrectly()
            throws Exception {
        File usersFile = getTestResourceFile("user.xml");
        importAndValidate(XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile));
    }

    private void importAndValidate(ImportDataProvider importDataProvider) {
        List<String> collections = new ArrayList<>();
        collections.add(zeCollection);
        BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
        assertThat(results.getImportErrors().size()).isEqualTo(0);
        validateAlice();

        validateBob();
    }

    @Test
    public void whenImportingUserWithInvalidGroupThenImportOtherUsersAndGiveAdequateErrorMessage()
            throws Exception {
        File usersFile = getTestResourceFile("userAliceWithInvalidGroup.xml");
        importAndValidateWhenUserWithInvalidGroup(XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile));
    }

    private void importAndValidateWhenUserWithInvalidGroup(ImportDataProvider importDataProvider) {
        List<String> collections = new ArrayList<>();
        collections.add(zeCollection);
        BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
        assertThat(results.getImportErrors().size()).isEqualTo(1);
        ImportError mariImportError = results.getImportErrors().get(0);
        assertThat(mariImportError.getInvalidElementId()).isEqualTo("mari");
        assertThat(mariImportError.getCompleteErrorMessage()).contains("Invalid group");//$("UserServicesRuntimeException_InvalidGroup"));
        assertThat(mariImportError.getCompleteErrorMessage()).contains($("legends1"));

        try{
            userServices.getUser("mari");
            fail("mari shouldn't exist");
        }catch(UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e){
        }

        validateBob();
    }

    //@Test
    public void whenImportingUserWithExistingEmailThenImportOtherUsersAndGiveAdequateErrorMessage()
            throws Exception {
        File usersFile = getTestResourceFile("userMariWithAliceEmail.xml");
        importAndValidateWhenMariWithAliceEmail(XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile));
    }

    private void importAndValidateWhenMariWithAliceEmail(ImportDataProvider importDataProvider) {
        List<String> collections = new ArrayList<>();
        collections.add(zeCollection);
        BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
        assertThat(results.getImportErrors().size()).isEqualTo(1);
        ImportError mariImportError = results.getImportErrors().get(0);
        assertThat(mariImportError.getInvalidElementId()).isEqualTo("mari");
        assertThat(mariImportError.getErrorMessage()).contains("Courriel");//$("ExistingEmail"));

        try{
            userServices.getUser("mari");
            //TODO Francis ? do not add user with existing email
            //fail("mari shouldn't exist");
        }catch(UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e){
        }

        validateBob();
    }

    private void validateBob() {
        UserCredential bob = userServices.getUser("bob");
        assertThat(bob.getFirstName()).isEqualTo("Bob 'Elvis'");
        assertThat(bob.getLastName()).isEqualTo("Gratton");
        assertThat(bob.getEmail()).isEqualTo("bob@doculibre.com");
        assertThat(bob.isSystemAdmin()).isEqualTo(true);
        assertThat(bob.getGlobalGroups().size()).isEqualTo(0);
        assertThat(bob.getCollections()).contains(zeCollection);
        assertThat(bob.getCollections().size()).isEqualTo(1);
    }

    private void validateAlice() {
        UserCredential alice = userServices.getUser("alice");
        assertThat(alice.getFirstName()).isEqualTo("Alice");
        assertThat(alice.getLastName()).isEqualTo("Wonderland");
        assertThat(alice.getEmail()).isEqualTo("alice@doculibre.com");
        assertThat(alice.isSystemAdmin()).isEqualTo(false);
        assertThat(alice.getGlobalGroups()).contains("legends", "heroes");
        assertThat(alice.getGlobalGroups().size()).isEqualTo(2);
        assertThat(alice.getCollections()).contains(zeCollection);
        assertThat(alice.getCollections().size()).isEqualTo(1);
    }
}
