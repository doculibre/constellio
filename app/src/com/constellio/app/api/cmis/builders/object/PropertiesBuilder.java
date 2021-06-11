package com.constellio.app.api.cmis.builders.object;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.SeparatedStructureFactory;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import static com.constellio.app.api.cmis.utils.CmisRecordUtils.toGregorianCalendar;

public class PropertiesBuilder {

	private final ConstellioCollectionRepository repository;

	private final PropertiesImpl builtProperties;
	private final String typeId;
	private Set<String> filter;

	public PropertiesBuilder(ConstellioCollectionRepository repository, String typeId, Set<String> filter) {
		this.repository = repository;
		this.builtProperties = new PropertiesImpl();
		this.typeId = typeId;
		this.filter = filter;
	}

	public void addPropertyForSingleValueMetadata(Metadata metadata,
												  Object value, String propertyCode) {
		if (metadata.getType().isStringOrText() || metadata.getType() == MetadataValueType.REFERENCE) {
			addPropertyString(propertyCode, (String) value);

		} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
			addPropertyBoolean(propertyCode, (Boolean) value);

		} else if (metadata.getType() == MetadataValueType.ENUM) {
			addPropertyEnum(propertyCode, (EnumWithSmallCode) value);

		} else if (metadata.getType() == MetadataValueType.NUMBER) {
			addPropertyDouble(propertyCode, (Double) value);

		} else if (metadata.getType() == MetadataValueType.DATE_TIME || metadata.getType() == MetadataValueType.DATE) {
			GregorianCalendar calendarValue = toGregorianCalendar(value);
			addPropertyDateTime(propertyCode, calendarValue);

		} else if (metadata.getType() == MetadataValueType.CONTENT) {
			addPropertyString(propertyCode, ((Content) value).getId());

		} else if (metadata.isSeparatedStructure()) {
			addPropertySeparatedStructure(propertyCode, metadata, (ModifiableStructure) value);
		}
	}

	public void addPropertyForMultiValueMetadata(Metadata metadata, Object value, String propertyCode) {
		if (!((List) value).isEmpty()) {
			if (metadata.getType().isStringOrText() || metadata.getType() == MetadataValueType.REFERENCE) {
				addPropertyListString(propertyCode, (List<String>) value);

			} else if (metadata.getType() == MetadataValueType.ENUM) {
				addPropertyListEnum(propertyCode, (List<EnumWithSmallCode>) value);

			} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
				addPropertyListBoolean(propertyCode, (List<Boolean>) value);

			} else if (metadata.getType() == MetadataValueType.NUMBER) {
				addPropertyListInteger(propertyCode, (List<Long>) value);

			} else if (metadata.getType() == MetadataValueType.DATE_TIME || metadata.getType() == MetadataValueType.DATE) {
				List<GregorianCalendar> calendarValues = new ArrayList<>();
				for (Object dateObject : (List<Object>) value) {
					if (dateObject != null) {
						calendarValues.add(toGregorianCalendar(dateObject));
					}
				}
				if (!calendarValues.isEmpty()) {
					addPropertyListDateTime(propertyCode, calendarValues);
				}
			}
		}
	}

	public void addPropertyId(String id, String value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyIdImpl(id, value));
	}

	public void addPropertyIdList(String id, List<String> value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyIdImpl(id, value));
	}

	public void addPropertyString(String id, String value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyStringImpl(id, value));
	}

	public void addPropertySeparatedStructure(String id, Metadata metadata, ModifiableStructure value) {
		if (!checkAddProperty(id)) {
			return;
		}

		SeparatedStructureFactory factory = (SeparatedStructureFactory) metadata.getStructureFactory();
		Object mainValue = factory.toFields(value).get(factory.getMainValueFieldName());

		builtProperties.addProperty(new PropertyStringImpl(id, "" + mainValue));
	}

	public void addPropertyEnum(String id, EnumWithSmallCode value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyStringImpl(id, value == null ? null : value.getCode()));
	}

	public void addPropertyListString(String id, List<String> value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyStringImpl(id, value));
	}

	public void addPropertyListEnum(String id,
									List<EnumWithSmallCode> value) {
		if (!checkAddProperty(id)) {
			return;
		}

		List<String> convertedValues = new ArrayList<>();
		if (value != null) {
			for (EnumWithSmallCode item : value) {
				convertedValues.add(item.getCode());
			}
		}

		builtProperties.addProperty(new PropertyStringImpl(id, convertedValues));
	}

	public void addPropertyDouble(String id, Double value) {
		if (!checkAddProperty(id)) {
			return;
		}
		BigDecimal bigDecimalValue = new BigDecimal(value, MathContext.DECIMAL64);
		builtProperties.addProperty(new PropertyDecimalImpl(id, bigDecimalValue));
	}

	public void addPropertyListInteger(String id, List<Long> values) {
		List<BigInteger> convertedValues = new ArrayList<>();
		for (Long value : values) {
			convertedValues.add(BigInteger.valueOf(value));
		}

		addPropertyListBigInteger(id, convertedValues);
	}

	public void addPropertyListBigInteger(String id,
										  List<BigInteger> value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyIntegerImpl(id, value));
	}

	public void addPropertyBoolean(String id, boolean value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyBooleanImpl(id, value));
	}

	public void addPropertyListBoolean(String id, List<Boolean> value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyBooleanImpl(id, value));
	}

	public void addPropertyDateTime(String id,
									GregorianCalendar value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyDateTimeImpl(id, value));
	}

	public void addPropertyListDateTime(String id,
										List<GregorianCalendar> value) {
		if (!checkAddProperty(id)) {
			return;
		}

		builtProperties.addProperty(new PropertyDateTimeImpl(id, value));
	}

	public boolean checkAddProperty(String id) {
		if ((builtProperties == null) || (builtProperties.getProperties() == null)) {
			throw new IllegalArgumentException("Properties must not be null!");
		}

		if (id == null) {
			throw new IllegalArgumentException("Id must not be null!");
		}

		TypeDefinition type = repository.getTypeDefinitionsManager().getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new IllegalArgumentException("Unknown type: " + typeId);
		}
		if (!type.getPropertyDefinitions().containsKey(id)) {
			throw new IllegalArgumentException("Unknown property: " + id);
		}

		String queryName = type.getPropertyDefinitions().get(id).getQueryName();

		if ((queryName != null) && (filter != null)) {
			if (!filter.contains(queryName)) {
				return false;
			} else {
				filter.remove(queryName);
			}
		}

		return true;
	}

	public PropertiesImpl getBuiltProperties() {
		return builtProperties;
	}
}
