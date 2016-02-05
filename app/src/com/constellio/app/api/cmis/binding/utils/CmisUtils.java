package com.constellio.app.api.cmis.binding.utils;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ConstraintCannotBeUpdated;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ConstraintReadOnly;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ConstraintUnknown;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_InvalidArgument;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ObjectNotFound;

public class CmisUtils {

	private CmisUtils() {
	}

	/**
	 * Returns the boolean value of the given value or the default value if the given value is <code>null</code>.
	 */
	public static boolean getBooleanParameter(Boolean value, boolean def) {
		if (value == null) {
			return def;
		}

		return value.booleanValue();
	}

	/**
	 * Converts milliseconds into a {@link GregorianCalendar} object, setting the timezone to GMT and cutting milliseconds off.
	 */
	public static GregorianCalendar millisToCalendar(long millis) {
		GregorianCalendar result = new GregorianCalendar();
		result.setTimeZone(TimeZone.getTimeZone("GMT"));
		result.setTimeInMillis((long) (Math.ceil((double) millis / 1000) * 1000));

		return result;
	}

	/**
	 * Splits a filter statement into a collection of properties. If <code>filter</code> is <code>null</code>, empty or one of the
	 * properties is '*' , an empty collection will be returned.
	 */
	public static Set<String> splitFilter(String filter) {
		if (filter == null) {
			return null;
		}

		if (filter.trim().length() == 0) {
			return null;
		}

		Set<String> result = new HashSet<String>();
		for (String s : filter.split(",")) {
			s = s.trim();
			if (s.equals("*")) {
				return null;
			} else if (s.length() > 0) {
				result.add(s);
			}
		}

		// set a few base properties
		// query name == id (for base type properties)
		result.add(PropertyIds.OBJECT_ID);
		result.add(PropertyIds.OBJECT_TYPE_ID);
		result.add(PropertyIds.BASE_TYPE_ID);

		return result;
	}

	/**
	 * Gets the type id from a set of properties.
	 */
	public static String getObjectTypeId(Properties properties) {
		PropertyData<?> typeProperty = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
		if (!(typeProperty instanceof PropertyId)) {
			throw new CmisExceptions_InvalidArgument("Type Id");
		}

		String typeId = ((PropertyId) typeProperty).getFirstValue();
		if (typeId == null) {
			throw new CmisExceptions_InvalidArgument("Type Id");
		}

		return typeId;
	}

	/**
	 * Returns the first value of an id property.
	 */
	public static String getIdProperty(Properties properties, String name) {
		PropertyData<?> property = properties.getProperties().get(name);
		if (!(property instanceof PropertyId)) {
			return null;
		}

		return ((PropertyId) property).getFirstValue();
	}

	/**
	 * Returns the first value of a toAString property.
	 */
	public static String getStringProperty(Properties properties, String name) {
		PropertyData<?> property = properties.getProperties().get(name);
		if (!(property instanceof PropertyString)) {
			return null;
		}

		return ((PropertyString) property).getFirstValue();
	}

	/**
	 * Returns the first value of a datetime property.
	 */
	public static GregorianCalendar getDateTimeProperty(Properties properties, String name) {
		PropertyData<?> property = properties.getProperties().get(name);
		if (!(property instanceof PropertyDateTime)) {
			return null;
		}

		return ((PropertyDateTime) property).getFirstValue();
	}

	/**
	 * Checks if the property belong to the type and are settable.
	 */
	public static void checkTypeProperties(TypeDefinition type, Properties properties, String typeId, boolean isCreate) {
		// check type
		if (type == null) {
			throw new CmisExceptions_ObjectNotFound("type", typeId);
		}

		// check if all required properties are there
		for (PropertyData<?> prop : properties.getProperties().values()) {
			PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

			// do we know that property?
			if (propType == null) {
				throw new CmisExceptions_ConstraintUnknown(prop.getId());
			}

			// can it be set?
			if (propType.getUpdatability() == Updatability.READONLY) {
				throw new CmisExceptions_ConstraintReadOnly(prop.getId());
			}

			if (!isCreate) {
				// can it be set?
				if (propType.getUpdatability() == Updatability.ONCREATE) {
					throw new CmisExceptions_ConstraintCannotBeUpdated(prop.getId());
				}
			}
		}
	}

}
