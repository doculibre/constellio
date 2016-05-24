package com.constellio.app.modules.rm.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRulesAcceptanceTest extends ConstellioTest {

	LocalDate january31_2014 = new LocalDate(2014, 1, 31);

	String anAdministrativeUnitId, anotherAdministrativeUnitId;
	String aCategoryId, anotherCategoryId;

	MetadataSchema retentionRuleSchema;

	MetadataSchemaTypes types;
	RecordServices recordServices;

	CopyRetentionRule principalAnalogicRetentionRule;
	CopyRetentionRule principalNumericRetentionRule;
	CopyRetentionRule secondaryRetentionRule;

	String zeFirstType = "zeFirstType";
	String anotherType = "anotherType";

	String history = "Zis is my history";

	String keyword1 = "keyword 1";
	String keyword2 = "keyword 2";

	RMSchemasRecordsServices rm;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		recordServices = getModelLayerFactory().newRecordServices();
		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		retentionRuleSchema = types.getSchema(RetentionRule.DEFAULT_SCHEMA);
		MetadataSchema administrativeUnitSchema = types.getSchema(
				com.constellio.app.modules.rm.wrappers.AdministrativeUnit.DEFAULT_SCHEMA);
		MetadataSchema categorySchema = types.getSchema(Category.DEFAULT_SCHEMA);

		Transaction transaction = new Transaction();
		setupAdministrativeUnits(administrativeUnitSchema, transaction);
		setupCategories(categorySchema, transaction);
		transaction.add(rm.newDocumentTypeWithId(zeFirstType).setCode("Ze code").setTitle("ze title"));
		transaction.add(rm.newDocumentTypeWithId(anotherType).setCode("Another code").setTitle("A title"));
		recordServices.execute(transaction);

		principalAnalogicRetentionRule = copyBuilder.newCopyRetentionRule();
		principalAnalogicRetentionRule.setCode("rule1");
		principalAnalogicRetentionRule.setCopyType(CopyType.PRINCIPAL);
		principalAnalogicRetentionRule.setMediumTypeIds(asList(rm.PA(), rm.FI()));
		principalAnalogicRetentionRule.setContentTypesComment("R1");
		principalAnalogicRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		principalAnalogicRetentionRule.setActiveRetentionComment("R2");
		principalAnalogicRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(3));
		principalAnalogicRetentionRule.setSemiActiveRetentionComment("R3");
		principalAnalogicRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
		principalAnalogicRetentionRule.setCode("R4");

		principalNumericRetentionRule = copyBuilder.newCopyRetentionRule();
		principalNumericRetentionRule.setCode("rule2");
		principalNumericRetentionRule.setCopyType(CopyType.PRINCIPAL);
		principalNumericRetentionRule.setMediumTypeIds(asList(rm.DM()));
		principalNumericRetentionRule.setContentTypesComment("R5");
		principalNumericRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		principalNumericRetentionRule.setActiveRetentionComment("R6");
		principalNumericRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(1));
		principalNumericRetentionRule.setSemiActiveRetentionComment("R7");
		principalNumericRetentionRule.setInactiveDisposalType(DisposalType.SORT);
		principalNumericRetentionRule.setCode("R8");

		secondaryRetentionRule = copyBuilder.newCopyRetentionRule();
		secondaryRetentionRule.setCode("rule3");
		secondaryRetentionRule.setCopyType(CopyType.SECONDARY);
		secondaryRetentionRule.setMediumTypeIds(asList(rm.PA(), rm.FI(), rm.DM()));
		secondaryRetentionRule.setContentTypesComment("R9");
		secondaryRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(10));
		secondaryRetentionRule.setActiveRetentionComment("R10");
		secondaryRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		secondaryRetentionRule.setSemiActiveRetentionComment("R11");
		secondaryRetentionRule.setInactiveDisposalType(DisposalType.DEPOSIT);
		secondaryRetentionRule.setCode("R12");

	}

	@Test
	public void whenSaveRetentionRuleThenAllValuesAreValid()
			throws Exception {

		VariableRetentionPeriod period42 = rm.newVariableRetentionPeriod().setCode("42").setTitle("Ze 42");
		VariableRetentionPeriod period666 = rm.newVariableRetentionPeriod().setCode("666").setTitle("Ze 666");

		CopyRetentionRule principal = copyBuilder.newPrincipal(asList("PA"))
				.setActiveRetentionPeriod(RetentionPeriod.variable(period42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.variable(period666))
				.setInactiveDisposalType(DisposalType.DEPOSIT);

		CopyRetentionRule secondary = copyBuilder.newSecondary(asList("PA"))
				.setActiveRetentionPeriod(RetentionPeriod.fixed(42))
				.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(666))
				.setInactiveDisposalType(DisposalType.DEPOSIT);

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		retentionRule.setAdministrativeUnits(asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setCopyRetentionRules(asList(principal, secondary));

		List<RetentionRuleDocumentType> documentTypes = asList(
				new RetentionRuleDocumentType(zeFirstType, DisposalType.DEPOSIT),
				new RetentionRuleDocumentType(anotherType, DisposalType.DESTRUCTION)
		);
		retentionRule.setDocumentTypesDetails(documentTypes);

		recordServices.add(retentionRuleRecord);
		RetentionRule savedRetentionRule = new RetentionRule(recordServices.getDocumentById(retentionRuleRecord.getId()), types);

		assertThat(savedRetentionRule.getPrincipalCopies()).hasSize(1);
		assertThat(savedRetentionRule.getPrincipalCopies().get(0).getActiveRetentionPeriod().isVariablePeriod()).isTrue();
		assertThat(savedRetentionRule.getPrincipalCopies().get(0).getActiveRetentionPeriod().getVariablePeriodCode())
				.isEqualTo("42");
		assertThat(savedRetentionRule.getPrincipalCopies().get(0).getSemiActiveRetentionPeriod().isVariablePeriod()).isTrue();
		assertThat(savedRetentionRule.getPrincipalCopies().get(0).getSemiActiveRetentionPeriod().getVariablePeriodCode())
				.isEqualTo("666");

		assertThat(savedRetentionRule.getSecondaryCopy().getActiveRetentionPeriod().isVariablePeriod()).isFalse();
		assertThat(savedRetentionRule.getSecondaryCopy().getActiveRetentionPeriod().getFixedValue()).isEqualTo(42);
		assertThat(savedRetentionRule.getSecondaryCopy().getSemiActiveRetentionPeriod().isVariablePeriod()).isFalse();
		assertThat(savedRetentionRule.getSecondaryCopy().getSemiActiveRetentionPeriod().getFixedValue()).isEqualTo(666);

	}

	@Test
	public void whenSaveRetentionRuleThenAllValuesAreValid2()
			throws Exception {

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(new ArrayList<String>());
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setResponsibleAdministrativeUnits(true);
		retentionRule.setCopyRetentionRules(
				asList(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule));

		recordServices.add(retentionRuleRecord);
		RetentionRule savedRetentionRule = new RetentionRule(recordServices.getDocumentById(retentionRuleRecord.getId()), types);

		assertThat(savedRetentionRule.isApproved()).isTrue();
		assertThat(savedRetentionRule.getApprovalDate()).isEqualTo(january31_2014);
		assertThat(savedRetentionRule.getCode()).isEqualTo("zeCode");
		assertThat(savedRetentionRule.getTitle()).isEqualTo("zeTitle");
		assertThat(savedRetentionRule.isResponsibleAdministrativeUnits()).isTrue();
		assertThat(savedRetentionRule.getAdministrativeUnits()).isEmpty();
		assertThat(savedRetentionRule.getCopyRetentionRules())
				.containsOnly(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule);
		assertThat(savedRetentionRule.getPrincipalCopies()).containsOnlyOnce(principalAnalogicRetentionRule,
				principalNumericRetentionRule);
		assertThat(savedRetentionRule.getSecondaryCopy()).isEqualTo(secondaryRetentionRule);

	}

	@Test
	public void whenSaveRetentionRuleWithSpecialVariablePeriodThenSavedCorrectly()
			throws Exception {

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(new ArrayList<String>());
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setResponsibleAdministrativeUnits(true);
		retentionRule.setCopyRetentionRules(
				asList(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule));

		recordServices.add(retentionRuleRecord);
		RetentionRule savedRetentionRule = new RetentionRule(recordServices.getDocumentById(retentionRuleRecord.getId()), types);

		assertThat(savedRetentionRule.isApproved()).isTrue();
		assertThat(savedRetentionRule.getApprovalDate()).isEqualTo(january31_2014);
		assertThat(savedRetentionRule.getCode()).isEqualTo("zeCode");
		assertThat(savedRetentionRule.getTitle()).isEqualTo("zeTitle");
		assertThat(savedRetentionRule.isResponsibleAdministrativeUnits()).isTrue();
		assertThat(savedRetentionRule.getAdministrativeUnits()).isEmpty();
		assertThat(savedRetentionRule.getCopyRetentionRules())
				.containsOnly(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule);
		assertThat(savedRetentionRule.getPrincipalCopies()).containsOnlyOnce(principalAnalogicRetentionRule,
				principalNumericRetentionRule);
		assertThat(savedRetentionRule.getSecondaryCopy()).isEqualTo(secondaryRetentionRule);

	}

	@Test
	public void whenSavingRetentionRuleWithInvalidCopyRetentionRulesThenValidationException()
			throws Exception {

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setResponsibleAdministrativeUnits(true);
		retentionRule.setCopyRetentionRules(asList(copyBuilder.newCopyRetentionRule()));

		try {
			recordServices.add(retentionRuleRecord);
		} catch (RecordServicesException.ValidationException e) {
			List<ValidationError> errors = e.getErrors().getValidationErrors();
			assertThat(errors).hasSize(4);
		}

	}

	@Test
	public void whenSavingRetentionRuleWithDocumentTypeInFolderCopyRuleThenValidationException()
			throws Exception {

		String pa = rm.getMediumTypeByCode("PA").getId();
		String dm = rm.getMediumTypeByCode("DM").getId();

		Transaction transaction = new Transaction();
		String type1 = transaction.add(rm.newDocumentType().setCode("code1").setTitle("title1")).getId();
		String type2 = transaction.add(rm.newDocumentType().setCode("code2").setTitle("title2")).getId();

		recordServices.execute(transaction);

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");

		retentionRule.setCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(pa), "1-0-D").setTypeId(type1),
				copyBuilder.newPrincipal(asList(pa), "2-0-D"),
				copyBuilder.newSecondary(asList(pa), "3-0-D")
		));

		try {
			recordServices.add(retentionRuleRecord);
			fail("exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).extracting("code").containsOnly(
					"com.constellio.model.services.schemas.validators.AllowedReferencesValidator_unallowedReferenceForMetadata");
		}

	}

	@Test
	public void whenSavingDocumentRetentionRuleWithFolderTypeInDocumentCopyRuleThenValidationException()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		String pa = rm.getMediumTypeByCode("PA").getId();

		Transaction transaction = new Transaction();
		String type1 = transaction.add(rm.newFolderType().setCode("code1").setTitle("title1")).getId();
		String type2 = transaction.add(rm.newFolderType().setCode("code2").setTitle("title2")).getId();

		recordServices.execute(transaction);

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setScope(RetentionRuleScope.DOCUMENTS);

		retentionRule.setPrincipalDefaultDocumentCopyRetentionRule(copyBuilder.newPrincipal(asList(pa), "2-0-D"));
		retentionRule.setSecondaryDefaultDocumentCopyRetentionRule(copyBuilder.newSecondary(asList(pa), "2-0-D"));

		retentionRule.setDocumentCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(pa), "2-0-D").setTypeId(type1),
				copyBuilder.newPrincipal(asList(pa), "3-0-D").setTypeId(type2)
		));

		try {
			recordServices.add(retentionRuleRecord);
			fail("exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).extracting("code").containsOnly(
					"com.constellio.model.services.schemas.validators.AllowedReferencesValidator_unallowedReferenceForMetadata");
		}

	}

	@Test
	public void whenSavingDocumentRetentionRuleWithDocumentTypeInDocumentCopyRulesAndManuallySpecifiedDocumentTypesThenMergedInCalculatedMetadata()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		String pa = rm.getMediumTypeByCode("PA").getId();

		Transaction transaction = new Transaction();
		String type1 = transaction.add(rm.newDocumentType().setCode("code1").setTitle("title1")).getId();
		String type2 = transaction.add(rm.newDocumentType().setCode("code2").setTitle("title2")).getId();
		String type3 = transaction.add(rm.newDocumentType().setCode("code3").setTitle("title3")).getId();

		recordServices.execute(transaction);

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		retentionRule.setDocumentTypesDetails(asList(
				new RetentionRuleDocumentType(type2),
				new RetentionRuleDocumentType(type3)
		));

		retentionRule.setCopyRetentionRules(
				copyBuilder.newPrincipal(asList(pa), "2-0-D"),
				copyBuilder.newSecondary(asList(pa), "2-0-D"));

		retentionRule.setDocumentCopyRetentionRules(asList(
				copyBuilder.newPrincipal(asList(pa), "2-0-D").setTypeId(type1),
				copyBuilder.newPrincipal(asList(pa), "3-0-D").setTypeId(type2)
		));

		recordServices.add(retentionRuleRecord);

		assertThat(retentionRule.getDocumentTypes()).containsOnly(type1, type2, type3).hasSize(3);
	}

	@Test
	public void whenSavingRetentionRuleWithFolderTypeInCopyRulesThenCopiedInCalculatedMetadata()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		String pa = rm.getMediumTypeByCode("PA").getId();

		Transaction transaction = new Transaction();
		String type1 = transaction.add(rm.newFolderType().setCode("code1").setTitle("title1")).getId();
		String type2 = transaction.add(rm.newFolderType().setCode("code2").setTitle("title2")).getId();
		String type3 = transaction.add(rm.newFolderType().setCode("code3").setTitle("title3")).getId();

		recordServices.execute(transaction);

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		retentionRule.setAdministrativeUnits(asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);

		retentionRule.setCopyRetentionRules(
				copyBuilder.newPrincipal(asList(pa), "1-0-D").setTypeId(type1),
				copyBuilder.newPrincipal(asList(pa), "2-0-D").setTypeId(type2),
				copyBuilder.newPrincipal(asList(pa), "3-0-D"),
				copyBuilder.newPrincipal(asList(pa), "4-0-D").setTypeId(type1),
				copyBuilder.newSecondary(asList(pa), "5-0-D"));

		recordServices.add(retentionRuleRecord);

		assertThat(retentionRule.getFolderTypes()).containsOnly(type1, type2).hasSize(2);
	}

	// ---------------------------------------------------------------

	private void setupAdministrativeUnits(MetadataSchema administrativeUnitSchema, Transaction transaction) {
		AdministrativeUnit anAdministrativeUnit = new AdministrativeUnit(
				recordServices.newRecordWithSchema(administrativeUnitSchema), types);
		AdministrativeUnit anotherAdministrativeUnit = new AdministrativeUnit(
				recordServices.newRecordWithSchema(administrativeUnitSchema), types);

		anAdministrativeUnit.setCode("anAdministrativeUnit");
		anAdministrativeUnit.setTitle("An administrative unit");

		anotherAdministrativeUnit.setCode("anotherAdministrativeUnit");
		anotherAdministrativeUnit.setTitle("Another administrative unit");

		anAdministrativeUnitId = anAdministrativeUnit.getId();
		anotherAdministrativeUnitId = anotherAdministrativeUnit.getId();
		transaction.add(anAdministrativeUnit.getWrappedRecord());
		transaction.add(anotherAdministrativeUnit.getWrappedRecord());
	}

	private void setupCategories(MetadataSchema categoriesSchema, Transaction transaction) {
		Category aCategory = new Category(recordServices.newRecordWithSchema(categoriesSchema), types);
		Category anotherCategory = new Category(recordServices.newRecordWithSchema(categoriesSchema), types);

		aCategory.setCode("aCategory");
		aCategory.setTitle("A category");

		anotherCategory.setCode("anotherCategory");
		anotherCategory.setTitle("Another category");

		aCategoryId = aCategory.getId();
		anotherCategoryId = anotherCategory.getId();
		transaction.add(aCategory.getWrappedRecord());
		transaction.add(anotherCategory.getWrappedRecord());
	}

}
