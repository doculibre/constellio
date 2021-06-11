package com.constellio.data.dao.services.solr;

import com.constellio.data.utils.ImpossibleRuntimeException;

import java.util.List;

public class SolrDataStoreTypesUtils {

	static SolrDataStoreTypesFactory factory = new SolrDataStoreTypesFactory();

	public static String getMultivalueFieldCode(String code) {
		if (code.endsWith(factory.forText(false))) {
			return code.replace("_" + factory.forText(false), "_" + factory.forText(true));
		}
		if (code.endsWith(factory.forBoolean(false))) {
			return code.replace("_" + factory.forBoolean(false), "_" + factory.forBoolean(true));
		}
		if (code.endsWith(factory.forDate(false))) {
			return code.replace("_" + factory.forDate(false), "_" + factory.forDate(true));
		}
		if (code.endsWith(factory.forDateTime(false))) {
			return code.replace("_" + factory.forDateTime(false), "_" + factory.forDateTime(true));
		}
		if (code.endsWith(factory.forDouble(false))) {
			return code.replace("_" + factory.forDouble(false), "_" + factory.forDouble(true));
		}
		if (code.endsWith(factory.forString(false))) {
			return code.replace("_" + factory.forString(false), "_" + factory.forString(true));
		}

		throw new ImpossibleRuntimeException("Invalid code '" + code + "'");
	}

	public static String getSinglevalueFieldCode(String code) {
		if (code.endsWith(factory.forText(true))) {
			return code.replace("_" + factory.forText(true), "_" + factory.forText(false));
		}
		if (code.endsWith(factory.forBoolean(true))) {
			return code.replace("_" + factory.forBoolean(true), "_" + factory.forBoolean(false));
		}
		if (code.endsWith(factory.forDate(true))) {
			return code.replace("_" + factory.forDate(true), "_" + factory.forDate(false));
		}
		if (code.endsWith(factory.forDateTime(true))) {
			return code.replace("_" + factory.forDateTime(true), "_" + factory.forDateTime(false));
		}
		if (code.endsWith(factory.forDouble(true))) {
			return code.replace("_" + factory.forDouble(true), "_" + factory.forDouble(false));
		}
		if (code.endsWith(factory.forString(true))) {
			return code.replace("_" + factory.forString(true), "_" + factory.forString(false));
		}

		throw new ImpossibleRuntimeException("Invalid code '" + code + "'");
	}

	public static String getTypeOrMutlivalueExtension(String type, boolean multivalue) {
		switch (type) {
			case "DATE":
				return factory.forDate(multivalue);
			case "DATE_TIME":
				return factory.forDateTime(multivalue);
			case "STRING":
			case "CONTENT":
			case "STRUCTURE":
			case "ENUM":
			case "REFERENCE":
				return factory.forString(multivalue);
			case "TEXT":
				return factory.forText(multivalue);
			case "INTEGER":
				return factory.forInteger(multivalue);
			case "NUMBER":
				return factory.forDouble(multivalue);
			case "BOOLEAN":
				return factory.forBoolean(multivalue);
		}

		throw new ImpossibleRuntimeException("Invalid type '" + type + "'");
	}

	/**
	 * Use carefully, may returns the wrong type as it only test the Java type of the value.
	 * So Reference & Text will not work. Also not all type are implemented yet.
	 *
	 * @return The datastore extension to use for the value
	 */
	public static String getTypeOrMultivalueExtensionForValue(Object value) {
		if (value == null || (value instanceof List && ((List<?>) value).isEmpty())) {
			return null;
		}

		boolean multivalue = value instanceof List;
		Object valueToDiscoverType = multivalue ? ((List) value).get(0) : value;
		String metadataValueTypeName;

		if (valueToDiscoverType instanceof String) {
			metadataValueTypeName = "STRING";
		} else if (valueToDiscoverType instanceof Integer) {
			metadataValueTypeName = "INTEGER";
		} else if (valueToDiscoverType instanceof Double) {
			metadataValueTypeName = "NUMBER";
		} else if (valueToDiscoverType instanceof Boolean) {
			metadataValueTypeName = "BOOLEAN";
		} else {
			throw new ImpossibleRuntimeException("Type for the value provided not found");
		}

		return getTypeOrMutlivalueExtension(metadataValueTypeName, multivalue);
	}
}
