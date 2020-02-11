package com.constellio.app.entities.calculators;

import com.constellio.app.ui.pages.summaryconfig.SummaryConfigParams;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.xml.TypeConvertionUtil;
import com.google.common.base.Strings;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SummaryColumnCalculator extends AbstractMetadataValueCalculator<String> implements InitializedMetadataValueCalculator<String> {
	DynamicLocalDependency dynamicMetadatasDependency = new DynamicMetadatasDependency();
	ConfigDependency<String> dateformat = ConstellioEIMConfigs.DATE_FORMAT.dependency();
	ConfigDependency<String> dateTimeformat = ConstellioEIMConfigs.DATE_TIME_FORMAT.dependency();

	public static final String SUMMARY_CONFIG = "summaryconfig";
	public static final String PREFIX = "prefix";
	public static final String METADATA_CODE = "metadataCode";
	public static final String REFERENCE_METADATA_DISPLAY = "referenceMetadataDisplay";

	public static final String IS_ALWAYS_SHOWN = "isAlwaysShown";

	List<Dependency> dependencies = new ArrayList<>();
	String metadataCode;

	@Override
	public String calculate(CalculatorParameters parameters) {
		DynamicDependencyValues values = parameters.get(dynamicMetadatasDependency);
		StringBuilder summmaryColumnValue = new StringBuilder();
		List<Map> listMap = (List<Map>) parameters.getMetadata().getCustomParameter().get(SUMMARY_CONFIG);

		if (listMap != null) {

			for (Map currentMap : listMap) {
				String code = (String) currentMap.get(METADATA_CODE);
				String localeCode = TypeConvertionUtil.getMetadataLocalCode(code);
				Metadata metadata = values.getAvailableMetadatasWithAValue().stream().filter((m) ->
						m.getLocalCode().equals(localeCode)).findFirst().orElse(null);

				StringBuilder textForMetadata = new StringBuilder();
				if (metadata != null) {
					if (metadata.getType() != MetadataValueType.REFERENCE) {
						if (metadata.isMultivalue()) {
							List metadataValue = values.getValue(localeCode);
							for (int i = 0; i < metadataValue.size(); i++) {
								Object object = metadataValue.get(i);
								textForMetadata.append(getValue(values, parameters, metadata, object));
								if (i != metadataValue.size() - 1) {
									textForMetadata.append(", ");
								}
							}
						} else {
							Object metadataValue = values.getValue(localeCode);
							textForMetadata.append(getValue(values, parameters, metadata, metadataValue));
						}
					} else {
						ReferenceDependency referenceDependency = getReferenceDependancy(code);

						if (metadata.isMultivalue()) {
							List<String> valueList = (List<String>) parameters.get(referenceDependency);
							for (int i = 0; i < valueList.size(); i++) {
								textForMetadata.append(valueList.get(i));
								if (i != valueList.size() - 1) {
									textForMetadata.append(", ");
								}
							}

						} else {
							textForMetadata.append((String) parameters.get(referenceDependency));
						}

					}
				}
				String prefixContentToStirng = addPrefixContentToString(textForMetadata.toString(), currentMap);

				if (!Strings.isNullOrEmpty(prefixContentToStirng) && summmaryColumnValue.length() > 0) {
					prefixContentToStirng = ", " + prefixContentToStirng;
				}

				summmaryColumnValue.append(prefixContentToStirng);
			}
		}
		if (Strings.isNullOrEmpty(summmaryColumnValue.toString())) {
			return null;
		}

		return summmaryColumnValue.toString();

	}

	public ReferenceDependency getReferenceDependancy(String metadataCode) {
		for (Dependency dependency : dependencies) {
			if (dependency instanceof ReferenceDependency) {
				ReferenceDependency referenceDependency = (ReferenceDependency) dependency;
				if (referenceDependency.getLocalMetadataCode().equalsIgnoreCase(metadataCode)) {
					return referenceDependency;
				}
			}
		}

		return null;
	}

	@Override
	public void initialize(List<Metadata> schemaMetadatas, Metadata calculatedMetadata) {

		List<Dependency> builtDependencies = new ArrayList<>();

		metadataCode = calculatedMetadata.getCode();

		List<Map> summaryColumnList = (List<Map>) calculatedMetadata.getCustomParameter().get(SUMMARY_CONFIG);
		if (summaryColumnList != null) {
			for (Map currentMap : summaryColumnList) {
				String metadataCode = (String) currentMap.get(METADATA_CODE);
				builtDependencies.add(toLocalDependency(schemaMetadatas, metadataCode));
			}
		}

		builtDependencies.addAll(asList(dynamicMetadatasDependency, dateformat, dateTimeformat));
		dependencies = Collections.unmodifiableList(builtDependencies);
	}

	@Override
	public void initialize(MetadataSchemaTypes types, MetadataSchema schema, Metadata calculatedMetadata) {
		metadataCode = calculatedMetadata.getCode();

		List<Dependency> builtDependencies = new ArrayList<>(dependencies);

		MetadataList metadataList = schema.getMetadatas();
		Metadata metadata = metadataList.getMetadataWithLocalCode("summary");
		List<Map> summaryColumnList = (List<Map>) metadata.getCustomParameter().get(SUMMARY_CONFIG);
		if (summaryColumnList != null) {
			for (Map currentMap : summaryColumnList) {

				String metadataCode = (String) currentMap.get(METADATA_CODE);
				Metadata currentMetadata = schema.getMetadata(metadataCode);

				if (currentMetadata.getType() == MetadataValueType.REFERENCE) {
					builtDependencies.add(toReferenceDependency(types, schema, metadataCode,
							getReferenceMetadataDisplayStringValue(currentMap)));
				}
			}
		}

		builtDependencies.addAll(asList(dynamicMetadatasDependency, dateformat, dateTimeformat));
		dependencies = Collections.unmodifiableList(builtDependencies);
	}

	public String getReferenceMetadataDisplayStringValue(Map mapWithReferenceMetadataDisplay) {
		SummaryConfigParams.ReferenceMetadataDisplay referenceMetadataDisplay = SummaryConfigParams.ReferenceMetadataDisplay
				.fromInteger((Integer) mapWithReferenceMetadataDisplay.get(REFERENCE_METADATA_DISPLAY));

		return referenceMetadataDisplay.getLocalCode();
	}

	public String addPrefixContentToString(String itemShown, Map summarySettings) {
		boolean isAlwaysShow = Boolean.TRUE.toString().equalsIgnoreCase(summarySettings.get(IS_ALWAYS_SHOWN).toString());

		if (isAlwaysShow || !Strings.isNullOrEmpty(itemShown)) {
			String prefix = (String) summarySettings.get(PREFIX);

			if (Strings.isNullOrEmpty(prefix)) {
				prefix = "";
			}

			itemShown = prefix + " " + itemShown;
		}

		return itemShown;
	}

	public String getValue(DynamicDependencyValues values, CalculatorParameters parameters, Metadata metadata,
						   Object value) {
		String returnValue = "";
		if (value != null) {
			switch (metadata.getType()) {
				case DATE:
					if (value instanceof LocalDate) {
						LocalDate localDateJoda = (LocalDate) value;
						value = localDateJoda.toDateTimeAtStartOfDay().toDate();
					}

					returnValue = SimpleDateFormatSingleton
							.getSimpleDateFormat(parameters.get
									(dateformat)).format(value);
					break;
				case DATE_TIME:
					if (value instanceof LocalDateTime) {
						LocalDateTime localDateTimeJoda = (LocalDateTime) value;
						returnValue = DateFormatUtils.format(localDateTimeJoda);
					}
					break;
				case STRING:
					returnValue = value.toString();
					break;
				case TEXT:
					returnValue = value.toString();
					break;
				case INTEGER:
					returnValue = value + "";
					break;
				case NUMBER:
					returnValue = value + "";
					break;
				case BOOLEAN:
					returnValue = value + "";
					break;
				case CONTENT:
					returnValue = values.getValue(Schemas.TITLE.getLocalCode());
					break;
				case ENUM:
					returnValue = $(value.toString());
					break;
			}
		}

		return returnValue;
	}

	@Override
	public String getDefaultValue() {
		return "";
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return dependencies;
	}

	public static class DynamicMetadatasDependency extends DynamicLocalDependency {

		@Override
		public boolean isDependentOf(Metadata metadata, Metadata aCalculatedMetadata) {
			Map modifiableMap = aCalculatedMetadata.getCustomParameter();

			List list = (List) modifiableMap.get(SUMMARY_CONFIG);

			if (list != null) {
				for (Map listItem : (List<Map>) list) {
					if (TypeConvertionUtil.getMetadataLocalCode((String) listItem.get(METADATA_CODE))
								.equals(metadata.getLocalCode()) || metadata.getLocalCode()
								.equals(Schemas.TITLE.getLocalCode())) {
						return true;
					}
				}
			}

			return false;
		}

		@Override
		public boolean isIncludingGlobalMetadatas() {
			return true;
		}

	}

	private ReferenceDependency toReferenceDependency(MetadataSchemaTypes types, MetadataSchema schema,
													  String metadataCode,
													  String metadataLocalCode) {
		Metadata referenceMetadata = schema.getMetadata(metadataCode);
		String referencedSchemaTypeCode = referenceMetadata.getAllowedReferences().getTypeWithAllowedSchemas();
		MetadataSchema referencedSchema = types.getDefaultSchema(referencedSchemaTypeCode);
		String referenceMetadataCode = referencedSchema.getCode() + "_" + metadataLocalCode;
		Metadata copiedMetadata = referencedSchema.getMetadata(referenceMetadataCode);

		boolean isRequired = referenceMetadata.isDefaultRequirement();
		boolean isMultivalue = copiedMetadata.isMultivalue() || referenceMetadata.isMultivalue();
		boolean isGroupedByReferences = false;
		return new ReferenceDependency<>(metadataCode, referenceMetadataCode, isRequired,
				isMultivalue,
				copiedMetadata.getType(), isGroupedByReferences, false);
	}

	private LocalDependency<?> toLocalDependency(List<Metadata> metadatas, String metadataCode) {
		boolean isRequired = false;
		Metadata metadata = null;

		String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(metadataCode);
		for (Metadata aMetadata : metadatas) {
			if (aMetadata.getLocalCode().equals(localCode)) {
				metadata = aMetadata;
				break;
			}
		}

		if (metadata == null) {
			throw new IllegalArgumentException("No such metadata with code " + metadataCode);
		}

		return new LocalDependency<>(metadataCode, isRequired, metadata.isMultivalue(),
				metadata.getType(), false);
	}

}
