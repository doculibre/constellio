package com.constellio.model.services.records;

import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelLayerLogger;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.DaysBetweenMultivalueLocalDateAndAnotherSchemaRequiredDateCalculator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.joda.time.LocalDate;
import org.mockito.Mock;

import java.util.List;

public class RecordAutomaticMetadataServicesCalculationTest extends ConstellioTest {

	Transaction zeTransaction = new Transaction();
	RecordUpdateOptions options = zeTransaction.getRecordUpdateOptions();
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

	@Mock SearchServices searchServices;

	@Mock ModelLayerFactory modelLayerFactory;

	TransactionExecutionContext context = new TransactionExecutionContext(null);
	TransactionExecutionRecordContext recordContext = new TransactionExecutionRecordContext(record, context);
//
	//	@SuppressWarnings("unchecked")
	//	@Before
	//	public void setUp()
	//			throws Exception {
	//
	//		schemas = new TestsSchemasSetup();
	//		zeSchema = schemas.new ZeSchemaMetadatas();
	//		anotherSchema = schemas.new AnotherSchemaMetadatas();
	//		define(schemasManager).using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));
	//
	//		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
	//		when(modelLayerFactory.getSystemConfigurationsManager()).thenReturn(systemConfigurationsManager);
	//		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(schemasManager);
	//		when(modelLayerFactory.getModelLayerLogger()).thenReturn(modelLayerLogger);
	//
	//		services = spy(new RecordAutomaticMetadataServices(modelLayerFactory));
	//
	//		record = spy(new TestRecord(zeSchema));
	//
	//		otherRecord = spy(new TestRecord(anotherSchema));
	//
	//		configureCalculatorDependencies();
	//		when(calculator.getDependencies()).thenReturn(dependencies);
	//
	//		reset(schemasManager.getSchemaTypes(zeCollection));
	//
	//		reindexedMetadata = new TransactionRecordsReindexation(new MetadataList(firstReindexedMetadata, secondReindexedMetadata));
	//
	//	}
//
	//	@Test
	//	public void givenIdentifierSpecialDependencyWhenAddValuesThenAddId() {
	//		Map<Dependency, Object> valuesMap = new HashMap<>();
	//		RecordImpl zeRecord = new TestRecord("zeSchema", "zeCollection", "zeId");
	//
	//		services.addValuesFromSpecialDependencies(context, zeRecord, recordProvider, valuesMap, SpecialDependencies.IDENTIFIER);
	//
	//		assertThat(valuesMap).containsEntry(SpecialDependencies.IDENTIFIER, "zeId").hasSize(1);
	//	}
	//
	//	@Test
	//	public void givenDependencyModifiedWhenSettingCalculatedValuesInRecordThenValueCalculated() {
	//		doNothing().when(services)
	//				.calculateValueInRecord(eq(recordContext), any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
	//						any(MetadataSchemaTypes.class), any(Transaction.class));
	//		doReturn(true).when(services).calculatorDependencyModified(any(RecordImpl.class), any(MetadataValueCalculator.class),
	//				any(MetadataSchemaTypes.class), any(Metadata.class));
	//
	//		services.setCalculatedValuesInRecords(recordContext, record, zeSchema.calculatedDaysBetween(), recordProvider,
	//				reindexedMetadata,
	//				schemas.getTypes(), zeTransaction);
	//
	//		verify(services)
	//				.calculateValueInRecord(recordContext, record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes(),
	//						zeTransaction);
	//	}
	//
	//	@Test
	//	public void givenDependencyNotModifiedWhenSettingCalculatedValuesInRecordThenValueCalculated() {
	//		doNothing().when(services)
	//				.calculateValueInRecord(eq(recordContext), any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
	//						any(MetadataSchemaTypes.class), any(Transaction.class));
	//		doReturn(false).when(services).calculatorDependencyModified(any(RecordImpl.class), any(MetadataValueCalculator.class),
	//				any(MetadataSchemaTypes.class), any(Metadata.class));
	//
	//		services.setCalculatedValuesInRecords(recordContext, record, zeSchema.calculatedDaysBetween(), recordProvider,
	//				reindexedMetadata,
	//				schemas.getTypes(), zeTransaction);
	//
	//		verify(services, never())
	//				.calculateValueInRecord(recordContext, record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes(),
	//						zeTransaction);
	//	}
	//
	//	@Test
	//	public void givenDependencyValueModifiedInRecordWhenVerifyingIsModifiedThenReturnTrue() {
	//		doReturn(true).when(record).isModified(any(Metadata.class));
	//
	//		assertThat(services.calculatorDependencyModified(record, ((CalculatedDataEntry) zeSchema.calculatedDaysBetween()
	//				.getDataEntry()).getCalculator(), schemas.getTypes(), mock(Metadata.class)));
	//	}
	//
	//	@Test
	//	public void givenDependencyValueNotModifiedInRecordWhenVerifyingIsModifiedThenReturnTrue() {
	//		doReturn(false).when(record).isModified(any(Metadata.class));
	//
	//		assertThat(services.calculatorDependencyModified(record, ((CalculatedDataEntry) zeSchema.calculatedDaysBetween()
	//				.getDataEntry()).getCalculator(), schemas.getTypes(), mock(Metadata.class)));
	//	}
	//
	//	@Test
	//	public void whenUpdatingAutomaticValuesInRecordsThenSetValueForAllMetadatas()
	//			throws Exception {
	//		doNothing().when(services)
	//				.setCalculatedValuesInRecords(any(TransactionExecutionRecordContext.class), any(RecordImpl.class), any(Metadata.class),
	//						any(RecordProvider.class), eq(reindexedMetadata), any(MetadataSchemaTypes.class), any(Transaction.class));
	//
	//		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);
	//
	//		Metadata calculatedDaysBetween = zeSchema.calculatedDaysBetween();
	//		verify(services).setCalculatedValuesInRecords(any(TransactionExecutionRecordContext.class), eq(record),
	//				eq(calculatedDaysBetween), eq(recordProvider), eq(reindexedMetadata), eq(schemas.getTypes()), eq(zeTransaction));
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Test
	//	public void givenRequiredDependencyUndefinedWhenCalculateValuesInRecordThenUseDefaultValue() {
	//		doReturn(false).when(services)
	//				.addValuesFromDependencies(eq(context), any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
	//						any(MetadataValueCalculator.class), any(Map.class), any(MetadataSchemaTypes.class),
	//						any(Transaction.class), any(Locale.class), any(LocalisedRecordMetadataRetrieval.class));
	//
	//		services.calculateValueInRecord(recordContext, record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes(),
	//				zeTransaction);
	//
	//		verify(record).updateAutomaticValue(zeSchema.calculatedDaysBetween(), -1.0, Locale.FRENCH);
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Test
	//	public void givenRequiredDependencyDefinedWhenCalculateValuesInRecordThenCalculateValue() {
	//		doReturn(true).when(services)
	//				.addValuesFromDependencies(eq(context), any(RecordImpl.class), any(Metadata.class), any(RecordProvider.class),
	//						any(MetadataValueCalculator.class), any(Map.class), any(MetadataSchemaTypes.class),
	//						any(Transaction.class), any(Locale.class), any(LocalisedRecordMetadataRetrieval.class));
	//		doReturn(calculator).when(services).getCalculatorFrom(any(Metadata.class));
	//
	//		services.calculateValueInRecord(recordContext, record, zeSchema.calculatedDaysBetween(), recordProvider, schemas.getTypes(),
	//				zeTransaction);
	//
	//		verify(record).updateAutomaticValue(eq(zeSchema.calculatedDaysBetween()), anyObject(), any(Locale.class));
	//		verify(calculator).calculate(any(CalculatorParameters.class));
	//	}
	//
	//	@SuppressWarnings({"unchecked", "rawtypes"})
	//	@Test
	//	public void givenTwoLocalAndOneReferenceDependenciesWhenGetValuesFromDependenciesThenAllValueRetrieved() {
	//		doReturn(true).when(services)
	//				.addValueForLocalDependency(any(RecordImpl.class), any(Map.class), any(Dependency.class), any(Locale.class),
	//						any(LocalisedRecordMetadataRetrieval.class));
	//		doReturn(true).when(services).addValueForReferenceDependency(any(RecordImpl.class), any(RecordProvider.class),
	//				any(Map.class), any(ReferenceDependency.class), any(RecordUpdateOptions.class), any(Locale.class),
	//				any(LocalisedRecordMetadataRetrieval.class));
	//		Map aMap = mock(Map.class);
	//		services.addValuesFromDependencies(context, record, mock(Metadata.class), recordProvider, calculator, aMap,
	//				schemas.getTypes(),
	//				zeTransaction, Locale.FRENCH, LocalisedRecordMetadataRetrieval.PREFERRING);
	//
	//		verify(services).addValueForLocalDependency(eq(record), eq(aMap), eq(aLocalDependency), any(Locale.class),
	//				any(LocalisedRecordMetadataRetrieval.class));
	//		verify(services).addValueForLocalDependency(eq(record), eq(aMap), eq(anotherLocalDependency), any(Locale.class),
	//				any(LocalisedRecordMetadataRetrieval.class));
	//		verify(services)
	//				.addValueForReferenceDependency(eq(record), eq(recordProvider), eq(aMap), eq(aReferenceDependency), eq(options),
	//						any(Locale.class), any(LocalisedRecordMetadataRetrieval.class));
	//	}
	//
	//	@SuppressWarnings({"rawtypes", "unchecked"})
	//	@Test
	//	public void givenLocalDependencyRequiredButNullWhenGettingValueThenReturnTrueAndDontAddValue() {
	//		Map aMap = mock(Map.class);
	//		Metadata aMetadata = mock(Metadata.class);
	//		when(aLocalDependency.isRequired()).thenReturn(true);
	//		doReturn(aMetadata).when(services).getMetadataFromDependency(any(RecordImpl.class), any(Dependency.class));
	//		doReturn(null).when(record).get(any(Metadata.class));
	//
	//		assertThat(services.addValueForLocalDependency(record, aMap, aLocalDependency, Locale.FRENCH,
	//				LocalisedRecordMetadataRetrieval.PREFERRING)).isFalse();
	//		verify(aMap, never()).put(any(Dependency.class), anyObject());
	//	}
	//
	//	@SuppressWarnings({"rawtypes", "unchecked"})
	//	@Test
	//	public void givenLocalDependencyRequiredAndNotNullWhenGettingValueThenReturnTrueAndAddValue() {
	//		Map aMap = mock(Map.class);
	//		Metadata aMetadata = mock(Metadata.class);
	//		when(aLocalDependency.isRequired()).thenReturn(true);
	//		doReturn(aMetadata).when(services).getMetadataFromDependency(any(RecordImpl.class), any(Dependency.class));
	//		doReturn("aValue").when(record)
	//				.get(any(Metadata.class), eq(Locale.KOREA), eq(LocalisedRecordMetadataRetrieval.PREFERRING));
	//
	//		assertThat(services.addValueForLocalDependency(record, aMap, aLocalDependency, Locale.KOREA,
	//				LocalisedRecordMetadataRetrieval.PREFERRING)).isTrue();
	//		verify(aMap).put(aLocalDependency, "aValue");
	//	}
	//
	//	@SuppressWarnings({"rawtypes", "unchecked"})
	//	@Test
	//	public void givenReferenceDependencyRequiredButNullWhenGettingValueThenReturnTrueAndDontAddValue() {
	//		Map aMap = mock(Map.class);
	//		Metadata aReferenceMetadata = mock(Metadata.class);
	//		Metadata theReferencedMetadata = mock(Metadata.class);
	//		doReturn(theReferencedMetadata).when(services).getDependentMetadataFromDependency(any(ReferenceDependency.class),
	//				eq(otherRecord));
	//		when(aReferenceDependency.isRequired()).thenReturn(true);
	//		doReturn(aReferenceMetadata).when(services).getMetadataFromDependency(record, aReferenceDependency);
	//		doReturn("otherRecordId").when(record).get(aReferenceMetadata);
	//		when(recordProvider.getRecord("otherRecordId")).thenReturn(otherRecord);
	//
	//		assertThat(services.addValueForReferenceDependency(record, recordProvider, aMap, aReferenceDependency, options,
	//				Locale.FRENCH, STRICT))
	//				.isFalse();
	//		verify(aMap, never()).put(any(Dependency.class), anyObject());
	//	}
	//
	//	@SuppressWarnings({"rawtypes", "unchecked"})
	//	@Test
	//	public void givenReferenceDependencyRequiredAndNotNullWhenGettingValueThenReturnTrueAndAddValue() {
	//		Map aMap = mock(Map.class);
	//		MetadataSchemaType referencedSchemaType = mock(MetadataSchemaType.class);
	//		MetadataSchema referencedDefaultSchema = mock(MetadataSchema.class);
	//		when(referencedSchemaType.getDefaultSchema()).thenReturn(referencedDefaultSchema);
	//
	//
	//		when(referencedSchemaType.getCacheType()).thenReturn(RecordCacheType.NOT_CACHED);
	//		Metadata aReferenceMetadata = mock(Metadata.class);
	//		Metadata theReferencedMetadata = mock(Metadata.class);
	//		when(aReferenceMetadata.getReferencedSchemaType()).thenReturn(referencedSchemaType);
	//		when(referencedDefaultSchema.getMetadata("theReferencedMetadata")).thenReturn(theReferencedMetadata);
	//		when(theReferencedMetadata.getSchemaType()).thenReturn(referencedSchemaType);
	//		when(aReferenceDependency.isRequired()).thenReturn(true);
	//		when(aReferenceDependency.getDependentMetadataCode()).thenReturn("theReferencedMetadata");
	//		doReturn(aReferenceMetadata).when(services).getMetadataFromDependency(record, aReferenceDependency);
	//		doReturn(theReferencedMetadata).when(services).getDependentMetadataFromDependency(any(ReferenceDependency.class),
	//				eq(otherRecord));
	//		doReturn("otherRecordId").when(record).get(aReferenceMetadata, Locale.FRENCH, STRICT);
	//		when(recordProvider.getRecord("otherRecordId")).thenReturn(otherRecord);
	//		doReturn("aValue").when(otherRecord).get(theReferencedMetadata, Locale.FRENCH, STRICT);
	//
	//		assertThat(services.addValueForReferenceDependency(record, recordProvider, aMap, aReferenceDependency, options,
	//				Locale.FRENCH, STRICT)).isTrue();
	//		verify(aMap).put(aReferenceDependency, "aValue");
	//	}
	//
	//
	//	@SuppressWarnings({"unchecked", "rawtypes"})
	//	private void configureCalculatorDependencies() {
	//		dependencies = new ArrayList();
	//		dependencies.add(aLocalDependency);
	//		dependencies.add(anotherLocalDependency);
	//		dependencies.add(aReferenceDependency);
	//	}
}
