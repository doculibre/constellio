package com.constellio.model.services.records;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerLogger;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.DaysBetweenMultivalueLocalDateAndAnotherSchemaRequiredDateCalculator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordAutomaticMetadataServicesCalculationTest extends ConstellioTest {

	RecordAutomaticMetadataServices services;
	@Mock RecordProvider recordProvider;

	@Mock MetadataSchemasManager schemasManager;

	@Mock ModelLayerLogger modelLayerLogger;

	@Mock DaysBetweenMultivalueLocalDateAndAnotherSchemaRequiredDateCalculator calculator;

	@SuppressWarnings("rawtypes") @Mock LocalDependency aLocalDependency;
	@SuppressWarnings("rawtypes") @Mock LocalDependency anotherLocalDependency;
	@SuppressWarnings("rawtypes") @Mock ReferenceDependency aReferenceDependency;

	String idReferencedRecordWithJan1DateValue, idReferencedRecordWithJan2DateValue, idReferencedRecordWithoutDateValue;

	LocalDate jan1 = new LocalDate(2014, 1, 1);
	LocalDate jan2 = new LocalDate(2014, 1, 2);
	LocalDate jan3 = new LocalDate(2014, 1, 3);

	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;

	RecordImpl record;
	RecordImpl otherRecord;
	@SuppressWarnings("rawtypes") List dependencies;

	@Mock Metadata firstReindexedMetadata, secondReindexedMetadata;
	TransactionRecordsReindexation reindexedMetadata;

	@Mock TaxonomiesManager taxonomiesManager;
	@Mock SystemConfigurationsManager systemConfigurationsManager;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp()
			throws Exception {

		schemas = new TestsSchemasSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		define(schemasManager).using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));

		services = spy(new RecordAutomaticMetadataServices(schemasManager, taxonomiesManager, systemConfigurationsManager,
				modelLayerLogger));

		record = spy(new TestRecord(zeSchema));

		otherRecord = spy(new TestRecord(anotherSchema));

		configureCalculatorDependencies();
		when(calculator.getDependencies()).thenReturn(dependencies);

		reset(schemasManager.getSchemaTypes(zeCollection));

		reindexedMetadata = new TransactionRecordsReindexation(new MetadataList(firstReindexedMetadata, secondReindexedMetadata));

	}

	@Test
	public void givenIdentifierSpecialDependencyWhenAddValuesThenAddId() {
		Map<Dependency, Object> valuesMap = new HashMap<>();
		RecordImpl zeRecord = new TestRecord("zeSchema", "zeCollection", "zeId");

		services.addValuesFromSpecialDependencies(zeRecord, recordProvider, valuesMap, SpecialDependencies.IDENTIFIER);

		assertThat(valuesMap).containsEntry(SpecialDependencies.IDENTIFIER, "zeId").hasSize(1);
	}

	@Test
	public void givenDependencyModifiedWhenSettingCalculatedValuesInRecordThenValueCalculated() {
		doNothing().when(services).calculateValueInRecord(any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
				any(MetadataSchemaTypes.class));
		doReturn(true).when(services).calculatorDependencyModified(any(RecordImpl.class), any(MetadataValueCalculator.class),
				any(MetadataSchemaTypes.class), any(Metadata.class));

		services.setCalculatedValuesInRecords(record, zeSchema.calculatedDaysBetween(), recordProvider, reindexedMetadata,
				schemas.getTypes());

		verify(services).calculateValueInRecord(record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes());
	}

	@Test
	public void givenDependencyNotModifiedWhenSettingCalculatedValuesInRecordThenValueCalculated() {
		doNothing().when(services).calculateValueInRecord(any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
				any(MetadataSchemaTypes.class));
		doReturn(false).when(services).calculatorDependencyModified(any(RecordImpl.class), any(MetadataValueCalculator.class),
				any(MetadataSchemaTypes.class), any(Metadata.class));

		services.setCalculatedValuesInRecords(record, zeSchema.calculatedDaysBetween(), recordProvider, reindexedMetadata,
				schemas.getTypes());

		verify(services, never())
				.calculateValueInRecord(record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes());
	}

	@Test
	public void givenDependencyValueModifiedInRecordWhenVerifyingIsModifiedThenReturnTrue() {
		doReturn(true).when(record).isModified(any(Metadata.class));

		assertThat(services.calculatorDependencyModified(record, ((CalculatedDataEntry) zeSchema.calculatedDaysBetween()
				.getDataEntry()).getCalculator(), schemas.getTypes(), mock(Metadata.class)));
	}

	@Test
	public void givenDependencyValueNotModifiedInRecordWhenVerifyingIsModifiedThenReturnTrue() {
		doReturn(false).when(record).isModified(any(Metadata.class));

		assertThat(services.calculatorDependencyModified(record, ((CalculatedDataEntry) zeSchema.calculatedDaysBetween()
				.getDataEntry()).getCalculator(), schemas.getTypes(), mock(Metadata.class)));
	}

	@Test
	public void whenUpdatingAutomaticValuesInRecordsThenSetValueForAllMetadatas()
			throws Exception {
		doNothing().when(services).setCalculatedValuesInRecords(any(RecordImpl.class), any(Metadata.class),
				any(RecordProvider.class), eq(reindexedMetadata), any(MetadataSchemaTypes.class));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		verify(services).setCalculatedValuesInRecords(record, zeSchema.calculatedDaysBetween(), recordProvider,
				reindexedMetadata, schemas.getTypes());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenRequiredDependencyUndefinedWhenCalculateValuesInRecordThenUseDefaultValue() {
		doReturn(false).when(services)
				.addValuesFromDependencies(any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
						any(MetadataValueCalculator.class), any(Map.class), any(MetadataSchemaTypes.class));

		services.calculateValueInRecord(record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes());

		verify(record).updateAutomaticValue(zeSchema.calculatedDaysBetween(), -1.0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenRequiredDependencyDefinedWhenCalculateValuesInRecordThenCalculateValue() {
		doReturn(true).when(services)
				.addValuesFromDependencies(any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
						any(MetadataValueCalculator.class), any(Map.class), any(MetadataSchemaTypes.class));
		doReturn(calculator).when(services).getCalculatorFrom(any(Metadata.class));

		services.calculateValueInRecord(record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes());

		verify(record).updateAutomaticValue(eq(zeSchema.calculatedDaysBetween()), anyObject());
		verify(calculator).calculate(any(CalculatorParameters.class));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void givenTwoLocalAndOneReferenceDependenciesWhenGetValuesFromDependenciesThenAllValueRetrieved() {
		doReturn(true).when(services).addValueForLocalDependency(any(RecordImpl.class), any(Map.class), any(Dependency.class));
		doReturn(true).when(services).addValueForReferenceDependency(any(RecordImpl.class), any(RecordProvider.class),
				any(Map.class), any(ReferenceDependency.class));
		Map aMap = mock(Map.class);
		services.addValuesFromDependencies(record, mock(Metadata.class), recordProvider, calculator, aMap, schemas.getTypes());

		verify(services).addValueForLocalDependency(eq(record), eq(aMap), eq(aLocalDependency));
		verify(services).addValueForLocalDependency(eq(record), eq(aMap), eq(anotherLocalDependency));
		verify(services).addValueForReferenceDependency(eq(record), eq(recordProvider), eq(aMap), eq(aReferenceDependency));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenLocalDependencyRequiredButNullWhenGettingValueThenReturnTrueAndDontAddValue() {
		Map aMap = mock(Map.class);
		Metadata aMetadata = mock(Metadata.class);
		when(aLocalDependency.isRequired()).thenReturn(true);
		doReturn(aMetadata).when(services).getMetadataFromDependency(any(RecordImpl.class), any(Dependency.class));
		doReturn(null).when(record).get(any(Metadata.class));

		assertThat(services.addValueForLocalDependency(record, aMap, aLocalDependency)).isFalse();
		verify(aMap, never()).put(any(Dependency.class), anyObject());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenLocalDependencyRequiredAndNotNullWhenGettingValueThenReturnTrueAndAddValue() {
		Map aMap = mock(Map.class);
		Metadata aMetadata = mock(Metadata.class);
		when(aLocalDependency.isRequired()).thenReturn(true);
		doReturn(aMetadata).when(services).getMetadataFromDependency(any(RecordImpl.class), any(Dependency.class));
		doReturn("aValue").when(record).get(any(Metadata.class));

		assertThat(services.addValueForLocalDependency(record, aMap, aLocalDependency)).isTrue();
		verify(aMap).put(aLocalDependency, "aValue");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenReferenceDependencyRequiredButNullWhenGettingValueThenReturnTrueAndDontAddValue() {
		Map aMap = mock(Map.class);
		Metadata aReferenceMetadata = mock(Metadata.class);
		Metadata theReferencedMetadata = mock(Metadata.class);
		doReturn(theReferencedMetadata).when(services).getDependentMetadataFromDependency(any(ReferenceDependency.class),
				eq(otherRecord));
		when(aReferenceDependency.isRequired()).thenReturn(true);
		doReturn(aReferenceMetadata).when(services).getMetadataFromDependency(record, aReferenceDependency);
		doReturn("otherRecordId").when(record).get(aReferenceMetadata);
		when(recordProvider.getRecord("otherRecordId")).thenReturn(otherRecord);

		assertThat(services.addValueForReferenceDependency(record, recordProvider, aMap, aReferenceDependency)).isFalse();
		verify(aMap, never()).put(any(Dependency.class), anyObject());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenReferenceDependencyRequiredAndNotNullWhenGettingValueThenReturnTrueAndAddValue() {
		Map aMap = mock(Map.class);
		Metadata aReferenceMetadata = mock(Metadata.class);
		Metadata theReferencedMetadata = mock(Metadata.class);
		when(aReferenceDependency.isRequired()).thenReturn(true);
		doReturn(aReferenceMetadata).when(services).getMetadataFromDependency(record, aReferenceDependency);
		doReturn(theReferencedMetadata).when(services).getDependentMetadataFromDependency(any(ReferenceDependency.class),
				eq(otherRecord));
		doReturn("otherRecordId").when(record).get(aReferenceMetadata);
		when(recordProvider.getRecord("otherRecordId")).thenReturn(otherRecord);
		doReturn("aValue").when(otherRecord).get(theReferencedMetadata);

		assertThat(services.addValueForReferenceDependency(record, recordProvider, aMap, aReferenceDependency)).isTrue();
		verify(aMap).put(aReferenceDependency, "aValue");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void configureCalculatorDependencies() {
		dependencies = new ArrayList();
		dependencies.add(aLocalDependency);
		dependencies.add(anotherLocalDependency);
		dependencies.add(aReferenceDependency);
	}
}
