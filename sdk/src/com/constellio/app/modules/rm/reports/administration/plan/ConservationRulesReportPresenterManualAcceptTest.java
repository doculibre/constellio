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
package com.constellio.app.modules.rm.reports.administration.plan;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.reports.builders.administration.plan.ConservationRulesReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Copy;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Rule;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportPresenter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class ConservationRulesReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	RMTestRecords records = new RMTestRecords(zeCollection);
	ConservationRulesReportPresenter presenter;
	RMSchemasRecordsServices rm;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		presenter = new ConservationRulesReportPresenter(zeCollection, getModelLayerFactory());
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenBuildingModelThenGetAppropriateModel() {
		boolean withAdministrativeUnit = false;
		presenter = new ConservationRulesReportPresenter(zeCollection, getModelLayerFactory(), withAdministrativeUnit);
		ConservationRulesReportModel model = presenter.build();
		assertThat(model.getTitle()).isEqualTo("Liste des règles de conservation");

		List<ConservationRulesReportModel_Rule> modelRules = model.getRules();
		assertThat(modelRules.size()).isEqualTo(5);

		processRule1(modelRules);

		processRule2(modelRules);

		processRule3(modelRules);

		processRule4(modelRules);

		build(new ConservationRulesReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	@Test
	public void givenNewRetentionRuleInAdministrativeUnitWhenBuildingModelByAdministrativeUnitThenGetAppropriateModel()
			throws Exception {

		givenNewRetentionRule();

		boolean withAdministrativeUnit = true;
		presenter = new ConservationRulesReportPresenter(zeCollection, getModelLayerFactory(), withAdministrativeUnit);
		ConservationRulesReportModel model = presenter.build();
		assertThat(model.getTitle()).isEqualTo("Liste des règles de conservation");

		Map<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> adminUnitRulesMap = model
				.getRulesByAdministrativeUnitMap();
		assertThat(adminUnitRulesMap.size()).isEqualTo(2);

		assertThat(adminUnitRulesMap.get(rm.getAdministrativeUnit("unitId_10")).size()).isEqualTo(2);
		assertRule1Infos(adminUnitRulesMap.get(rm.getAdministrativeUnit("unitId_10")).get(0));
		assertThat(adminUnitRulesMap.get(rm.getAdministrativeUnit("unitId_10")).get(1).getRuleNumber()).isEqualTo("zeCode");
		assertThat(adminUnitRulesMap.get(rm.getAdministrativeUnit("unitId_10")).get(1).getTitle()).isEqualTo("zeTitle");

		assertThat(adminUnitRulesMap.get(rm.getAdministrativeUnit("unitId_20")).size()).isEqualTo(1);
		assertRule1Infos(adminUnitRulesMap.get(rm.getAdministrativeUnit("unitId_20")).get(0));

		build(new ConservationRulesReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	private void processRule1(List<ConservationRulesReportModel_Rule> modelRules) {

		ConservationRulesReportModel_Rule rule1 = modelRules.get(1);
		assertRule1Infos(rule1);

	}

	private void assertRule1Infos(ConservationRulesReportModel_Rule rule1) {
		assertThat(rule1.getRuleNumber()).isEqualTo("1");
		assertThat(rule1.getTitle()).isEqualTo("Rule #1");
		assertThat(rule1.getDescription()).isEqualTo("Description Rule 1");

		Map<String, String> principalHolders = rule1.getAdministrativeUnits();
		assertThat(principalHolders).containsOnly(entry("10", "Administrative unit with room A"),
				entry("20", "Administrative unit with room D"));

		List<ConservationRulesReportModel_Copy> principalCopies = rule1.getPrincipalsCopies();
		assertThat(principalCopies.size()).isEqualTo(1);

		ConservationRulesReportModel_Copy principalCopy = principalCopies.get(0);
		assertThat(principalCopy.getActive()).isEqualTo("42");
		assertThat(principalCopy.getSemiActive()).isEqualTo("5");
		assertThat(principalCopy.getInactive()).isEqualTo("C");
		assertThat(principalCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(principalCopy.getObservations()).isEqualTo("Supports: comment1\nActif: comment2");

		ConservationRulesReportModel_Copy secondaryCopy = rule1.getSecondaryCopy();
		assertThat(secondaryCopy.getActive()).isEqualTo("888");
		assertThat(secondaryCopy.getSemiActive()).isEqualTo("0");
		assertThat(secondaryCopy.getInactive()).isEqualTo("D");
		assertThat(secondaryCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(secondaryCopy.getObservations()).isEqualTo("Semi-actif: comment3\nInactif: comment4");
	}

	private void processRule2(List<ConservationRulesReportModel_Rule> modelRules) {

		ConservationRulesReportModel_Rule rule2 = modelRules.get(2);
		assertThat(rule2.getRuleNumber()).isEqualTo("2");
		assertThat(rule2.getTitle()).isEqualTo("Rule #2");
		assertThat(rule2.getDescription()).isEmpty();

		Map<String, String> principalHolders = rule2.getAdministrativeUnits();
		assertThat(principalHolders).isEmpty();

		List<ConservationRulesReportModel_Copy> principalCopies2 = rule2.getPrincipalsCopies();
		assertThat(principalCopies2.size()).isEqualTo(1);

		ConservationRulesReportModel_Copy principalCopy = principalCopies2.get(0);
		assertThat(principalCopy.getActive()).isEqualTo("5");
		assertThat(principalCopy.getSemiActive()).isEqualTo("2");
		assertThat(principalCopy.getInactive()).isEqualTo("T");
		assertThat(principalCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(principalCopy.getObservations()).isEmpty();

		ConservationRulesReportModel_Copy secondaryCopy = rule2.getSecondaryCopy();
		assertThat(secondaryCopy.getActive()).isEqualTo("2");
		assertThat(secondaryCopy.getSemiActive()).isEqualTo("0");
		assertThat(secondaryCopy.getInactive()).isEqualTo("D");
		assertThat(secondaryCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(secondaryCopy.getObservations()).isEmpty();
	}

	private void processRule3(List<ConservationRulesReportModel_Rule> modelRules) {

		ConservationRulesReportModel_Rule rule3 = modelRules.get(3);
		assertThat(rule3.getRuleNumber()).isEqualTo("3");
		assertThat(rule3.getTitle()).isEqualTo("Rule #3");
		assertThat(rule3.getDescription()).isEmpty();

		Map<String, String> principalHolders = rule3.getAdministrativeUnits();
		assertThat(principalHolders).isEmpty();

		List<ConservationRulesReportModel_Copy> principalCopies3 = rule3.getPrincipalsCopies();
		assertThat(principalCopies3.size()).isEqualTo(1);

		ConservationRulesReportModel_Copy principalCopy = principalCopies3.get(0);
		assertThat(principalCopy.getActive()).isEqualTo("999");
		assertThat(principalCopy.getSemiActive()).isEqualTo("4");
		assertThat(principalCopy.getInactive()).isEqualTo("T");
		assertThat(principalCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(principalCopy.getObservations()).isEmpty();

		ConservationRulesReportModel_Copy secondaryCopy = rule3.getSecondaryCopy();
		assertThat(secondaryCopy.getActive()).isEqualTo("1");
		assertThat(secondaryCopy.getSemiActive()).isEqualTo("0");
		assertThat(secondaryCopy.getInactive()).isEqualTo("D");
		assertThat(secondaryCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(secondaryCopy.getObservations()).isEmpty();
	}

	private void processRule4(List<ConservationRulesReportModel_Rule> modelRules) {
		ConservationRulesReportModel_Rule rule4 = modelRules.get(4);
		assertThat(rule4.getRuleNumber()).isEqualTo("4");
		assertThat(rule4.getTitle()).isEqualTo("Rule #4");
		assertThat(rule4.getDescription()).isEmpty();

		Map<String, String> principalHolders = rule4.getAdministrativeUnits();
		assertThat(principalHolders).isEmpty();

		List<ConservationRulesReportModel_Copy> principalCopies4 = rule4.getPrincipalsCopies();
		assertThat(principalCopies4.size()).isEqualTo(2);

		ConservationRulesReportModel_Copy principalCopy1 = principalCopies4.get(0);
		assertThat(principalCopy1.getActive()).isEqualTo("3");
		assertThat(principalCopy1.getSemiActive()).isEqualTo("888");
		assertThat(principalCopy1.getInactive()).isEqualTo("D");
		assertThat(principalCopy1.getSupportTypes()).containsOnly("PA");
		assertThat(principalCopy1.getObservations()).isEmpty();

		ConservationRulesReportModel_Copy principalCopy2 = principalCopies4.get(1);
		assertThat(principalCopy2.getActive()).isEqualTo("3");
		assertThat(principalCopy2.getSemiActive()).isEqualTo("888");
		assertThat(principalCopy2.getInactive()).isEqualTo("C");
		assertThat(principalCopy2.getSupportTypes()).containsOnly("DM");
		assertThat(principalCopy2.getObservations()).isEmpty();

		ConservationRulesReportModel_Copy secondaryCopy = rule4.getSecondaryCopy();
		assertThat(secondaryCopy.getActive()).isEqualTo("666");
		assertThat(secondaryCopy.getSemiActive()).isEqualTo("0");
		assertThat(secondaryCopy.getInactive()).isEqualTo("D");
		assertThat(secondaryCopy.getSupportTypes()).containsOnly("PA", "DM");
		assertThat(secondaryCopy.getObservations()).isEqualTo("Semi-actif: comment3");
	}

	private void givenNewRetentionRule()
			throws RecordServicesException {
		VariableRetentionPeriod period42 = rm.newVariableRetentionPeriod().setCode("42").setTitle("Ze 42");
		VariableRetentionPeriod period666 = rm.newVariableRetentionPeriod().setCode("666").setTitle("Ze 666");

		CopyRetentionRule principal = CopyRetentionRule.newPrincipal(asList("PA"))
				.setActiveRetentionPeriod(RetentionPeriod.variable(period42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.variable(period666))
				.setInactiveDisposalType(DisposalType.DEPOSIT);

		CopyRetentionRule secondary = CopyRetentionRule.newSecondary(asList("PA"))
				.setActiveRetentionPeriod(RetentionPeriod.fixed(42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(666))
				.setInactiveDisposalType(DisposalType.DEPOSIT);

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		RetentionRule retentionRule = rm.newRetentionRuleWithId("newRetentionRule");
		retentionRule.setAdministrativeUnits(asList("unitId_10"));
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setCopyRetentionRules(asList(principal, secondary));

		DocumentType documentType1 = rm.newDocumentType();
		documentType1.setCode("docType1").setTitle("docType1");
		DocumentType documentType2 = rm.newDocumentType();
		documentType2.setCode("docType2").setTitle("docType2");
		recordServices.add(documentType1);
		recordServices.add(documentType2);

		List<RetentionRuleDocumentType> documentTypes = asList(
				new RetentionRuleDocumentType(documentType1.getId(), DisposalType.DEPOSIT),
				new RetentionRuleDocumentType(documentType2.getId(), DisposalType.DESTRUCTION)
		);
		retentionRule.setDocumentTypesDetails(documentTypes);
		recordServices.add(retentionRule);
	}
}