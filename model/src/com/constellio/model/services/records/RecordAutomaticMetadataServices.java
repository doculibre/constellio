package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.Stats.CallStatCompiler;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.MultiMetadatasValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams.SearchAggregatedValuesParamsQuery;
import com.constellio.model.entities.schemas.entries.TransactionAggregatedValuesParams;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SingletonSecurityModel;
import com.constellio.model.entities.security.TransactionSecurityModel;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelLayerLogger;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.LogicalSearchQueryExecutionCancelledException;
import com.constellio.model.services.search.LogicalSearchQueryExecutorInCache;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.model.services.security.SecurityModelCache;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.constellio.data.utils.systemLogger.SystemLogger.logImportantWarningOnce;
import static com.constellio.model.entities.enums.GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD;
import static com.constellio.model.services.records.aggregations.MetadataAggregationHandlerFactory.getHandlerFor;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RecordAutomaticMetadataServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordAutomaticMetadataServices.class);

	private final ModelLayerLogger modelLayerLogger;
	private final MetadataSchemasManager schemasManager;
	private final TaxonomiesManager taxonomiesManager;
	private final SystemConfigurationsManager systemConfigurationsManager;
	private final SearchServices searchServices;
	private final ModelLayerFactory modelLayerFactory;

	public RecordAutomaticMetadataServices(ModelLayerFactory modelLayerFactory) {
		super();
		this.modelLayerLogger = modelLayerFactory.getModelLayerLogger();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.modelLayerFactory = modelLayerFactory;
	}

	public void updateAutomaticMetadatas(RecordImpl record, RecordProvider recordProvider,
										 TransactionRecordsReindexation reindexation, Transaction transaction) {
		TransactionExecutionRecordContext context = new TransactionExecutionContext(transaction).contextForRecord(record);
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchemaOf(record);
		for (Metadata automaticMetadata : schema.getAutomaticMetadatas()) {
			updateAutomaticMetadata(context, record, recordProvider, automaticMetadata, reindexation, types, transaction);
		}

	}

	public void loadTransientEagerMetadatas(RecordImpl record, RecordProvider recordProvider, Transaction transaction) {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record);
		MetadataSchema schema = types.getSchemaOf(record);
		loadTransientEagerMetadatas(schema, record, recordProvider, transaction);
	}

	public void loadTransientEagerMetadatas(MetadataSchema schema, RecordImpl record, RecordProvider recordProvider,
											Transaction transaction) {
		TransactionExecutionRecordContext context = new TransactionExecutionContext(transaction).contextForRecord(record);
		TransactionRecordsReindexation reindexation = TransactionRecordsReindexation.ALL();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record);
		for (Metadata automaticMetadata : schema.getEagerTransientMetadatas()) {
			updateAutomaticMetadata(context, record, recordProvider, automaticMetadata, reindexation, types, transaction);
		}

	}

	public void loadTransientLazyMetadatas(RecordImpl record, RecordProvider recordProvider, Transaction transaction) {
		TransactionExecutionRecordContext context = new TransactionExecutionContext(transaction).contextForRecord(record);
		TransactionRecordsReindexation reindexation = TransactionRecordsReindexation.ALL();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchemaOf(record);
		for (Metadata automaticMetadata : schema.getLazyTransientMetadatas()) {
			updateAutomaticMetadata(context, record, recordProvider, automaticMetadata, reindexation, types, transaction);
		}

	}

	public void updateAutomaticMetadatas(RecordImpl record, RecordProvider recordProvider,
										 List<String> automaticMetadatas, Transaction transaction) {
		TransactionExecutionRecordContext context = new TransactionExecutionContext(transaction).contextForRecord(record);
		TransactionRecordsReindexation reindexation = TransactionRecordsReindexation.ALL();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchemaOf(record);
		for (String metadata : automaticMetadatas) {
			updateAutomaticMetadata(context, record, recordProvider, schema.get(metadata), reindexation, types, transaction);
		}
	}

	void updateAutomaticMetadata(TransactionExecutionRecordContext context, RecordImpl record,
								 RecordProvider recordProvider,
								 Metadata metadata,
								 TransactionRecordsReindexation reindexation, MetadataSchemaTypes types,
								 Transaction transaction) {

		if (context.getCalculatedMetadatas().contains(metadata.getLocalCode())) {
			//Already updated
			return;
		}

		CallStatCompiler statCompiler = Stats.compilerFor("updateAutomaticMetadata:" + record.getSchemaCode() + "_*_" + metadata);
		statCompiler.log(() -> {
			if (metadata.isMarkedForDeletion()) {
				record.updateAutomaticValue(metadata, null);

			} else if (metadata.getDataEntry().getType() == DataEntryType.COPIED) {
				setCopiedValuesInRecords(record, metadata, recordProvider, reindexation, transaction.getRecordUpdateOptions());

			} else if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
				setCalculatedValuesInRecords(context, record, metadata, recordProvider, reindexation, types,
						transaction);

			} else if (metadata.getDataEntry().getType() == DataEntryType.AGGREGATED) {
				//We don't want to calculate this metadata during record imports

				if (!record.isSaved()) {
					setAggregatedValuesInRecordsBasedOnOtherRecordInTransaction(context.getTransactionExecutionContext(), record, metadata, transaction, types);

				} else if (transaction.getRecordUpdateOptions().isUpdateAggregatedMetadatas()) {
					setAggregatedValuesInRecords(record, metadata, types);
				}

			}
		});

		//		context.markAsCalculated(metadata);
	}

	private void setAggregatedValuesInRecords(RecordImpl record, Metadata metadata, MetadataSchemaTypes types) {

		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) metadata.getDataEntry();

		Map<String, List<String>> inputMetadatasByReferenceMetadata = aggregatedDataEntry.getInputMetadatasByReferenceMetadata();
		LogicalSearchQuery query = buildCombinedAggregatedQuery(record, types, inputMetadatasByReferenceMetadata);
		List<SearchAggregatedValuesParamsQuery> queries = buildAggregatedQueries(metadata, record, types, inputMetadatasByReferenceMetadata);

		AggregationType agregationType = aggregatedDataEntry.getAgregationType();
		if (agregationType != null) {
			SearchAggregatedValuesParams aggregatedValuesParams = new SearchAggregatedValuesParams(query, queries, record, metadata,
					aggregatedDataEntry, types, searchServices, modelLayerFactory);
			Object calculatedValue = getHandlerFor(metadata).calculate(aggregatedValuesParams);
			(aggregatedValuesParams.getRecord()).updateAutomaticValue(metadata, calculatedValue);
		}
	}

	private LogicalSearchQuery buildCombinedAggregatedQuery(Record record, MetadataSchemaTypes types,
															Map<String, List<String>> inputMetadatasByReferenceMetadata) {
		List<MetadataSchemaType> schemaTypes = new ArrayList<>();
		for (String referenceMetadata : inputMetadatasByReferenceMetadata.keySet()) {
			MetadataSchemaType schemaType = types.getSchemaType(SchemaUtils.getSchemaTypeCode(referenceMetadata));
			schemaTypes.add(schemaType);
		}
		OngoingLogicalSearchCondition ongoingCondition = from(schemaTypes);

		List<LogicalSearchCondition> conditions = new ArrayList<>();
		for (String referenceMetadata : inputMetadatasByReferenceMetadata.keySet()) {
			Metadata metadata = types.getMetadata(referenceMetadata);
			conditions.add(ongoingCondition.where(metadata).isEqualTo(record));
		}
		return new LogicalSearchQuery().setCondition(ongoingCondition.whereAnyCondition(conditions));


	}

	private List<SearchAggregatedValuesParamsQuery> buildAggregatedQueries(Metadata aggregatedMetadata,
																		   Record record,
																		   MetadataSchemaTypes types,
																		   Map<String, List<String>> inputMetadatasByReferenceMetadata) {

		List<SearchAggregatedValuesParamsQuery> queries = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : inputMetadatasByReferenceMetadata.entrySet()) {
			Metadata metadata = types.getMetadata(entry.getKey());
			MetadataSchemaType schemaType = types.getSchemaType(SchemaUtils.getSchemaTypeCode(entry.getKey()));

			LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).where(metadata).isEqualTo(record));
			List<Metadata> metadatas = new ArrayList<>();
			for (String value : entry.getValue()) {
				metadatas.add(schemaType.getDefaultSchema().get(value));
			}

			Supplier<Stream<Record>> streamSupplier = new Supplier<Stream<Record>>() {
				@Override
				public Stream<Record> get() {

					LogicalSearchQueryExecutorInCache executorInCache = searchServices.getQueryExecutorInCache();
					Stream<Record> stream = null;
					if (schemaType.getCacheType().isSummaryCache()) {
						for (Metadata aMetadata : metadatas) {
							if (!SchemaUtils.isSummary(aMetadata)) {
								String message = "Aggregated metadata '" + aggregatedMetadata.getNoInheritanceCode() + "' should use cache for recalculation : metadata '" + aMetadata.getCode() + "' used to calculation is not summary";
								logImportantWarningOnce(message);
								stream = searchServices.streamFromSolr(query);
								break;
							}
						}

						query.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
						if (stream == null) {
							try {
								if (executorInCache.isQueryExecutableInCache(query)) {
									stream = executorInCache.stream(query);
								}
							} catch (LogicalSearchQueryExecutionCancelledException ignored) {
							}
							if (stream == null) {
								String message = "Aggregated metadata '" + aggregatedMetadata.getNoInheritanceCode() + "' should use cache for recalculation : query unsupported in cache";
								logImportantWarningOnce(message);
								stream = searchServices.streamFromSolr(query);
							}

						}
					} else {
						try {
							if (executorInCache.isQueryExecutableInCache(query)) {
								stream = executorInCache.stream(query);

							} else {
								LOGGER.warn("Aggregated metadata '" + aggregatedMetadata.getCode() + "' should use cache for recalculation");
								stream = searchServices.streamFromSolr(query);

							}
						} catch (LogicalSearchQueryExecutionCancelledException ignored) {
							stream = searchServices.streamFromSolr(query);
						}
					}
					return stream;
				}
			};

			queries.add(new SearchAggregatedValuesParamsQuery(query, metadatas, schemaType, streamSupplier));
		}

		return queries;
	}

	private void setAggregatedValuesInRecordsBasedOnOtherRecordInTransaction(TransactionExecutionContext context,
																			 RecordImpl record, Metadata metadata,
																			 Transaction transaction,
																			 MetadataSchemaTypes types) {

		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) metadata.getDataEntry();

		AggregationType agregationType = aggregatedDataEntry.getAgregationType();
		if (agregationType != null) {
			TransactionAggregatedValuesParams aggregatedValuesParams = new TransactionAggregatedValuesParams(
					record, metadata, aggregatedDataEntry, types);
			List<Object> values = new ArrayList<>();
			//TODO Populate based on transaction values
			Object calculatedValue = getHandlerFor(metadata).calculate(new InMemoryAggregatedValuesParams(record.getId(), metadata, values) {

				@Override
				public List<AggregatedValuesEntry> getEntries() {
					return new ArrayList<>();
				}

				@Override
				public int getReferenceCount() {
					return 0;
				}
			});
			(aggregatedValuesParams.getRecord()).updateAutomaticValue(metadata, calculatedValue);
		}
	}

	void setCopiedValuesInRecords(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
								  TransactionRecordsReindexation reindexation, RecordUpdateOptions options) {

		CopiedDataEntry copiedDataEntry = (CopiedDataEntry) metadataWithCopyDataEntry.getDataEntry();
		Metadata referenceMetadata = schemasManager.getSchemaTypes(record.getCollection())
				.getMetadata(copiedDataEntry.getReferenceMetadata());
		Object referenceValue = record.get(referenceMetadata);
		Map<String, Object> modifiedValues = record.getModifiedValues();
		boolean isReferenceModified = modifiedValues.containsKey(referenceMetadata.getDataStoreCode());
		boolean forcedReindexation = reindexation.isReindexed(metadataWithCopyDataEntry);
		boolean inTransaction = recordProvider.hasRecordInMemoryList(referenceValue);
		if (isReferenceModified || forcedReindexation || inTransaction) {
			Metadata copiedMetadata = schemasManager.getSchemaTypes(record.getCollection())
					.getMetadata(copiedDataEntry.getCopiedMetadata());

			copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);
		}
	}

	boolean calculatorDependencyModified(RecordImpl record, MetadataValueCalculator<?> calculator,
										 MetadataSchemaTypes types,
										 Metadata calculatedMetadata) {
		List<Metadata> modifiedMetadatas = record.getModifiedMetadataList(types);
		boolean calculatorDependencyModified = !record.isSaved();
		for (Dependency dependency : calculator.getDependencies()) {
			if (SpecialDependencies.HIERARCHY.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (SpecialDependencies.IDENTIFIER.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (SpecialDependencies.PRINCIPAL_TAXONOMY_CODE.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (SpecialDependencies.AURHORIZATIONS_TARGETTING_RECORD.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (SpecialDependencies.SECURITY_MODEL.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (dependency instanceof DynamicLocalDependency) {
				DynamicLocalDependency dynamicLocalDependency = (DynamicLocalDependency) dependency;
				for (Metadata metadata : modifiedMetadatas) {
					if (new SchemaUtils().isDependentMetadata(calculatedMetadata, metadata, dynamicLocalDependency)) {
						calculatorDependencyModified = true;
						break;
					}
				}

			} else if (!(dependency instanceof ConfigDependency)) {
				Metadata localMetadata = getMetadataFromDependency(record, dependency, false);
				if (localMetadata != null && record.isModified(localMetadata)) {
					calculatorDependencyModified = true;
				}
			}
		}
		return calculatorDependencyModified;
	}

	void calculateValueInRecord(TransactionExecutionRecordContext context, RecordImpl record,
								Metadata metadataWithCalculatedDataEntry,
								RecordProvider recordProvider, MetadataSchemaTypes types, Transaction transaction) {

		if (metadataWithCalculatedDataEntry.isMultiLingual()) {
			for (Locale locale : types.getCollectionInfo().getCollectionLocales()) {
				calculateValueInRecord(context, record, metadataWithCalculatedDataEntry, recordProvider, types, transaction,
						locale, LocalisedRecordMetadataRetrieval.PREFERRING);
			}

		} else {
			calculateValueInRecord(context, record, metadataWithCalculatedDataEntry, recordProvider, types, transaction,
					types.getCollectionInfo().getMainSystemLocale(), LocalisedRecordMetadataRetrieval.STRICT);
		}
	}

	void calculateValueInRecord(TransactionExecutionRecordContext context, RecordImpl record,
								Metadata metadataWithCalculatedDataEntry,
								RecordProvider recordProvider, MetadataSchemaTypes types,
								Transaction transaction,
								Locale locale,
								LocalisedRecordMetadataRetrieval mode) {
		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);
		Map<Dependency, Object> values = new HashMap<>();
		boolean requiredValuesDefined = addValuesFromDependencies(context.transactionExecutionContext, record, metadataWithCalculatedDataEntry,
				recordProvider, calculator, values, types, transaction, locale, mode);

		Object calculatedValue;
		if (requiredValuesDefined) {
			MetadataSchemaType schemaType = types.getSchemaType(record.getTypeCode());
			boolean typeInPrincipalTaxonomy = taxonomiesManager.isTypeInPrincipalTaxonomy(schemaType);
			modelLayerLogger.logCalculatedValue(record, calculator, values);
			calculatedValue = calculator.calculate(new CalculatorParameters(values, record.getId(),
					record.<String>get(Schemas.LEGACY_ID), schemaType, record.getCollection(), typeInPrincipalTaxonomy, metadataWithCalculatedDataEntry));
		} else {
			calculatedValue = calculator.getDefaultValue();
		}

		if (calculator instanceof MultiMetadatasValueCalculator) {

			Map<String, Object> multiMetadataValues = (Map<String, Object>) calculatedValue;
			for (Map.Entry<String, Object> entry : multiMetadataValues.entrySet()) {
				Metadata metadata = metadataWithCalculatedDataEntry.getSchema().get(entry.getKey());
				record.updateAutomaticValue(metadata, entry.getValue(), locale);
				context.markAsCalculated(metadata);
			}

		} else {
			record.updateAutomaticValue(metadataWithCalculatedDataEntry, calculatedValue, locale);
		}


	}

	public boolean isValueAutomaticallyFilled(Metadata metadataWithCalculatedDataEntry, Record record) {
		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);
		if (!calculator.hasEvaluator()) {
			return true;

		} else {
			Map<Dependency, Object> values = new HashMap<>();
			addValuesFromEvaluatorDependencies(record, calculator, values);
			return calculator.isAutomaticallyFilled(new CalculatorEvaluatorParameters(values));
		}
	}

	MetadataValueCalculator<?> getCalculatorFrom(Metadata metadataWithCalculatedDataEntry) {
		CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) metadataWithCalculatedDataEntry.getDataEntry();
		return calculatedDataEntry.getCalculator();
	}

	private void addValuesFromEvaluatorDependencies(Record record, MetadataValueCalculator<?> calculator,
													Map<Dependency, Object> values) {
		for (Dependency dependency : calculator.getEvaluatorDependencies()) {
			if (dependency instanceof LocalDependency<?>) {
				addValueForLocalDependency((RecordImpl) record, values, dependency, null, null);
			} else if (dependency instanceof ConfigDependency<?>) {
				addValueForConfigDependency(values, dependency);
			} else {
				throw new UnsupportedOperationException("Only Local and config dependencies are supported");
			}
		}
	}

	boolean addValuesFromDependencies(TransactionExecutionContext context, RecordImpl record, Metadata metadata,
									  RecordProvider recordProvider,
									  MetadataValueCalculator<?> calculator,
									  Map<Dependency, Object> values, MetadataSchemaTypes types,
									  Transaction transaction, Locale locale,
									  LocalisedRecordMetadataRetrieval mode) {
		for (Dependency dependency : calculator.getDependencies()) {
			if (dependency instanceof LocalDependency<?>) {
				if (!addValueForLocalDependency(record, values, dependency, locale, mode)) {
					return false;
				}

			} else if (dependency instanceof ReferenceDependency<?>) {
				if (!addValueForReferenceDependency(record, recordProvider, values, dependency,
						transaction.getRecordUpdateOptions(), locale, mode)) {
					return false;
				}

			} else if (dependency instanceof DynamicLocalDependency) {
				addValueForDynamicLocalDependency(record, metadata, values, (DynamicLocalDependency) dependency, types,
						recordProvider, transaction, locale, mode);

			} else if (dependency instanceof ConfigDependency<?>) {
				addValueForConfigDependency(values, dependency);

			} else if (dependency instanceof SpecialDependency<?>) {
				addValuesFromSpecialDependencies(context, record, recordProvider, values, dependency);
			}
		}
		return true;
	}

	private void addValueForConfigDependency(Map<Dependency, Object> values, Dependency dependency) {
		ConfigDependency<?> configDependency = (ConfigDependency<?>) dependency;
		Object configValue = systemConfigurationsManager.getValue(configDependency.getConfiguration());
		values.put(configDependency, configValue);
	}

	private void addValueForDynamicLocalDependency(RecordImpl record, Metadata calculatedMetadata,
												   Map<Dependency, Object> values, DynamicLocalDependency dependency,
												   MetadataSchemaTypes types,
												   RecordProvider recordProvider, Transaction transaction,
												   Locale locale, LocalisedRecordMetadataRetrieval mode) {

		//Map<String, Object> dynamicDependencyValues = new HashMap<>();

		List<Metadata> availableMetadatas = new ArrayList<>();
		List<Object> availableMetadatasValues = new ArrayList<>();
		List<Metadata> availableMetadatasWithValue = new ArrayList();
		for (Metadata metadata : types.getSchemaOf(record).getMetadatas()) {

			if (metadata.getTransiency() == MetadataTransiency.TRANSIENT_LAZY
				&& record.getLazyTransientValues().isEmpty()) {
				loadTransientLazyMetadatas(record, recordProvider, transaction);
			}

			if (SchemaUtils.isDependentMetadata(calculatedMetadata, metadata, dependency)) {
				availableMetadatas.add(metadata);
				if (metadata.isMultivalue()) {
					List<?> metadataValues = record.getList(metadata);
					availableMetadatasValues.add(metadataValues);
					//dynamicDependencyValues.put(metadata.getLocalCode(), metadataValues);
					if (!metadataValues.isEmpty()) {
						availableMetadatasWithValue.add(metadata);
					}
				} else {
					Object metadataValue = record.get(metadata, locale, mode);
					//dynamicDependencyValues.put(metadata.getLocalCode(), metadataValue);
					availableMetadatasValues.add(metadataValue);
					if (metadataValue != null) {
						availableMetadatasWithValue.add(metadata);
					}
				}
			}
		}
		MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) calculatedMetadata.getDataEntry()).getCalculator();
		values.put(dependency, new DynamicDependencyValues(calculator, availableMetadatas,
				availableMetadatasValues, availableMetadatasWithValue));

	}

	void addValuesFromSpecialDependencies(TransactionExecutionContext context, RecordImpl record,
										  RecordProvider recordProvider,
										  Map<Dependency, Object> values, Dependency dependency) {
		if (SpecialDependencies.HIERARCHY.equals(dependency)) {
			addValueForTaxonomyDependency(record, recordProvider, values, dependency);

		} else if (SpecialDependencies.SECURITY_MODEL.equals(dependency)) {
			values.put(dependency, toSecurityModel(context, record, recordProvider));

		} else if (SpecialDependencies.IDENTIFIER.equals(dependency)) {
			values.put(dependency, record.getId());

		} else if (SpecialDependencies.PRINCIPAL_TAXONOMY_CODE.equals(dependency)) {
			Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
			if (principalTaxonomy != null) {
				values.put(dependency, principalTaxonomy.getCode());
			}
		}
	}

	private SecurityModel toSecurityModel(TransactionExecutionContext context, RecordImpl calculatedRecord,
										  RecordProvider recordProvider) {

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(calculatedRecord.getCollection());
		MetadataSchemaType type = types.getSchemaType(calculatedRecord.getTypeCode());
		RecordsCache recordsCache = modelLayerFactory.getRecordsCaches().getCache(calculatedRecord.getCollection());
		if (type.hasSecurity() && recordsCache.isConfigured(User.SCHEMA_TYPE) && recordsCache.isConfigured(Group.SCHEMA_TYPE)
			&& recordsCache.isConfigured(Authorization.SCHEMA_TYPE)) {

			RolesManager rolesManager = modelLayerFactory.getRolesManager();
			Roles roles = rolesManager.getCollectionRoles(calculatedRecord.getCollection(), modelLayerFactory);

			TransactionSecurityModel securityModel = context.getTransactionSecurityModel();

			if (securityModel == null) {

				securityModel = buildTransactionSecurityModel(context.getTransaction(), roles, types);
				context.setTransactionSecurityModel(securityModel);
			}
			return securityModel;

		} else {
			return SingletonSecurityModel.empty(context.getTransaction().getCollection());
		}

	}

	public SingletonSecurityModel getSecurityModel(String collection) {

		SecurityModelCache cache = modelLayerFactory.getSecurityModelCache();
		SingletonSecurityModel model = cache.getCached(collection);

		if (model == null) {

			MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
			Roles roles = modelLayerFactory.getRolesManager().getCollectionRoles(collection);

			synchronized (SingletonSecurityModel.class) {

				model = cache.getCached(collection);
				if (model == null) {
					//TODO Put in singleton
					model = buildSingletonSecurityModel(roles, types, collection);
					cache.insert(model);
				}
			}
		}

		return model;
	}

	private TransactionSecurityModel buildTransactionSecurityModel(Transaction tx, Roles roles,
																   MetadataSchemaTypes types) {

		SingletonSecurityModel singletonSecurityModel = getSecurityModel(types.getCollection());
		return new TransactionSecurityModel(types, roles, singletonSecurityModel, tx);
	}

	private SingletonSecurityModel buildSingletonSecurityModel(Roles roles, MetadataSchemaTypes types,
															   String collection) {

		List<Authorization> authorizationDetails = new ArrayList<>();
		List<String> disabledGroups = new ArrayList<>();


		RecordsCache systemCollectionCache = modelLayerFactory.getRecordsCaches().getCache(Collection.SYSTEM_COLLECTION);
		SchemasRecordsServices systemCollectionSchemasRecordServices = new SchemasRecordsServices(
				Collection.SYSTEM_COLLECTION, modelLayerFactory);

		if (systemCollectionCache.isConfigured(GlobalGroup.SCHEMA_TYPE)) {
			for (Record record : searchServices.getAllRecordsInUnmodifiableState(systemCollectionSchemasRecordServices.getTypes()
					.getSchemaType(GlobalGroup.SCHEMA_TYPE))) {
				GlobalGroup globalGroup = systemCollectionSchemasRecordServices.wrapGlobalGroup(record);
				if (record != null && GlobalGroupStatus.INACTIVE.equals(globalGroup.getStatus())) {
					disabledGroups.add(globalGroup.getCode());
				}
			}

			boolean newGroupsDisabled = true;
			while (newGroupsDisabled) {
				newGroupsDisabled = false;

				for (Record record : searchServices.getAllRecordsInUnmodifiableState(systemCollectionSchemasRecordServices.getTypes()
						.getSchemaType(GlobalGroup.SCHEMA_TYPE))) {
					GlobalGroup globalGroup = systemCollectionSchemasRecordServices.wrapGlobalGroup(record);
					boolean disabled = disabledGroups.contains(globalGroup.getCode());
					if (!disabled && globalGroup.getParent() != null && disabledGroups.contains(globalGroup.getParent())) {
						disabledGroups.add(globalGroup.getCode());
						newGroupsDisabled = true;
					}
				}
			}
		}

		GroupAuthorizationsInheritance groupInheritanceMode =
				systemConfigurationsManager.getValue(ConstellioEIMConfigs.GROUP_AUTHORIZATIONS_INHERITANCE);
		KeyListMap<String, String> groupsReceivingAccessFromGroup = new KeyListMap<>();
		KeyListMap<String, String> groupsGivingAccessToGroup = new KeyListMap<>();

		KeyListMap<String, String> groupsGivingAccessToUser = new KeyListMap<>();
		KeyListMap<String, String> activePrincipalsGivingAccessToPrincipal = new KeyListMap<>();

		Map<String, Boolean> globalGroupEnabledMap = new HashMap<>();
		if (types.getSchema(Group.DEFAULT_SCHEMA).hasMetadataWithCode(Group.ANCESTORS)) {
			Metadata groupAncestorMetadata = types.getSchema(Group.DEFAULT_SCHEMA).getMetadata(Group.ANCESTORS);

			for (Record group : searchServices.getAllRecordsInUnmodifiableState(types.getSchemaType(Group.SCHEMA_TYPE))) {
				if (group != null) {
					boolean enabled = !disabledGroups.contains(group.<String>get(Schemas.CODE));
					globalGroupEnabledMap.put(group.getId(), enabled);

					for (String ancestor : group.<String>getList(groupAncestorMetadata)) {
						if (groupInheritanceMode == FROM_PARENT_TO_CHILD) {
							groupsGivingAccessToGroup.add(group.getId(), ancestor);
							groupsReceivingAccessFromGroup.add(ancestor, group.getId());

						} else {
							groupsReceivingAccessFromGroup.add(group.getId(), ancestor);
							groupsGivingAccessToGroup.add(ancestor, group.getId());
						}

					}

				} else {
					LOGGER.warn("Null record returned while getting all groups");
				}
			}
		}

		Metadata userGroups = types.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema().getMetadata(User.GROUPS);
		for (Record user : searchServices.getAllRecordsInUnmodifiableState(types.getSchemaType(User.SCHEMA_TYPE))) {
			if (user != null) {
				Set<String> allGroups = new HashSet<>();

				for (String groupId : user.<String>getList(userGroups)) {
					allGroups.add(groupId);
					allGroups.addAll(groupsGivingAccessToGroup.get(groupId));
				}

				groupsGivingAccessToUser.addAll(user.getId(), new ArrayList<>(allGroups));
				activePrincipalsGivingAccessToPrincipal.add(user.getId(), user.getId());
			} else {
				LOGGER.warn("Null record returned while getting all groups");
			}
		}

		for (Record record : searchServices
				.getAllRecordsInUnmodifiableState(types.getSchemaType(Authorization.SCHEMA_TYPE))) {
			if (record != null) {
				authorizationDetails.add(Authorization.wrapNullable(record, types));
			} else {
				LOGGER.warn("Null record returned while getting all users");
			}
		}


		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(types.getCollection());

		List<String> securableRecordSchemaTypes = new ArrayList<>();
		for (MetadataSchemaType schemaType : schemasManager.getSchemaTypes(collection).getSchemaTypes()) {
			if (schemaType.hasSecurity() && (principalTaxonomy == null || !principalTaxonomy.getSchemaTypes().contains(schemaType.getCode()))) {
				securableRecordSchemaTypes.add(schemaType.getCode());
			}
		}


		for (Map.Entry<String, List<String>> entry : groupsGivingAccessToGroup.getMapEntries()) {
			for (String groupId : entry.getValue()) {
				Boolean disabled = globalGroupEnabledMap.get(groupId);
				if (Boolean.TRUE.equals(disabled)) {
					activePrincipalsGivingAccessToPrincipal.add(entry.getKey(), groupId);
				}

			}
		}

		for (Map.Entry<String, List<String>> entry : groupsGivingAccessToUser.getMapEntries()) {
			for (String groupId : entry.getValue()) {
				Boolean disabled = globalGroupEnabledMap.get(groupId);
				if (Boolean.TRUE.equals(disabled)) {
					activePrincipalsGivingAccessToPrincipal.add(entry.getKey(), groupId);
				}
			}
		}

		return new SingletonSecurityModel(authorizationDetails, globalGroupEnabledMap, groupsReceivingAccessFromGroup,
				groupsGivingAccessToGroup, groupsGivingAccessToUser, activePrincipalsGivingAccessToPrincipal,
				groupInheritanceMode, securableRecordSchemaTypes, collection);
	}


	boolean addValueForReferenceDependency(RecordImpl record, RecordProvider recordProvider,
										   Map<Dependency, Object> values,
										   Dependency dependency, RecordUpdateOptions options, Locale locale,
										   LocalisedRecordMetadataRetrieval mode) {
		ReferenceDependency<?> referenceDependency = (ReferenceDependency<?>) dependency;
		Metadata referenceMetadata = getMetadataFromDependency(record, referenceDependency, false);
		if (referenceMetadata != null) {
			if (!referenceMetadata.isMultivalue()) {
				return addSingleValueReference(record, recordProvider, values, referenceDependency, referenceMetadata, options,
						locale, mode);
			} else {
				return addMultivalueReference(record, recordProvider, values, referenceDependency, referenceMetadata, options, locale,
						mode);
			}
		} else {
			return false;
		}
	}


	boolean addValueForTaxonomyDependency(RecordImpl record, RecordProvider recordProvider,
										  Map<Dependency, Object> values,
										  Dependency dependency) {

		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(record.getCollection(), schemaTypeCode);
		HierarchyDependencyValue built = new HierarchyDependencyValue(taxonomy);


		//		List<String> paths = new ArrayList<>();
		//		List<String> removedAuthorizations = new ArrayList<>();
		//		List<String> attachedAncestors = new ArrayList<>();
		MetadataSchema recordSchema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());

		List<Metadata> parentReferences = recordSchema.getParentReferences();
		//List<Integer> allAncestorsExceptPrincipals = new ArrayList<>();
		//		List<Integer> secondaryConceptIdsAndTheirAncestorsIds = new ArrayList<>();
		//		List<Integer> principalConceptIdsAndTheirAncestorsIds = new ArrayList<>();
		//		List<Integer> principalAncestors = new ArrayList<>();
		for (Metadata metadata : parentReferences) {

			String referenceValue = record.get(metadata);
			if (referenceValue != null) {
				//				Record referencedRecord = recordProvider.getRecord(referenceValue);
				//				List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
				//				paths.addAll(parentPaths);
				//				removedAuthorizations.addAll(referencedRecord.<String>getList(Schemas.ALL_REMOVED_AUTHS));
				//				attachedAncestors.addAll(referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS));
				//
				//				secondaryConceptAncestors.addAll(referencedRecord.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));
				//				principalAncestors.addAll(referencedRecord.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
				//				allAncestorsExceptPrincipals.addAll(referencedRecord.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));


				boolean retrievedUsingSummaryCached = false;
				if (metadata.getReferencedSchemaType().getCacheType().isSummaryCache()
					&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()) {
					Metadata pathMetadata = metadata.getReferencedSchemaType().getDefaultSchema().getMetadata(Schemas.PATH.getLocalCode());
					Metadata allRemovedAuthsMetadata = metadata.getReferencedSchemaType().getDefaultSchema().getMetadata(Schemas.ALL_REMOVED_AUTHS.getLocalCode());
					Metadata attachedAncestorsMetadata = metadata.getReferencedSchemaType().getDefaultSchema().getMetadata(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS.getLocalCode());

					if (pathMetadata.isStoredInSummaryCache()
						&& allRemovedAuthsMetadata.isStoredInSummaryCache()
						&& attachedAncestorsMetadata.isStoredInSummaryCache()) {
						retrievedUsingSummaryCached = true;

						Record referencedRecord = recordProvider.getRecordSummary(referenceValue);
						List<String> parentPaths = referencedRecord.getList(pathMetadata);
						built.getPaths().addAll(parentPaths);
						built.getRemovedAuthorizationIds().addAll(referencedRecord.<String>getList(allRemovedAuthsMetadata));
						referencedRecord.<Integer>getList(attachedAncestorsMetadata).forEach((id) -> {
							built.getAttachedAncestors().add(RecordId.toId(id).stringValue());
						});

					} else {
						logImportantWarningOnce("Metadatas 'path, allRemovedAuths, attachedAncestors' of type '"
												+ metadata.getReferencedSchemaType().getCode() + "' should be cached, it would avoid a getById");
					}
				}

				if (!retrievedUsingSummaryCached) {
					Record referencedRecord = recordProvider.getRecord(referenceValue);
					List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
					built.getPaths().addAll(parentPaths);
					built.getRemovedAuthorizationIds().addAll(referencedRecord.<String>getList(Schemas.ALL_REMOVED_AUTHS));
					built.getAttachedAncestors().addAll(referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS));
				}
			}
		}
		for (Taxonomy aTaxonomy : taxonomiesManager.getEnabledTaxonomies(record.getCollection())) {
			for (Metadata metadata : recordSchema.getTaxonomyRelationshipReferences(aTaxonomy)) {
				List<String> referencesValues = new ArrayList<>();
				if (metadata.isMultivalue()) {
					referencesValues.addAll(record.<String>getList(metadata));
				} else {
					String referenceValue = record.get(metadata);
					if (referenceValue != null) {
						referencesValues.add(referenceValue);
					}
				}
				for (String referenceValue : referencesValues) {
					if (referenceValue != null) {
						RecordId referenceValueRecordId = RecordId.toId(referenceValue);
						try {
							Record referencedRecord = recordProvider.getRecord(referenceValue);
							List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
							built.getPaths().addAll(parentPaths);
							built.getRemovedAuthorizationIds().addAll(referencedRecord.<String>getList(Schemas.ALL_REMOVED_AUTHS));

							if (aTaxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(record.getCollection()))) {
								//built.getAttachedAncestors().addAll(referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS));

								//TODO Enlever une de ces liste ?
								built.getPrincipalConceptsIntIds().add(referenceValueRecordId.intValue());
								built.getPrincipalConceptsIntIds().addAll(referencedRecord.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
								built.getPrincipalAncestorsIntIds().add(referenceValueRecordId.intValue());
								built.getPrincipalAncestorsIntIds().addAll(referencedRecord.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
								//								referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS).forEach((id) ->
								//										built.getPrincipalConceptsIntIds().add(toIntId(id)));

							} else {
								built.getSecondaryConceptsIntIds().add(referenceValueRecordId.intValue());
								for (String pathPart : referencedRecord.<String>getList(Schemas.PATH_PARTS)) {
									if (!pathPart.startsWith("_LAST_") && !pathPart.equals("R")) {
										built.getSecondaryConceptsIntIds().add(RecordId.toIntId(pathPart));
									}
								}
								//built.getSecondaryConceptsIntIds().addAll(referencedRecord.getList(Schemas.PATH_PARTS));
								//								referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS).forEach((id) ->
								//										built.getSecondaryConceptsIntIds().add(toIntId(id)));

							}
						} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		//		List<Integer> attachedPrincipalConceptsIntIdsFromParent = new ArrayList<>();
		//		List<Integer> principalAncestorsIntIdsFromParent = new ArrayList<>();
		//		List<Integer> secondaryConceptsIntIdsFromParent = new ArrayList<>();
		//		List<Integer> principalConceptsIntIdsFromParent = new ArrayList<>();

		String parentId = record.getParentId();
		if (parentId != null) {
			Record parent = recordProvider.getRecordSummary(parentId);
			if (parent != null) {
				int parentIntId = RecordId.toId(parentId).intValue();

				if (!Boolean.TRUE.equals(record.get(Schemas.IS_DETACHED_AUTHORIZATIONS))) {
					built.getAttachedPrincipalConceptsIntIdsFromParent().addAll(parent.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS));
					built.getAttachedPrincipalConceptsIntIdsFromParent().add(parentIntId);
					built.getAttachedPrincipalConceptsIntIdsFromParent().addAll(parent.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS));
				}

				built.getPrincipalAncestorsIntIdsFromParent().addAll(parent.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
				built.getPrincipalAncestorsIntIdsFromParent().add(RecordId.toId(parent.getId()).intValue());


				built.getPrincipalConceptsIntIdsFromParent().addAll(parent.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS));
				built.getSecondaryConceptsIntIdsFromParent().addAll(parent.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));
			}
		}

		values.put(dependency, built);
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean addMultivalueReference(RecordImpl record, RecordProvider recordProvider,
										   Map<Dependency, Object> values,
										   ReferenceDependency<?> referenceDependency, Metadata referenceMetadata,
										   RecordUpdateOptions options,
										   Locale locale, LocalisedRecordMetadataRetrieval mode) {
		List<String> referencesValues = record.<String>getList(referenceMetadata, locale, mode);
		List<Record> referencedRecords = new ArrayList<>();
		for (String referenceValue : referencesValues) {
			if (referenceValue != null) {
				try {
					referencedRecords.add(recordProvider.getRecord(referenceValue));
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
							record.getId(), referenceValue, referenceMetadata, e);
					if (options.isCatchBrokenReferenceErrors()) {
						LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
					} else {
						throw brokenReferenceException;
					}
				}
			}

		}
		List<Object> referencedValues = new ArrayList<>();
		SortedMap<String, Object> referencedValuesMap = new TreeMap<>();
		for (Record referencedRecord : referencedRecords) {
			Metadata dependentMetadata = getDependentMetadataFromDependency(referenceDependency, referencedRecord);
			Object dependencyValue = referencedRecord.get(dependentMetadata, locale, mode);
			if (referenceDependency.isRequired() && dependencyValue == null) {
				return false;

			} else if (referenceDependency.isGroupedByReference()) {
				referencedValuesMap.put(referencedRecord.getId(), dependencyValue);

			} else if (dependencyValue instanceof List) {
				referencedValues.addAll((List) dependencyValue);

			} else {
				referencedValues.add(dependencyValue);
			}
		}

		if (referenceDependency.isGroupedByReference()) {
			values.put(referenceDependency, referencedValuesMap);
		} else {
			values.put(referenceDependency, referencedValues);
		}

		return true;
	}

	private boolean addSingleValueReference(RecordImpl record, RecordProvider recordProvider,
											Map<Dependency, Object> values,
											ReferenceDependency<?> dependency, Metadata referenceMetadata,
											RecordUpdateOptions options,
											Locale locale, LocalisedRecordMetadataRetrieval mode) {
		String referenceValue = (String) record.get(referenceMetadata, locale, mode);
		Record referencedRecord;
		if (dependency.isRequired() && referenceValue == null) {
			return false;
		} else {
			try {
				referencedRecord = null;
				if (referenceValue != null) {
					Metadata dependentMetadata = referenceMetadata.getReferencedSchemaType().getDefaultSchema()
							.getMetadata(dependency.getDependentMetadataCode());
					if (referenceMetadata.getReferencedSchemaType().getCacheType().isSummaryCache()) {


						if (dependentMetadata.isStoredInSummaryCache()) {
							referencedRecord = recordProvider.getRecordSummary(referenceValue);
						} else {
							logImportantWarningOnce("Metadata '" + dependentMetadata.getNoInheritanceCode() + "' should be cached, it would avoid a getById");
							referencedRecord = recordProvider.getRecord(referenceValue);
						}

					} else {
						if (!dependentMetadata.getSchemaType().getCacheType().hasPermanentCache()) {
							logImportantWarningOnce("Metadata '" + dependentMetadata.getNoInheritanceCode() + "' should be cached, it would avoid a getById");
						}
						referencedRecord = recordProvider.getRecord(referenceValue);
					}
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
						record.getId(), referenceValue, referenceMetadata, e);
				if (options.isCatchBrokenReferenceErrors()) {
					LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
					referencedRecord = null;
				} else {
					throw brokenReferenceException;
				}
			}
		}

		Object dependencyValue;
		if (referencedRecord != null) {
			Metadata dependentMetadata = getDependentMetadataFromDependency(dependency, referencedRecord);
			dependencyValue = referencedRecord.get(dependentMetadata, locale, mode);
		} else if (dependency.isMultivalue()) {
			dependencyValue = new ArrayList<>();
		} else {
			dependencyValue = null;
		}
		if (dependency.isRequired() && dependencyValue == null) {
			return false;
		} else {
			values.put(dependency, dependencyValue);
		}
		return true;
	}

	Metadata getDependentMetadataFromDependency(ReferenceDependency<?> referenceDependency, Record referencedRecord) {
		MetadataSchema schema = schemasManager.getSchemaTypes(referencedRecord.getCollection())
				.getSchema(referencedRecord.getSchemaCode());
		return schema.get(referenceDependency.getDependentMetadataCode());
	}

	boolean addValueForLocalDependency(RecordImpl record, Map<Dependency, Object> values, Dependency dependency,
									   Locale locale, LocalisedRecordMetadataRetrieval mode) {
		Metadata metadata = getMetadataFromDependency(record, dependency);
		Object dependencyValue = mode != null ? record.get(metadata, locale, mode) : record.get(metadata, locale);
		if (dependency.isRequired() && dependencyValue == null) {
			return false;
		} else {
			values.put(dependency, dependencyValue);
		}
		return true;
	}

	Metadata getMetadataFromDependency(RecordImpl record, Dependency dependency) {
		return getMetadataFromDependency(record, dependency, true);
	}

	Metadata getMetadataFromDependency(RecordImpl record, Dependency dependency, boolean throwExceptionIfNoMetadata) {
		String localMetadataCode = dependency.getLocalMetadataCode();
		MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		return throwExceptionIfNoMetadata || schema.hasMetadataWithCode(localMetadataCode) ? schema.get(localMetadataCode) : null;
	}

	void copyValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
						   Metadata referenceMetadata, Metadata copiedMetadata, RecordUpdateOptions options) {

		if (referenceMetadata.isMultivalue()) {
			List<String> referencedRecordIds = record.getList(referenceMetadata);
			if (referencedRecordIds == null || referencedRecordIds.isEmpty()) {
				record.updateAutomaticValue(metadataWithCopyDataEntry, Collections.emptyList());
			} else {
				copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata,
						referencedRecordIds, referenceMetadata, options);
			}
		} else {
			String referencedRecordId = record.get(referenceMetadata);
			if (referencedRecordId == null) {
				record.updateAutomaticValue(metadataWithCopyDataEntry, null);
			} else {
				copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata, referencedRecordId,
						referenceMetadata, options);
			}
		}

	}

	void copyReferenceValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry,
									RecordProvider recordProvider,
									Metadata copiedMetadata, String referencedRecordId, Metadata referenceMetadata,
									RecordUpdateOptions options) {
		Object copiedValue;
		try {
			Record referencedRecord;
			if (copiedMetadata.getSchemaType().getCacheType().isSummaryCache()) {
				if (copiedMetadata.isStoredInSummaryCache()) {
					referencedRecord = recordProvider.getRecordSummary(referencedRecordId);
				} else {
					logImportantWarningOnce("Metadata '" + copiedMetadata.getNoInheritanceCode() + "' should be cached, it would avoid a getById");
					referencedRecord = recordProvider.getRecord(referencedRecordId);
				}
			} else {
				if (!copiedMetadata.getSchemaType().getCacheType().hasPermanentCache()) {
					logImportantWarningOnce("Metadata '" + copiedMetadata.getNoInheritanceCode() + "' should be cached, it would avoid a getById");
				}
				referencedRecord = recordProvider.getRecord(referencedRecordId);
			}
			copiedValue = referencedRecord.get(copiedMetadata);

		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
					record.getId(), referencedRecordId, referenceMetadata, e);
			if (options.isCatchBrokenReferenceErrors()) {
				LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
				copiedValue = null;
			} else {
				throw brokenReferenceException;
			}
		}
		record.updateAutomaticValue(metadataWithCopyDataEntry, copiedValue);
	}

	void copyReferenceValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry,
									RecordProvider recordProvider,
									Metadata copiedMetadata, List<String> referencedRecordIds,
									Metadata referenceMetadata, RecordUpdateOptions options) {
		List<Object> values = new ArrayList<>();
		for (String referencedRecordId : referencedRecordIds) {
			if (referencedRecordId != null) {
				try {
					RecordImpl referencedRecord = (RecordImpl) recordProvider.getRecord(referencedRecordId);

					if (copiedMetadata.isMultivalue()) {
						values.addAll(referencedRecord.getList(copiedMetadata));
					} else {
						Object value = referencedRecord.get(copiedMetadata);
						if (value != null) {
							values.add(value);
						}
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
							record.getId(), referencedRecordId, referenceMetadata, e);
					if (options.isCatchBrokenReferenceErrors()) {
						LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
					} else {
						throw brokenReferenceException;
					}
				}
			}

		}
		record.updateAutomaticValue(metadataWithCopyDataEntry, values);
	}

	public List<Metadata> sortMetadatasUsingLocalDependencies(
			Map<Metadata, Set<String>> metadatasWithLocalCodeDependencies) {
		List<Metadata> sortedMetadatas = new ArrayList<>();
		Map<Metadata, Set<String>> metadatas = copyInModifiableMap(metadatasWithLocalCodeDependencies);
		while (!metadatas.isEmpty()) {
			Metadata nextMetadata = getAMetadataWithoutDependencies(metadatas);
			if (nextMetadata == null) {
				throw new ImpossibleRuntimeException("Cyclic dependency");
			}
			metadatas.remove(nextMetadata);
			for (Map.Entry<Metadata, Set<String>> otherMetadataEntry : metadatas.entrySet()) {
				otherMetadataEntry.getValue().remove(nextMetadata.getLocalCode());
			}
			sortedMetadatas.add(nextMetadata);
		}
		return sortedMetadatas;
	}

	private Map<Metadata, Set<String>> copyInModifiableMap(
			Map<Metadata, Set<String>> metadatasWithLocalCodeDependencies) {
		Map<Metadata, Set<String>> metadatas = new HashMap<>();
		for (Map.Entry<Metadata, Set<String>> entry : metadatasWithLocalCodeDependencies.entrySet()) {
			metadatas.put(entry.getKey(), new HashSet<String>(entry.getValue()));
		}
		return metadatas;
	}

	private Metadata getAMetadataWithoutDependencies(Map<Metadata, Set<String>> metadatas) {
		Metadata nextMetadata = null;
		for (Map.Entry<Metadata, Set<String>> entry : metadatas.entrySet()) {
			if (entry.getValue().isEmpty()) {
				nextMetadata = entry.getKey();
				break;
			}
		}
		return nextMetadata;
	}

	void setCalculatedValuesInRecords(TransactionExecutionRecordContext context, RecordImpl record,
									  Metadata metadataWithCalculatedDataEntry, RecordProvider recordProvider,
									  TransactionRecordsReindexation reindexation, MetadataSchemaTypes types,
									  Transaction transaction) {

		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);

		boolean lazyTransientMetadataToLoad = metadataWithCalculatedDataEntry.getTransiency() == MetadataTransiency.TRANSIENT_LAZY
											  && !record.getLazyTransientValues().containsKey(metadataWithCalculatedDataEntry.getDataStoreCode());

		if (calculatorDependencyModified(record, calculator, types, metadataWithCalculatedDataEntry)
			|| reindexation.isReindexed(metadataWithCalculatedDataEntry)
			|| lazyTransientMetadataToLoad) {
			calculateValueInRecord(context, record, metadataWithCalculatedDataEntry, recordProvider, types, transaction);
		}
	}

}
