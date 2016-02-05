package com.constellio.app.modules.rm.reports.administration.plan;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.reports.builders.administration.plan.UserReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class UserReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	UserReportModel model;

	@Before
	public void setUp()
			throws Exception {
	}

	@Test
	public void whenBuildEmptyClassificationPlanReportThenOk() {
		model = new UserReportModel();
		build(new UserReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDetailedClassificationPlanReportThenOk() {
		model = configAdminUnits();
		build(new UserReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	private UserReportModel configAdminUnits() {

		UserReportModel model = new UserReportModel();

		UserReportModel_User user1 = new UserReportModel_User();

		List<UserReportModel_AdministrativeUnit> administrativeUnits01 = new ArrayList<UserReportModel_AdministrativeUnit>();
		UserReportModel_AdministrativeUnit administrativeUnits01_adminUnit01 = new UserReportModel_AdministrativeUnit();
		administrativeUnits01_adminUnit01.setCode("0111").setLabel("MCC").setDescription("");
		administrativeUnits01.add(administrativeUnits01_adminUnit01);

		user1.setAdministrativeUnits(administrativeUnits01);

		UserReportModel_User user2 = new UserReportModel_User();

		List<UserReportModel_AdministrativeUnit> administrativeUnits02 = new ArrayList<UserReportModel_AdministrativeUnit>();

		UserReportModel_AdministrativeUnit administrativeUnits02_adminUnit01 = new UserReportModel_AdministrativeUnit();
		administrativeUnits02_adminUnit01.setCode("A-Salle").setLabel("Planification des ressources humaines").setDescription("");
		UserReportModel_AdministrativeUnit administrativeUnits02_adminUnit02 = new UserReportModel_AdministrativeUnit();
		administrativeUnits02_adminUnit02.setCode("B-Salle").setLabel("Organisation des ressources humaines").setDescription("");
		UserReportModel_AdministrativeUnit administrativeUnits02_adminUnit03 = new UserReportModel_AdministrativeUnit();
		administrativeUnits02_adminUnit03.setCode("C-Salle").setLabel("Administration des ressources humaines")
				.setDescription("");
		UserReportModel_AdministrativeUnit administrativeUnits02_adminUnit04 = new UserReportModel_AdministrativeUnit();
		administrativeUnits02_adminUnit04.setCode("D-Salle").setLabel("Contrôle des ressources humaines").setDescription("");

		administrativeUnits02.add(administrativeUnits02_adminUnit01);
		administrativeUnits02.add(administrativeUnits02_adminUnit02);
		administrativeUnits02.add(administrativeUnits02_adminUnit03);
		administrativeUnits02.add(administrativeUnits02_adminUnit04);

		user2.setAdministrativeUnits(administrativeUnits02);

		UserReportModel_User user3 = new UserReportModel_User();

		UserReportModel_User user4 = new UserReportModel_User();

		List<UserReportModel_AdministrativeUnit> administrativeUnits04 = new ArrayList<UserReportModel_AdministrativeUnit>();
		UserReportModel_AdministrativeUnit administrativeUnits04_adminUnit01 = new UserReportModel_AdministrativeUnit();
		administrativeUnits04_adminUnit01.setCode("3700")
				.setLabel("Direction générale du suivi des risques organisationnels et de la mesure de la performance - poste")
				.setDescription("");
		administrativeUnits04.add(administrativeUnits04_adminUnit01);

		user4.setAdministrativeUnits(administrativeUnits04);

		UserReportModel_User user5 = new UserReportModel_User();
		user5.setAdministrativeUnits(administrativeUnits01);

		UserReportModel_User user6 = new UserReportModel_User();

		UserReportModel_User user7 = new UserReportModel_User();

		List<UserReportModel_AdministrativeUnit> administrativeUnits07 = new ArrayList<UserReportModel_AdministrativeUnit>();
		administrativeUnits07.add(administrativeUnits02_adminUnit01);

		user7.setAdministrativeUnits(administrativeUnits07);

		UserReportModel_User user8 = new UserReportModel_User();

		UserReportModel_User user9 = new UserReportModel_User();

		user1.setUserId("37").setLastName("admin2").setFirstName("admin2").setUserName("admin2").setStatus("Actif")
				.setUnit("0000");
		user2.setUserId("3").setLastName("Bissonnette").setFirstName("Natalie").setUserName("NBisonette").setStatus("Actif")
				.setUnit("RH");
		user3.setUserId("29").setLastName("Blais").setFirstName("Maud").setUserName("Maud").setStatus("Actif").setUnit("");

		user4.setUserId("34").setLastName("Bolduc").setFirstName("Christian").setUserName("CBolduc").setStatus("Actif")
				.setUnit("UA 3700");
		user5.setUserId("9").setLastName("Bouchard").setFirstName("Marc").setUserName("MBouchard").setStatus("Actif")
				.setUnit("0000");
		user6.setUserId("30").setLastName("Chapdelaine").setFirstName("Maria").setUserName("MChapdelaine").setStatus("Actif")
				.setUnit("CAF");

		user7.setUserId("35").setLastName("Couture").setFirstName("Mélanie").setUserName("couture_mt").setStatus("Actif")
				.setUnit("RH");
		user8.setUserId("16").setLastName("Couture").setFirstName("Nadia").setUserName("CON16").setStatus("Actif")
				.setUnit("UA 1641");
		user9.setUserId("39").setLastName("Voyer").setFirstName("Daniel").setUserName("vod00").setStatus("Actif").setUnit("7000");

		ArrayList<UserReportModel_User> users = new ArrayList<UserReportModel_User>();

		users.add(user1);
		users.add(user2);
		users.add(user3);

		users.add(user4);
		users.add(user5);
		users.add(user6);

		users.add(user7);
		users.add(user8);
		users.add(user9);

		model.setUsers(users);

		return model;
	}

}
