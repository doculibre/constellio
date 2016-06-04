package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.app.modules.rm.model.enums.DisposalType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DESTRUCTION;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2007ImportDataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRulesRecordsImportServicesAcceptanceTest extends ConstellioTest {

	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	private RecordsImportServices importServices;
	private User admin;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);

		importServices = new RecordsImportServices(getModelLayerFactory());

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenImportingXLSXFileWithRetentionRulesThenValidate()
			throws Exception {
		importServices
				.bulkImport(Excel2007ImportDataProvider.fromFile(getTestResourceFile("retentionRule.xlsx")), progressionListener,
						admin);

		importAndValidateWithRetentionRules();
	}

	private void importAndValidateWithRetentionRules()
			throws RecordServicesException {

		LocalDate localDate = new LocalDate(2015, 5, 1);

		RetentionRule rule1 = rm.wrapRetentionRule(expectedRecordWithLegacyId("1"));
		assertThat(rule1.isApproved()).isTrue();
		assertThat(rule1.getApprovalDate()).isEqualTo(localDate);
		assertThat(rule1.getTitle()).isEqualTo("Rule#1");
		assertThat(rule1.getCode()).isEqualTo("111190");
		assertThat(rule1.getDescription())
				.isEqualTo("Documents produits ou reçus relatifs à la gestion des documents constitutifs. Les documents peuvent");
		assertThat(rule1.getCorpus()).isNull();
		assertThat(rule1.getCorpusRuleNumber()).isEqualTo("123");
		assertThat(rule1.getAdministrativeUnits()).isEqualTo(
				asList(expectedAdministrativeUnitWithCode("10").getId(), expectedAdministrativeUnitWithCode("20").getId()));
		assertThat(rule1.isResponsibleAdministrativeUnits()).isFalse();
		assertThat(rule1.getKeywords()).isEqualTo(asList("Rule #1"));
		assertThat(rule1.getJuridicReference()).isEqualTo("Référence #1");
		assertThat(rule1.getGeneralComment()).isEqualTo("Commentaire #1");
		assertThat(rule1.getHistory()).isEqualTo("Historique #1");
		assertThat(rule1.isEssentialDocuments()).isTrue();
		assertThat(rule1.isConfidentialDocuments()).isFalse();
		assertThat(rule1.getDocumentTypes().contains(rm.getDocumentTypeByCode("2").getId())).isTrue();
		assertThat(rule1.getDocumentTypes().contains(rm.getDocumentTypeByCode("1").getId())).isTrue();

		CopyRetentionRule secondaryCopy = rule1.getSecondaryCopy();
		assertThat(secondaryCopy.getCode()).isEqualTo("123");
		assertThat(secondaryCopy.getCopyType()).isEqualTo(CopyType.SECONDARY);
		assertThat(secondaryCopy.getMediumTypeIds()).isEqualTo(asList(rm.getMediumTypeByCode("DM").getId()));
		assertThat(secondaryCopy.getContentTypesComment()).isEqualTo("R1");
		assertThat(secondaryCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		assertThat(secondaryCopy.getActiveRetentionComment()).isEqualTo("R2");
		assertThat(secondaryCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.ZERO);
		assertThat(secondaryCopy.getSemiActiveRetentionComment()).isEqualTo("R3");
		assertThat(secondaryCopy.getInactiveDisposalType()).isEqualTo(DESTRUCTION);
		assertThat(secondaryCopy.getInactiveDisposalComment()).isEqualTo("R4");

		assertThat(rule1.getPrincipalCopies().size()).isEqualTo(1);

		CopyRetentionRule principalCopy = rule1.getPrincipalCopies().get(0);
		assertThat(principalCopy.getCode()).isEqualTo("123");
		assertThat(principalCopy.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(principalCopy.getMediumTypeIds()).isEqualTo(asList(rm.getMediumTypeByCode("PA").getId()));
		assertThat(principalCopy.getContentTypesComment()).isEqualTo("R5");
		assertThat(principalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		assertThat(principalCopy.getActiveRetentionComment()).isEqualTo("R6");
		assertThat(principalCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.ZERO);
		assertThat(principalCopy.getSemiActiveRetentionComment()).isEqualTo("R7");
		assertThat(principalCopy.getInactiveDisposalType()).isEqualTo(DEPOSIT);
		assertThat(principalCopy.getInactiveDisposalComment()).isEqualTo("R8");

		assertThat(rule1.getCopyRulesComment())
				.isEqualTo(asList("R1: #1", "R2: #2", "R3: #3", "R4: #4", "R5: #5", "R6: #6", "R7: #7", "R8: #8"));

		RetentionRule ruleAdministration = rm.wrapRetentionRule(expectedRecordWithLegacyId("2"));
		assertThat(ruleAdministration.isApproved()).isTrue();
		assertThat(ruleAdministration.getApprovalDate()).isEqualTo(localDate);
		assertThat(ruleAdministration.getTitle()).isEqualTo("Conseil d'administration");
		assertThat(ruleAdministration.getCode()).isEqualTo("111201");
		assertThat(ruleAdministration.getDescription()).isEqualTo(
				"Documents produits ou reçus relatifs à l'historique de l'Ordre et aux événements qui ont marqué le cours de son développement. Les documents peuvent comprendre les textes, les notes, les images fixes ou animées.  comprendre les certifications, les lettres patentes, la charte et les statuts.");
		assertThat(ruleAdministration.getCorpus()).isNull();
		assertThat(ruleAdministration.getCorpusRuleNumber()).isEqualTo("456");
		assertThat(ruleAdministration.getAdministrativeUnits()).isEqualTo(asList(expectedAdministrativeUnitWithCode("30").getId()));
		assertThat(ruleAdministration.isResponsibleAdministrativeUnits()).isFalse();
		assertThat(ruleAdministration.getKeywords()).isEqualTo(asList("Rule #2"));
		assertThat(ruleAdministration.getJuridicReference()).isEqualTo("Référence #2");
		assertThat(ruleAdministration.getGeneralComment()).isEqualTo("Commentaire #2");
		assertThat(ruleAdministration.getHistory()).isEqualTo("Historique #2");
		assertThat(ruleAdministration.isEssentialDocuments()).isFalse();
		assertThat(ruleAdministration.isConfidentialDocuments()).isTrue();
		assertThat(ruleAdministration.getDocumentTypes().contains(rm.getDocumentTypeByCode("1").getId())).isTrue();

		secondaryCopy = ruleAdministration.getSecondaryCopy();
		assertThat(secondaryCopy.getCode()).isEqualTo("123");
		assertThat(secondaryCopy.getCopyType()).isEqualTo(CopyType.SECONDARY);
		assertThat(secondaryCopy.getMediumTypeIds())
				.isEqualTo(asList(rm.getMediumTypeByCode("DM").getId(), rm.getMediumTypeByCode("PA").getId()));
		assertThat(secondaryCopy.getContentTypesComment()).isNull();
		assertThat(secondaryCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		assertThat(secondaryCopy.getActiveRetentionComment()).isNull();
		assertThat(secondaryCopy.getSemiActiveRetentionPeriod()).isEqualTo(RetentionPeriod.ZERO);
		assertThat(secondaryCopy.getSemiActiveRetentionComment()).isNull();
		assertThat(secondaryCopy.getInactiveDisposalType()).isEqualTo(DESTRUCTION);
		assertThat(secondaryCopy.getInactiveDisposalComment()).isEqualTo("R1");

		assertThat(ruleAdministration.getPrincipalCopies().size()).isEqualTo(2);

		principalCopy = ruleAdministration.getPrincipalCopies().get(0);
		assertThat(principalCopy.getCode()).isEqualTo("123");
		assertThat(principalCopy.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(principalCopy.getMediumTypeIds())
				.isEqualTo(asList(rm.getMediumTypeByCode("PA").getId()));
		assertThat(principalCopy.getContentTypesComment()).isNull();
		assertThat(principalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		assertThat(principalCopy.getActiveRetentionComment()).isNull();
		assertThat(principalCopy.getSemiActiveRetentionPeriod()).isEqualTo(
				RetentionPeriod.fixed(1));
		assertThat(principalCopy.getSemiActiveRetentionComment()).isEqualTo("R2");
		assertThat(principalCopy.getInactiveDisposalType()).isEqualTo(DEPOSIT);
		assertThat(principalCopy.getInactiveDisposalComment()).isNull();

		principalCopy = ruleAdministration.getPrincipalCopies().get(1);
		assertThat(principalCopy.getCode()).isEqualTo("123");
		assertThat(principalCopy.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(principalCopy.getMediumTypeIds())
				.isEqualTo(asList(rm.getMediumTypeByCode("DM").getId()));
		assertThat(principalCopy.getContentTypesComment()).isNull();
		assertThat(principalCopy.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(3));
		assertThat(principalCopy.getActiveRetentionComment()).isEqualTo("R3");
		assertThat(principalCopy.getSemiActiveRetentionPeriod())
				.isEqualTo(RetentionPeriod.fixed(5));
		assertThat(principalCopy.getSemiActiveRetentionComment()).isNull();
		assertThat(principalCopy.getInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		assertThat(principalCopy.getInactiveDisposalComment()).isNull();

		assertThat(ruleAdministration.getCopyRulesComment())
				.isEqualTo(asList("R1: #1", "R2: #2", "R3: #3"));
	}

	private Record expectedRecordWithLegacyId(String legacyId) {
		Record record = recordWithLegacyId(legacyId);
		assertThat(record).describedAs("Record with legacy id '" + legacyId + "' should exist");
		return record;
	}

	private Record expectedAdministrativeUnitWithCode(String code) {
		return rm.getAdministrativeUnitWithCode(code).getWrappedRecord();
	}

	private Record recordWithLegacyId(String legacyId) {
		return getModelLayerFactory().newSearchServices().searchSingleResult(
				fromAllSchemasIn(zeCollection).where(Schemas.LEGACY_ID).isEqualTo(legacyId));
	}
}
