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
package com.constellio.app.ui.pages.search.criteria;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class CriterionFactory implements StructureFactory {
	final String DATE_TIME_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	@Override
	public String toString(ModifiableStructure structure) {
		return gson().toJson(structure);
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

		newCriterion.setMetadata(criterion.getMetadataCode(), criterion.getMetadataType(), criterion.getEnumClassName());

		SearchOperator searchOperator = criterion.getSearchOperator();
		newCriterion.setSearchOperator(searchOperator);

		newCriterion.setLeftParens(criterion.isLeftParens());
		newCriterion.setRightParens(criterion.isRightParens());

		MetadataValueType metadataValueType = criterion.getMetadataType();

		String value = null;
		if (criterion.getValue() != null) {
			value = "" + criterion.getValue();
		}

		switch (metadataValueType) {
		case DATE:
			if (value != null) {
				LocalDateTime ldt = new LocalDateTime().parse(value);
				newCriterion.setValue(ldt);
			}

			String endValue = null;
			if (criterion.getEndValue() != null) {
				endValue = "" + criterion.getEndValue();
			}
			if (endValue != null) {
				LocalDateTime ldt = new LocalDateTime().parse(endValue);
				newCriterion.setEndValue(ldt);
			}

			break;
		case DATE_TIME:
			if (value != null) {
				LocalDateTime ldt = new LocalDateTime().parse(value);
				newCriterion.setValue(ldt);
			}

			endValue = (String) criterion.getEndValue();
			if (endValue != null) {
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
			double doubleValue = Double.parseDouble(value);
			newCriterion.setValue(doubleValue);
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
			newCriterion.setValue(Enum.valueOf(clazz, value));
			break;
		default:
			throw new UnsupportedOperationException("Unknow metadata type");
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
