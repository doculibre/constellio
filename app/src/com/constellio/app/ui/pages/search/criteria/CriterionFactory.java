package com.constellio.app.ui.pages.search.criteria;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CriterionFactory implements CombinedStructureFactory {
	final String DATE_TIME_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	@Override
	public String toString(ModifiableStructure structure) {
		return gson().toJson(structure);
	}

	public Map<String, Object> toMap(ModifiableStructure structure) {
		try {
			return new ObjectMapper().readValue(toString(structure), HashMap.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Criterion build(String serializedCriterion) {
		Criterion criterion = new Criterion();
		if (StringUtils.isNotBlank(serializedCriterion)) {
			TypeToken<Criterion> listTypeToken = new TypeToken<Criterion>() {
			};

			criterion = gson().fromJson(serializedCriterion, listTypeToken.getType());
			criterion = rebuildCriterion(criterion);
		}
		return criterion;
	}

	private Criterion rebuildCriterion(Criterion criterion) {
		Criterion newCriterion = new Criterion();

		newCriterion.setSchemaType(criterion.getSchemaType());

		BooleanOperator booleanOperator = criterion.getBooleanOperator();
		newCriterion.setBooleanOperator(booleanOperator);

		if (criterion.getMetadataCode() != null) {
			newCriterion.setMetadata(criterion.getMetadataCode(), criterion.getMetadataType(), criterion.getEnumClassName());
		}

		SearchOperator searchOperator = criterion.getSearchOperator();
		newCriterion.setSearchOperator(searchOperator);

		RelativeCriteria relativeCriteria = criterion.getRelativeCriteria();
		newCriterion.setRelativeCriteria(relativeCriteria);

		newCriterion.setLeftParens(criterion.isLeftParens());
		newCriterion.setRightParens(criterion.isRightParens());

		MetadataValueType metadataValueType = criterion.getMetadataType();

		String value = null;
		if (criterion.getValue() != null) {
			value = "" + criterion.getValue();
		}

		String endValue = null;
		if (criterion.getEndValue() != null) {
			endValue = "" + criterion.getEndValue();
		}

		if (metadataValueType != null) {

			switch (metadataValueType) {
				case DATE:
				case DATE_TIME:

					if (relativeCriteria != null
						&& (relativeCriteria.getRelativeSearchOperator() == RelativeSearchOperator.PAST
							|| relativeCriteria.getRelativeSearchOperator() == RelativeSearchOperator.FUTURE)) {
						Double newValue = Double.valueOf(value);
						newCriterion.setValue(newValue);
					} else if (value != null) {
						LocalDateTime ldt = new LocalDateTime().parse(value);
						newCriterion.setValue(ldt);
					}
					if (relativeCriteria != null
						&& (relativeCriteria.getEndRelativeSearchOperator() == RelativeSearchOperator.PAST
							|| relativeCriteria.getEndRelativeSearchOperator() == RelativeSearchOperator.FUTURE)) {
						Double newEndValue = Double.valueOf(endValue);
						newCriterion.setEndValue(newEndValue);
					} else if (endValue != null) {
						LocalDateTime ldt = new LocalDateTime().parse(endValue);
						newCriterion.setEndValue(ldt);
					}

					break;
				case STRING:
					newCriterion.setValue(value);
					break;
				case TEXT:
					newCriterion.setValue(value);
					break;
				case INTEGER:
					newCriterion.setValue(Integer.valueOf(value));
					break;
				case NUMBER:
					if (value != null) {
						double doubleValue = Double.parseDouble(value);
						newCriterion.setValue(doubleValue);
						if (endValue != null) {
							double doubleEndValue = Double.parseDouble(endValue);
							newCriterion.setEndValue(doubleEndValue);
						}
					} else {
						newCriterion.setValue(null);
					}
					break;
				case BOOLEAN:
					Boolean booleanValue = null;
					if (value != null) {
						booleanValue = Boolean.parseBoolean(value);
					}
					newCriterion.setValue(booleanValue);
					break;
				case REFERENCE:
					newCriterion.setValue(value);
					if (endValue != null) {
						newCriterion.setEndValue(build(endValue));
					}
					break;
				case CONTENT:
					newCriterion.setValue(value);
					break;
				case STRUCTURE:
					newCriterion.setValue(value);
					break;
				case ENUM:
					Class clazz = null;
					try {
						clazz = Class.forName(criterion.getEnumClassName());
					} catch (ClassNotFoundException e) {
						newCriterion.setValue(null);
					}
					if (value != null) {
						newCriterion.setValue(Enum.valueOf(clazz, value));
					} else {
						newCriterion.setValue(null);
					}
					break;
				default:
					throw new UnsupportedOperationException("Unknow metadata type");
			}
		}

		return newCriterion;
	}

	//
	private JsonSerializer<LocalDateTime> newLocalDateTimeTypeAdapter() {
		return new JsonSerializer<LocalDateTime>() {
			@Override
			public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
				final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_TIME_ISO_PATTERN);
				return new JsonPrimitive(formatter.print(src));
			}
		};
	}

	private JsonSerializer<EnumWithSmallCode> newEnumWithSmallCodeTypeAdapter() {
		return new JsonSerializer<EnumWithSmallCode>() {
			@Override
			public JsonElement serialize(EnumWithSmallCode src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.getClass().getName());
			}
		};
	}

	private Gson gson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(EnumWithSmallCode.class, newEnumWithSmallCodeTypeAdapter());
		gsonBuilder = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, newLocalDateTimeTypeAdapter());
		return gsonBuilder.create();
	}
}
