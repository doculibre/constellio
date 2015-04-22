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
package com.constellio.app.modules.rm.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
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

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
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

		principalAnalogicRetentionRule = new CopyRetentionRule();
		principalAnalogicRetentionRule.setCode("rule1");
		principalAnalogicRetentionRule.setCopyType(CopyType.PRINCIPAL);
		principalAnalogicRetentionRule.setMediumTypeIds(Arrays.asList(rm.PA(), rm.FI()));
		principalAnalogicRetentionRule.setContentTypesComment("R1");
		principalAnalogicRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		principalAnalogicRetentionRule.setActiveRetentionComment("R2");
		principalAnalogicRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(3));
		principalAnalogicRetentionRule.setSemiActiveRetentionComment("R3");
		principalAnalogicRetentionRule.setInactiveDisposalType(DisposalType.DESTRUCTION);
		principalAnalogicRetentionRule.setCode("R4");

		principalNumericRetentionRule = new CopyRetentionRule();
		principalNumericRetentionRule.setCode("rule2");
		principalNumericRetentionRule.setCopyType(CopyType.PRINCIPAL);
		principalNumericRetentionRule.setMediumTypeIds(Arrays.asList(rm.DM()));
		principalNumericRetentionRule.setContentTypesComment("R5");
		principalNumericRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		principalNumericRetentionRule.setActiveRetentionComment("R6");
		principalNumericRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(1));
		principalNumericRetentionRule.setSemiActiveRetentionComment("R7");
		principalNumericRetentionRule.setInactiveDisposalType(DisposalType.SORT);
		principalNumericRetentionRule.setCode("R8");

		secondaryRetentionRule = new CopyRetentionRule();
		secondaryRetentionRule.setCode("rule3");
		secondaryRetentionRule.setCopyType(CopyType.SECONDARY);
		secondaryRetentionRule.setMediumTypeIds(Arrays.asList(rm.PA(), rm.FI(), rm.DM()));
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

		Record retentionRuleRecord = recordServices.newRecordWithSchema(retentionRuleSchema);
		RetentionRule retentionRule = new RetentionRule(retentionRuleRecord, types);
		assertThat(retentionRule.isApproved()).isFalse();
		assertThat(retentionRule.isResponsibleAdministrativeUnits()).isFalse();
		assertThat(retentionRule.isConfidentialDocuments()).isFalse();
		assertThat(retentionRule.isEssentialDocuments()).isFalse();
		retentionRule.setAdministrativeUnits(Arrays.asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setResponsibleAdministrativeUnits(false);
		retentionRule.setCopyRetentionRules(
				Arrays.asList(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule));
		retentionRule.setHistory(history);
		retentionRule.setConfidentialDocuments(true);
		retentionRule.setEssentialDocuments(true);
		retentionRule.setKeywords(Arrays.asList(keyword1, keyword2));

		List<RetentionRuleDocumentType> documentTypes = Arrays.asList(
				new RetentionRuleDocumentType(zeFirstType, DisposalType.DEPOSIT),
				new RetentionRuleDocumentType(anotherType, DisposalType.DESTRUCTION)
		);

		retentionRule.setDocumentTypesDetails(documentTypes);

		recordServices.add(retentionRuleRecord);
		RetentionRule savedRetentionRule = new RetentionRule(recordServices.getDocumentById(retentionRuleRecord.getId()), types);

		assertThat(savedRetentionRule.isApproved()).isTrue();
		assertThat(savedRetentionRule.getApprovalDate()).isEqualTo(january31_2014);
		assertThat(savedRetentionRule.getCode()).isEqualTo("zeCode");
		assertThat(savedRetentionRule.getTitle()).isEqualTo("zeTitle");
		assertThat(savedRetentionRule.isResponsibleAdministrativeUnits()).isFalse();
		assertThat(savedRetentionRule.getAdministrativeUnits()).containsOnly(anAdministrativeUnitId, anotherAdministrativeUnitId);
		assertThat(savedRetentionRule.getCopyRetentionRules())
				.containsOnly(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule);
		assertThat(savedRetentionRule.getPrincipalCopies()).containsOnlyOnce(principalAnalogicRetentionRule,
				principalNumericRetentionRule);
		assertThat(savedRetentionRule.getSecondaryCopy()).isEqualTo(secondaryRetentionRule);

		assertThat(savedRetentionRule.getDocumentTypes()).isEqualTo(Arrays.asList(zeFirstType, anotherType));
		assertThat(savedRetentionRule.getDocumentTypesDetails()).isEqualTo(documentTypes);

		assertThat(savedRetentionRule.getHistory()).isEqualTo(history);
		assertThat(savedRetentionRule.isEssentialDocuments()).isTrue();
		assertThat(savedRetentionRule.isConfidentialDocuments()).isTrue();
		assertThat(savedRetentionRule.getKeywords()).containsExactly(keyword1, keyword2);
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
				Arrays.asList(principalAnalogicRetentionRule, principalNumericRetentionRule, secondaryRetentionRule));

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
		retentionRule.setAdministrativeUnits(Arrays.asList(anAdministrativeUnitId, anotherAdministrativeUnitId));
		retentionRule.setApproved(true);
		retentionRule.setApprovalDate(january31_2014);
		retentionRule.setCode("zeCode");
		retentionRule.setTitle("zeTitle");
		retentionRule.setResponsibleAdministrativeUnits(true);
		retentionRule.setCopyRetentionRules(Arrays.asList(new CopyRetentionRule()));

		try {
			recordServices.add(retentionRuleRecord);
		} catch (RecordServicesException.ValidationException e) {
			List<ValidationError> errors = e.getErrors().getValidationErrors();
			assertThat(errors).hasSize(4);
		}
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
