package com.constellio.app.modules.rm.reports.administration.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_User;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportPresenter;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class AdministrativeUnitReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	RMTestRecords records = new RMTestRecords(zeCollection);
	AdministrativeUnitReportPresenter presenter;
	AdministrativeUnitReportModel_User referenceUserAdmin;
	AdministrativeUnitReportModel_User referenceUserBob;
	AdministrativeUnitReportModel_User referenceUserCharles;
	AdministrativeUnitReportModel_User referenceUserGandalf;
	AdministrativeUnitReportModel_User referenceUserDakota;
	AdministrativeUnitReportModel_User referenceUserEdouard;
	AdministrativeUnitReportModel_User referenceUserChuck;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		presenter = new AdministrativeUnitReportPresenter(zeCollection, getModelLayerFactory());

		referenceUserAdmin = new AdministrativeUnitReportModel_User();
		referenceUserAdmin.setFirstName("System");
		referenceUserAdmin.setLastName("Admin");
		referenceUserAdmin.setUserName("admin");
		referenceUserAdmin.setEmail("admin@organization.com");

		referenceUserBob = new AdministrativeUnitReportModel_User();
		referenceUserBob.setFirstName("Bob 'Elvis'");
		referenceUserBob.setLastName("Gratton");
		referenceUserBob.setUserName("bob");
		referenceUserBob.setEmail("bob@doculibre.com");

		referenceUserCharles = new AdministrativeUnitReportModel_User();
		referenceUserCharles.setFirstName("Charles-François");
		referenceUserCharles.setLastName("Xavier");
		referenceUserCharles.setUserName("charles");
		referenceUserCharles.setEmail("charles@doculibre.com");

		referenceUserGandalf = new AdministrativeUnitReportModel_User();
		referenceUserGandalf.setFirstName("Gandalf");
		referenceUserGandalf.setLastName("Leblanc");
		referenceUserGandalf.setUserName("gandalf");
		referenceUserGandalf.setEmail("gandalf@doculibre.com");

		referenceUserDakota = new AdministrativeUnitReportModel_User();
		referenceUserDakota.setFirstName("Dakota");
		referenceUserDakota.setLastName("L'Indien");
		referenceUserDakota.setUserName("dakota");
		referenceUserDakota.setEmail("dakota@doculibre.com");

		referenceUserEdouard = new AdministrativeUnitReportModel_User();
		referenceUserEdouard.setFirstName("Edouard");
		referenceUserEdouard.setLastName("Lechat");
		referenceUserEdouard.setUserName("edouard");
		referenceUserEdouard.setEmail("edouard@doculibre.com");

		referenceUserChuck = new AdministrativeUnitReportModel_User();
		referenceUserChuck.setFirstName("Chuck");
		referenceUserChuck.setLastName("Norris");
		referenceUserChuck.setUserName("chuck");
		referenceUserChuck.setEmail("chuck@doculibre.com");

	}

	@Test
	public void givenAdministrativeUnitsTreeWhenBuildingModelWithUsersThenGetAppropriateModel() {
		AdministrativeUnitReportModel model = presenter.build();
		assertThat(model.getAdministrativeUnits()).isNotNull();
		assertThat(model.getAdministrativeUnits().size()).isEqualTo(3);

		testAdministrativeUnit10(model);

		testAdministrativeUnit10a(model);

		testAdministrativeUnit11(model);

		testAdministrativeUnit11b(model);

		testAdministrativeUnit12(model);

		testAdministrativeUnit12b(model);

		testAdministrativeUnit12c(model);

		testAdministrativeUnit20(model);

		testAdministrativeUnit20d(model);

		testAdministrativeUnit20e(model);

		testAdministrativeUnit30(model);

		testAdministrativeUnit30c(model);

		build(new AdministrativeUnitReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	@Test
	public void givenAdministrativeUnitsTreeWhenBuildingModelWithoutUsersThenGetAppropriateModel() {
		presenter = new AdministrativeUnitReportPresenter(zeCollection, getModelLayerFactory(), false);
		AdministrativeUnitReportModel model = presenter.build();
		assertThat(model.getAdministrativeUnits()).isNotNull();

		build(new AdministrativeUnitReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	private void testAdministrativeUnit10(AdministrativeUnitReportModel model) {
		assertThat(model.getAdministrativeUnits()).extracting("code").containsOnlyOnce("10");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		assertThat(administrativeUnit10.getChildAdministrativeUnits()).hasSize(3);

		assertThat(administrativeUnit10.getLabel()).isEqualTo("Unité 10");
		assertThat(administrativeUnit10.getDescription()).isEqualTo("Ze ultimate unit 10");

		List<AdministrativeUnitReportModel_User> usersUnit10 = administrativeUnit10.getUsers();
		assertThat(usersUnit10.size()).isEqualTo(5);

		assertThat(usersUnit10).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit10, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit10).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit10, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit10).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit10, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit10).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit10, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit10).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit10, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);

	}

	private void testAdministrativeUnit10a(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		assertThat(administrativeUnit10.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("10A");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10A = getUnit(administrativeUnit10, "10A");
		assertThat(administrativeUnit10A.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit10A.getLabel()).isEqualTo("Unité 10-A");
		assertThat(administrativeUnit10A.getDescription()).isEqualTo("Ze ultimate unit 10A");

		List<AdministrativeUnitReportModel_User> usersUnit10a = administrativeUnit10A.getUsers();
		assertThat(usersUnit10a.size()).isEqualTo(5);

		assertThat(usersUnit10a).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit10a, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit10a).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit10a, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit10a).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit10a, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit10a).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit10a, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit10a).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit10a, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit11(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		assertThat(administrativeUnit10.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("11");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit11 = getUnit(administrativeUnit10, "11");
		assertThat(administrativeUnit11.getChildAdministrativeUnits()).hasSize(1);
		assertThat(administrativeUnit11.getLabel()).isEqualTo("Unité 11");
		assertThat(administrativeUnit11.getDescription()).isEqualTo("Ze ultimate unit 11");

		List<AdministrativeUnitReportModel_User> usersUnit11 = administrativeUnit11.getUsers();
		assertThat(usersUnit11.size()).isEqualTo(6);

		assertThat(usersUnit11).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit11, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit11).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit11, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit11).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit11, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit11).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit11, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit11).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit11, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit11).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit11, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit11b(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit11 = getUnit(administrativeUnit10, "11");
		assertThat(administrativeUnit11.getChildAdministrativeUnits()).hasSize(1);
		assertThat(administrativeUnit11.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("11B");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit11B = getUnit(administrativeUnit11, "11B");
		assertThat(administrativeUnit11B.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit11B.getLabel()).isEqualTo("Unité 11-B");
		assertThat(administrativeUnit11B.getDescription()).isEqualTo("Ze ultimate unit 11B");

		List<AdministrativeUnitReportModel_User> usersUnit11b = administrativeUnit11B.getUsers();
		assertThat(usersUnit11b.size()).isEqualTo(6);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit11b, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit11b, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit11b, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit11b, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit11b, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit11b, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit12(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		assertThat(administrativeUnit10.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("12");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit12 = getUnit(administrativeUnit10, "12");
		assertThat(administrativeUnit12.getChildAdministrativeUnits()).hasSize(2);
		assertThat(administrativeUnit12.getLabel()).isEqualTo("Unité 12");
		assertThat(administrativeUnit12.getDescription()).isEqualTo("Ze ultimate unit 12");

		List<AdministrativeUnitReportModel_User> usersUnit12 = administrativeUnit12.getUsers();
		assertThat(usersUnit12.size()).isEqualTo(6);

		assertThat(usersUnit12).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit12, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit12).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit12, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit12).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit12, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit12).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit12, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit12).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit12, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit12).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit12, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit12b(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit12 = getUnit(administrativeUnit10, "12");
		assertThat(administrativeUnit12.getChildAdministrativeUnits()).hasSize(2);
		assertThat(administrativeUnit12.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("12B");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit12B = getUnit(administrativeUnit12, "12B");
		assertThat(administrativeUnit12B.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit12B.getLabel()).isEqualTo("Unité 12-B");
		assertThat(administrativeUnit12B.getDescription()).isEqualTo("Ze ultimate unit 12B");

		List<AdministrativeUnitReportModel_User> usersUnit11b = administrativeUnit12B.getUsers();
		assertThat(usersUnit11b.size()).isEqualTo(6);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit11b, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit11b, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit11b, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit11b, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit11b, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit11b, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit12c(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit12 = getUnit(administrativeUnit10, "12");
		assertThat(administrativeUnit12.getChildAdministrativeUnits()).hasSize(2);
		assertThat(administrativeUnit12.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("12C");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit12C = getUnit(administrativeUnit12, "12C");
		assertThat(administrativeUnit12C.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit12C.getLabel()).isEqualTo("Unité 12-C");
		assertThat(administrativeUnit12C.getDescription()).isEqualTo("Ze ultimate unit 12C");

		List<AdministrativeUnitReportModel_User> usersUnit11b = administrativeUnit12C.getUsers();
		assertThat(usersUnit11b.size()).isEqualTo(6);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit11b, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit11b, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("charles");
		AdministrativeUnitReportModel_User userCharles = getUser(usersUnit11b, "charles");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit11b, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersUnit11b, "dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(usersUnit11b).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit11b, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit20(AdministrativeUnitReportModel model) {
		assertThat(model.getAdministrativeUnits()).extracting("code").containsOnlyOnce("20");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit20 = getUnit(model, "20");
		assertThat(administrativeUnit20.getChildAdministrativeUnits()).hasSize(2);
		assertThat(administrativeUnit20.getLabel()).isEqualTo("Unité 20");
		assertThat(administrativeUnit20.getDescription()).isEqualTo("Ze ultimate unit 20");

		List<AdministrativeUnitReportModel_User> usersUnit20 = administrativeUnit20.getUsers();
		assertThat(usersUnit20).isEmpty();
	}

	private void testAdministrativeUnit20d(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit20 = getUnit(model, "20");
		assertThat(administrativeUnit20.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("20D");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit20D = getUnit(administrativeUnit20, "20D");
		assertThat(administrativeUnit20D.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit20D.getLabel()).isEqualTo("Unité 20-D");
		assertThat(administrativeUnit20D.getDescription()).isEqualTo("Ze ultimate unit 20D");

		List<AdministrativeUnitReportModel_User> usersUnit20D = administrativeUnit20D.getUsers();
		assertThat(usersUnit20D).isEmpty();

	}

	private void testAdministrativeUnit20e(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit20 = getUnit(model, "20");
		assertThat(administrativeUnit20.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("20E");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit20E = getUnit(administrativeUnit20, "20E");
		assertThat(administrativeUnit20E.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit20E.getLabel()).isEqualTo("Unité 20-E");
		assertThat(administrativeUnit20E.getDescription()).isEqualTo("Ze ultimate unit 20E");

		List<AdministrativeUnitReportModel_User> usersUnit20E = administrativeUnit20E.getUsers();
		assertThat(usersUnit20E).isEmpty();

	}

	private void testAdministrativeUnit30(AdministrativeUnitReportModel model) {
		assertThat(model.getAdministrativeUnits()).extracting("code").containsOnlyOnce("30");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit30 = getUnit(model, "30");
		assertThat(administrativeUnit30.getChildAdministrativeUnits()).hasSize(1);
		assertThat(administrativeUnit30.getLabel()).isEqualTo("Unité 30");
		assertThat(administrativeUnit30.getDescription()).isEqualTo("Ze ultimate unit 30");

		List<AdministrativeUnitReportModel_User> usersUnit30 = administrativeUnit30.getUsers();
		assertThat(usersUnit30.size()).isEqualTo(4);

		assertThat(usersUnit30).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit30, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit30).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit30, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit30).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit30, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit30).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit30, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private void testAdministrativeUnit30c(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit30 = getUnit(model, "30");
		assertThat(administrativeUnit30.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("30C");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit30C = getUnit(administrativeUnit30, "30C");
		assertThat(administrativeUnit30C.getChildAdministrativeUnits()).isEmpty();
		assertThat(administrativeUnit30C.getLabel()).isEqualTo("Unité 30-C");
		assertThat(administrativeUnit30C.getDescription()).isEqualTo("Ze ultimate unit 30C");

		List<AdministrativeUnitReportModel_User> usersUnit30c = administrativeUnit30C.getUsers();
		assertThat(usersUnit30c.size()).isEqualTo(4);

		assertThat(usersUnit30c).extracting("userName").containsOnlyOnce("admin");
		AdministrativeUnitReportModel_User userAdmin = getUser(usersUnit30c, "admin");
		assertThat(userAdmin).isEqualToComparingFieldByField(referenceUserAdmin);

		assertThat(usersUnit30c).extracting("userName").containsOnlyOnce("bob");
		AdministrativeUnitReportModel_User userBob = getUser(usersUnit30c, "bob");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersUnit30c).extracting("userName").containsOnlyOnce("edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersUnit30c, "edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersUnit30c).extracting("userName").containsOnlyOnce("gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(usersUnit30c, "gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);
	}

	private AdministrativeUnitReportModel_AdministrativeUnit getUnit(AdministrativeUnitReportModel model, String code) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnitFound = null;
		for (AdministrativeUnitReportModel_AdministrativeUnit unit : model.getAdministrativeUnits()) {
			if (unit.getCode().equals(code)) {
				administrativeUnitFound = unit;
			}
		}
		if (administrativeUnitFound == null) {
			fail("Did not find administrative unit " + code);
		}

		return administrativeUnitFound;
	}

	private AdministrativeUnitReportModel_AdministrativeUnit getUnit(
			AdministrativeUnitReportModel_AdministrativeUnit parentUnit, String code) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnitFound = null;
		for (AdministrativeUnitReportModel_AdministrativeUnit unit : parentUnit.getChildAdministrativeUnits()) {
			if (unit.getCode().equals(code)) {
				administrativeUnitFound = unit;
			}
		}
		if (administrativeUnitFound == null) {
			fail("Did not find child administrative unit " + code);
		}

		return administrativeUnitFound;
	}

	private AdministrativeUnitReportModel_User getUser(List<AdministrativeUnitReportModel_User> users, String userName) {
		AdministrativeUnitReportModel_User userFound = null;

		for (AdministrativeUnitReportModel_User user : users) {
			if (user.getUserName().equals(userName)) {
				userFound = user;
			}
		}
		if (userFound == null) {
			fail("Did not find user " + userName);
		}

		return userFound;
	}
}