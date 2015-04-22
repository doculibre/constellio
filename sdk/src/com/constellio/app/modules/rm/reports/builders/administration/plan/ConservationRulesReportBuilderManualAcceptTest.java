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
package com.constellio.app.modules.rm.reports.builders.administration.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Copy;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Rule;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class ConservationRulesReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	List<String> supportTypes;
	Map<String, String> principalsHolders;
	Map<String, String> secondaryHolder;

	@Before
	public void setUp()
			throws Exception {

		principalsHolders = new HashMap<>();
		principalsHolders.put("UA 1600", "Vice-présidence aux affaires publiques et "
				+ "gouvernementales et secrétariat général ");
		principalsHolders.put("UA 3700", "Direction générale du suivi des risques "
				+ "organisationnels et de la mesure de la "
				+ "performance ");

		secondaryHolder = new HashMap<>();
		secondaryHolder.put("UA 5461", "Service de l'assurance qualité et de la "
				+ "formation");

		supportTypes = new ArrayList<>();
		supportTypes.add("DM");
		supportTypes.add("PA");
	}

	@Test
	public void whenBuildConservationRulesReportThenOk()
			throws Exception {

		ConservationRulesReportModel_Copy principalCopy1 = new ConservationRulesReportModel_Copy();
		principalCopy1.setPrincipal(true);
		principalCopy1.setActive("888");
		principalCopy1.setSemiActive("2");
		principalCopy1.setInactive("D");
		principalCopy1.setObservations("Actif : jusqu'à remplacement par une nouvelle version. "
				+ "Inactif : La valeur de recherche est assurée par la conservation des "
				+ "documents importants sur support papier aux archives historiques.");
		principalCopy1.setSupportTypes(supportTypes);

		ConservationRulesReportModel_Copy principalCopy2 = new ConservationRulesReportModel_Copy();
		principalCopy2.setPrincipal(true);
		principalCopy2.setActive("4");
		principalCopy2.setSemiActive("2");
		principalCopy2.setInactive("T");
		principalCopy2.setSupportTypes(supportTypes);
		principalCopy2.setObservations("Actif : jusqu'à remplacement par une nouvelle version.");

		ConservationRulesReportModel_Copy principalCopy3 = new ConservationRulesReportModel_Copy();
		principalCopy3.setPrincipal(true);
		principalCopy3.setActive("999");
		principalCopy3.setSemiActive("2");
		principalCopy3.setInactive("T");
		principalCopy3.setSupportTypes(supportTypes);

		ConservationRulesReportModel_Copy secondaryCopy = new ConservationRulesReportModel_Copy();
		secondaryCopy.setPrincipal(false);
		secondaryCopy.setActive("999");
		secondaryCopy.setSemiActive("2");
		secondaryCopy.setInactive("T");
		secondaryCopy.setSupportTypes(supportTypes);

		ConservationRulesReportModel_Rule rule = new ConservationRulesReportModel_Rule();
		rule.setPrincipalsCopies(Arrays.asList(principalCopy1, principalCopy2, principalCopy3));
		rule.setSecondaryCopy(secondaryCopy);
		String description =
				"Documents relatifs aux comités permanents mis sur pied à la demande du conseil d'aministration. Ces documents concernent le fonctionnement "
						+ "(avis de convocation, ordres du jour, procès-verbaux et documents afférents) des comités du Conseil d'administration (tel que le comité de "
						+ "vérification) ainsi que les documents officiels (décisions, mémoires, etc.)";
		rule.setDescription(description);
		rule.setPrincipalsHolders(principalsHolders);
		rule.setRuleNumber("14 002");
		rule.setTitle("Gestion des effectifs");

		ConservationRulesReportModel model = new ConservationRulesReportModel();
		model.setRules(Arrays.asList(rule, rule));
		//		model.setRules(Arrays.asList(rule));

		build(new ConservationRulesReportBuilder(model,getModelLayerFactory().getFoldersLocator()));
	}
}
