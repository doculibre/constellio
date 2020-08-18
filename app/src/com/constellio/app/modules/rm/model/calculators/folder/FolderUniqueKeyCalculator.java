package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.xml.TypeConvertionUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class FolderUniqueKeyCalculator extends AbstractMetadataValueCalculator<String> {
	public static final String METADATA_CODE = "metadataCode";
	public static final String UNIQUE_KEY_CONFIG = "uniqueKeyConfig";

	DynamicLocalDependency dynamicMetadatasDependency = new DynamicMetadatasDependency();

	@Override
	public String calculate(CalculatorParameters parameters) {
		StringBuilder unicityValueBuilder = new StringBuilder();

		DynamicDependencyValues values = parameters.get(dynamicMetadatasDependency);
		List<Map> listMap = (List<Map>) parameters.getMetadata().getCustomParameter().get(UNIQUE_KEY_CONFIG);

		if (listMap != null) {
			for (Map<String, Object> currentMap : listMap) {
				String code = (String) currentMap.get(METADATA_CODE);
				String localeCode = TypeConvertionUtil.getMetadataLocalCode(code);
				Metadata metadata = values.getAvailableMetadatasWithAValue().stream().filter((m) ->
						m.getLocalCode().equals(localeCode)).findFirst().orElse(null);

				if (metadata != null) {
					if (metadata.isMultivalue()) {
						List metadataValue = values.getValue(localeCode);
						for (int i = 0; i < metadataValue.size(); i++) {
							if (unicityValueBuilder.length() > 0) {
								unicityValueBuilder.append(", ");
							}
							Object object = metadataValue.get(i);
							unicityValueBuilder.append(getValue(metadata, object));
						}
					} else {
						if (unicityValueBuilder.length() > 0) {
							unicityValueBuilder.append(", ");
						}
						Object metadataValue = values.getValue(localeCode);
						unicityValueBuilder.append(getValue(metadata, metadataValue));
					}
				}
			}
		}

		return unicityValueBuilder.toString();
	}

	public String getValue(Metadata metadata, Object value) {
		String returnValue = "";
		if (value != null) {
			switch (metadata.getType()) {
				case DATE:
					LocalDate localDateJoda1 = (LocalDate) value;
					Date dateValue = localDateJoda1.toDateTimeAtStartOfDay().toDate();

					returnValue = dateValue.getTime() + "";
					break;
				case DATE_TIME:
					LocalDateTime localDateJoda2 = (LocalDateTime) value;
					DateTime dateTime = localDateJoda2.toDateTime();

					returnValue = dateTime.getMillis() + "";
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
				case ENUM:
					returnValue = $(value.toString());
					break;
				case REFERENCE:
					returnValue = value + "";
					break;
				default:
					returnValue = value + "";
			}
		}

		return returnValue;
	}

	@Override
	public String getDefaultValue() {
		return null;
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
		return asList(dynamicMetadatasDependency);
	}

	public static class DynamicMetadatasDependency extends DynamicLocalDependency {

		private boolean isMultiValue;
		private boolean isRequired;
		private String localMetadataCode;

		@Override
		public boolean isDependentOf(Metadata metadata, Metadata aCalculatedMetadata) {
			Map modifiableMap = aCalculatedMetadata.getCustomParameter();
			Metadata metadataDependedOn = null;
			List list = (List) modifiableMap.get(UNIQUE_KEY_CONFIG);

			if (list != null) {
				for (Map listItem : (List<Map>) list) {
					if (TypeConvertionUtil.getMetadataLocalCode((String) listItem.get(METADATA_CODE)).equals(metadata.getLocalCode())) {
						metadataDependedOn = metadata;
					}

					if (metadata == null && metadata.getLocalCode()
							.equals(Schemas.TITLE.getLocalCode())) {
						metadataDependedOn = metadata;
					}

					if (metadataDependedOn != null) {
						isMultiValue = metadataDependedOn.isMultivalue();
						isRequired = metadataDependedOn.isDefaultRequirement();
						localMetadataCode = metadataDependedOn.getLocalCode();
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

		@Override
		public boolean isMultivalue() {
			return isMultiValue;
		}

		@Override
		public boolean isRequired() {
			return isRequired;
		}

		@Override
		public String getLocalMetadataCode() {
			return localMetadataCode;
		}
	}
}
