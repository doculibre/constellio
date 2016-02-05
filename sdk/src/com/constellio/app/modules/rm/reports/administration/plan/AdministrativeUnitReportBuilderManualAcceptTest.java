package com.constellio.app.modules.rm.reports.administration.plan;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_User;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class AdministrativeUnitReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	AdministrativeUnitReportModel model;

	@Before
	public void setUp()
			throws Exception {
	}

	@Test
	public void whenBuildEmptyAdministrativeUnitReportThenOk() {
		model = new AdministrativeUnitReportModel();
		build(new AdministrativeUnitReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDetailedAdministrativeUnitReportThenOk() {
		model = configAdminUnits();
		build(new AdministrativeUnitReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	private AdministrativeUnitReportModel configAdminUnits() {

		AdministrativeUnitReportModel model = new AdministrativeUnitReportModel();

		AdministrativeUnitReportModel_User user_11a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_11a_02 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_11a_03 = new AdministrativeUnitReportModel_User();

		user_11a_01.setFirstName("Dakota_11_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_11a_02.setFirstName("Dakota_11_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_11a_03.setFirstName("Dakota_11_03").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_11a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_11a.add(user_11a_01);
		users_11a.add(user_11a_02);
		users_11a.add(user_11a_02);

		AdministrativeUnitReportModel_User user_12a_01 = new AdministrativeUnitReportModel_User();
		ArrayList<AdministrativeUnitReportModel_User> users_12a = new ArrayList<AdministrativeUnitReportModel_User>();
		user_12a_01.setFirstName("Dakota_12_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		users_12a.add(user_12a_01);

		AdministrativeUnitReportModel_User user_1311a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_1311a_02 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_1311a_03 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_1311a_04 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_1311a_05 = new AdministrativeUnitReportModel_User();

		user_1311a_01.setFirstName("Dakota_1311_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_1311a_02.setFirstName("Dakota_1311_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_1311a_03.setFirstName("Dakota_1311_03").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_1311a_04.setFirstName("Dakota_1311_04").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_1311a_05.setFirstName("Dakota_1311_05").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_1311a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_1311a.add(user_1311a_01);
		users_1311a.add(user_1311a_02);
		users_1311a.add(user_1311a_03);
		users_1311a.add(user_1311a_04);
		users_1311a.add(user_1311a_05);

		AdministrativeUnitReportModel_User user_132a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_132a_02 = new AdministrativeUnitReportModel_User();

		user_1311a_01.setFirstName("Dakota_132_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_1311a_01.setFirstName("Dakota_132_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_132a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_132a.add(user_132a_01);
		users_132a.add(user_132a_02);

		AdministrativeUnitReportModel_User user_14a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_14a_02 = new AdministrativeUnitReportModel_User();

		user_14a_01.setFirstName("Dakota_14_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_14a_02.setFirstName("Dakota_14_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_14a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_14a.add(user_14a_01);
		users_14a.add(user_14a_02);

		AdministrativeUnitReportModel_User user_15a_01 = new AdministrativeUnitReportModel_User();
		user_15a_01.setFirstName("Dakota_15_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		AdministrativeUnitReportModel_User user_21a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_02 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_03 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_04 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_05 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_06 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_07 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_08 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_09 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_21a_10 = new AdministrativeUnitReportModel_User();

		user_21a_01.setFirstName("Dakota_21_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_02.setFirstName("Dakota_21_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_03.setFirstName("Dakota_21_03").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_04.setFirstName("Dakota_21_04").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_05.setFirstName("Dakota_21_05").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_06.setFirstName("Dakota_21_06").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_07.setFirstName("Dakota_21_07").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_08.setFirstName("Dakota_21_08").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_09.setFirstName("Dakota_21_09").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_21a_10.setFirstName("Dakota_21_10").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_21 = new ArrayList<AdministrativeUnitReportModel_User>();
		users_21.add(user_21a_01);
		users_21.add(user_21a_02);
		users_21.add(user_21a_03);
		users_21.add(user_21a_04);
		users_21.add(user_21a_05);
		users_21.add(user_21a_06);
		users_21.add(user_21a_07);
		users_21.add(user_21a_08);
		users_21.add(user_21a_09);
		users_21.add(user_21a_10);

		AdministrativeUnitReportModel_User user_22a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_22a_02 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_22a_03 = new AdministrativeUnitReportModel_User();

		user_22a_01.setFirstName("Dakota_22_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_22a_02.setFirstName("Dakota_22_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_22a_03.setFirstName("Dakota_22_03").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_22a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_22a.add(user_22a_01);
		users_22a.add(user_22a_02);
		users_22a.add(user_22a_03);

		AdministrativeUnitReportModel_User user_23a_01 = new AdministrativeUnitReportModel_User();
		user_23a_01.setFirstName("Dakota_23_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_23a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_23a.add(user_23a_01);

		AdministrativeUnitReportModel_User user_4a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_4a_02 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_4a_03 = new AdministrativeUnitReportModel_User();

		user_4a_01.setFirstName("Dakota_4_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_4a_02.setFirstName("Dakota_4_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_4a_03.setFirstName("Dakota_4_03").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_4a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_4a.add(user_4a_01);
		users_4a.add(user_4a_02);
		users_4a.add(user_4a_03);

		AdministrativeUnitReportModel_User user_5a_01 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_5a_02 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_5a_03 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_5a_04 = new AdministrativeUnitReportModel_User();
		AdministrativeUnitReportModel_User user_5a_05 = new AdministrativeUnitReportModel_User();

		user_5a_01.setFirstName("Dakota_5_01").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_5a_02.setFirstName("Dakota_5_02").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_5a_03.setFirstName("Dakota_5_03").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_5a_04.setFirstName("Dakota_5_04").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");
		user_5a_05.setFirstName("Dakota_5_05").setLastName("Indien").setUserName("DakotaIndien").setEmail("dako@dako.com");

		ArrayList<AdministrativeUnitReportModel_User> users_5a = new ArrayList<AdministrativeUnitReportModel_User>();
		users_5a.add(user_5a_01);
		users_5a.add(user_5a_02);
		users_5a.add(user_5a_03);
		users_5a.add(user_5a_04);
		users_5a.add(user_5a_05);

		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_1 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_11 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_12 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_13 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_13_1 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_13_1_1 = new AdministrativeUnitReportModel_AdministrativeUnit();

		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_13_2 = new AdministrativeUnitReportModel_AdministrativeUnit();

		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_14 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_15 = new AdministrativeUnitReportModel_AdministrativeUnit();

		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_2 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_21 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_22 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_23 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_24 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_25 = new AdministrativeUnitReportModel_AdministrativeUnit();

		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_3 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_4 = new AdministrativeUnitReportModel_AdministrativeUnit();
		AdministrativeUnitReportModel_AdministrativeUnit adminUnit_5 = new AdministrativeUnitReportModel_AdministrativeUnit();

		adminUnit_1.setCode("level_1").setLabel("Ze level 1").setDescription(textOfLength(200));

		adminUnit_11.setCode("level_11").setLabel("Ze level 11").setDescription(textOfLength(200)).setUsers(users_11a);
		adminUnit_12.setCode("level_12").setLabel("Ze level 12").setDescription(textOfLength(200)).setUsers(users_12a);
		adminUnit_13.setCode("level_13").setLabel("Ze level 13").setDescription(textOfLength(200));

		adminUnit_13_1.setCode("level_13_1").setLabel("Ze level 13_1").setDescription(textOfLength(200));
		adminUnit_13_1_1.setCode("level_13_1_1").setLabel("Ze level 13_1_1").setDescription(textOfLength(200))
				.setUsers(users_1311a);
		adminUnit_13_2.setCode("level_13_2").setLabel("Ze level 13_2").setDescription(textOfLength(200)).setUsers(users_132a);

		adminUnit_14.setCode("level_14").setLabel("Ze level 14").setDescription(textOfLength(200)).setUsers(users_14a);
		adminUnit_15.setCode("level_15").setLabel("Ze level 15").setDescription(textOfLength(200));

		ArrayList<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits_13_1 = new ArrayList<AdministrativeUnitReportModel_AdministrativeUnit>();
		childAdministrativeUnits_13_1.add(adminUnit_13_1_1);

		adminUnit_13_1.setChildAdministrativeUnits(childAdministrativeUnits_13_1);

		ArrayList<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits_13 = new ArrayList<AdministrativeUnitReportModel_AdministrativeUnit>();
		childAdministrativeUnits_13.add(adminUnit_13_1);

		adminUnit_13.setChildAdministrativeUnits(childAdministrativeUnits_13);

		ArrayList<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits_1 = new ArrayList<AdministrativeUnitReportModel_AdministrativeUnit>();
		childAdministrativeUnits_1.add(adminUnit_11);
		childAdministrativeUnits_1.add(adminUnit_12);
		childAdministrativeUnits_1.add(adminUnit_13);
		childAdministrativeUnits_1.add(adminUnit_14);
		childAdministrativeUnits_1.add(adminUnit_15);

		adminUnit_1.setChildAdministrativeUnits(childAdministrativeUnits_1);

		adminUnit_2.setCode("level_2").setLabel("Ze level 2").setDescription(textOfLength(200));
		adminUnit_21.setCode("level_21").setLabel("Ze level 21").setDescription(textOfLength(200)).setUsers(users_21);
		adminUnit_22.setCode("level_22").setLabel("Ze level 22").setDescription(textOfLength(200));
		adminUnit_23.setCode("level_23").setLabel("Ze level 23").setDescription(textOfLength(200));
		adminUnit_24.setCode("level_24").setLabel("Ze level 24").setDescription(textOfLength(200));
		adminUnit_25.setCode("level_25").setLabel("Ze level 25").setDescription(textOfLength(200));

		ArrayList<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits_2 = new ArrayList<AdministrativeUnitReportModel_AdministrativeUnit>();
		childAdministrativeUnits_2.add(adminUnit_21);
		childAdministrativeUnits_2.add(adminUnit_22);
		childAdministrativeUnits_2.add(adminUnit_23);
		childAdministrativeUnits_2.add(adminUnit_24);
		childAdministrativeUnits_2.add(adminUnit_25);

		adminUnit_2.setChildAdministrativeUnits(childAdministrativeUnits_2);

		adminUnit_3.setCode("level_3").setLabel("Ze level 3").setDescription(textOfLength(200));
		adminUnit_4.setCode("level_4").setLabel("Ze level 4").setDescription(textOfLength(200)).setUsers(users_4a);
		adminUnit_5.setCode("level_5").setLabel("Ze level 5").setDescription(textOfLength(200)).setUsers(users_5a);

		ArrayList<AdministrativeUnitReportModel_AdministrativeUnit> adminUnits = new ArrayList<AdministrativeUnitReportModel_AdministrativeUnit>();

		adminUnits.add(adminUnit_1);
		adminUnits.add(adminUnit_11);
		adminUnits.add(adminUnit_12);
		adminUnits.add(adminUnit_13);
		adminUnits.add(adminUnit_13_1);
		//adminUnits.add(adminUnit_13_1_1);

		adminUnits.add(adminUnit_13_2);

		adminUnits.add(adminUnit_14);
		adminUnits.add(adminUnit_15);

		adminUnits.add(adminUnit_2);
		adminUnits.add(adminUnit_21);
		adminUnits.add(adminUnit_22);
		//adminUnits.add(adminUnit_23);
		//adminUnits.add(adminUnit_24);
		//adminUnits.add(adminUnit_25);

		adminUnits.add(adminUnit_3);
		adminUnits.add(adminUnit_4);
		adminUnits.add(adminUnit_5);

		model.setAdministrativeUnits(adminUnits);

		return model;
	}

}
