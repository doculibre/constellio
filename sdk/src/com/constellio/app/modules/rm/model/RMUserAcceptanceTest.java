package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMUserAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	UserServices userServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();

	}

	private UserCredential newJackBauerUserCredential() {
		return createUserCredential("jack.bauer", "Jack", "Bauer", "jack.bauer@constellio.com",
				new ArrayList<String>(), asList(zeCollection), UserCredentialStatus.ACTIVE);
	}

	@Test
	public void whenAddUpdatingUserWithoutRolesThenAddDefaultUserRole()
			throws Exception {

		userServices.addUpdateUserCredential(newJackBauerUserCredential());
		assertThat(jackBauerInZeCollection().getAllRoles()).containsOnly(RMRoles.USER);

		recordServices.update(jackBauerInZeCollection().setUserRoles(asList(RMRoles.MANAGER)));
		assertThat(jackBauerInZeCollection().getAllRoles()).containsOnly(RMRoles.MANAGER);

		recordServices.update(jackBauerInZeCollection().setUserRoles(new ArrayList<String>()));
		assertThat(jackBauerInZeCollection().getAllRoles()).containsOnly(RMRoles.USER);
	}

	private User jackBauerInZeCollection() {
		return userServices.getUserInCollection("jack.bauer", zeCollection);
	}
}
