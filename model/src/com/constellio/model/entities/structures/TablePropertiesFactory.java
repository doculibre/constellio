package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class TablePropertiesFactory implements StructureFactory {

	private static final String NULL = "~null~";
	private static final String DELIMITER = ":";
	private static final String ITEM_DELIMITER = ",";
	private static final String VALUE_DELIMITER = "=";

	@Override
	public ModifiableStructure build(String value) {
		if (value == null) {
			return null;
		}

		StringTokenizer stringTokenizer = new StringTokenizer(value, DELIMITER);

		TableProperties properties = new TableProperties();
		properties.tableId = readString(stringTokenizer);
		properties.visibleColumnIds = readList(stringTokenizer);
		properties.columnWidths = readMap(stringTokenizer);
		properties.sortedColumnId = readString(stringTokenizer);
		properties.isSortedAscending = readBoolean(stringTokenizer);
		properties.dirty = false;
		return properties;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		if (structure == null) {
			return null;
		}

		TableProperties properties = (TableProperties) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, properties.getTableId());
		writeList(stringBuilder, properties.getVisibleColumnIds());
		writeMap(stringBuilder, properties.getColumnWidths());
		writeString(stringBuilder, properties.getSortedColumnId());
		writeBoolean(stringBuilder, properties.getSortedAscending());
		return stringBuilder.toString();
	}

	private Map<String, Integer> readMap(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		if (value == null) {
			return null;
		}

		Map<String, Integer> valueMap = new HashMap<>();
		StringTokenizer mapTokenizer = new StringTokenizer(value, ITEM_DELIMITER);
		while (mapTokenizer.hasMoreTokens()) {
			String item = readString(mapTokenizer);
			if (item != null) {
				String[] parts = item.split(VALUE_DELIMITER);
				if (parts.length == 2) {
					valueMap.put(parts[0], Integer.parseInt(parts[1]));
				}
			}
		}
		return valueMap;
	}

	private void writeMap(StringBuilder stringBuilder, Map<String, Integer> valueMap) {
		if (valueMap != null) {
			StringBuilder mapBuilder = new StringBuilder();
			for (Entry<String, Integer> entry : valueMap.entrySet()) {
				String value = entry.getKey() + VALUE_DELIMITER + entry.getValue();
				writeString(ITEM_DELIMITER, mapBuilder, value);
			}
			writeString(stringBuilder, mapBuilder.toString());
		} else {
			writeString(stringBuilder, null);
		}
	}

	private List<String> readList(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		if (value == null) {
			return null;
		}

		List<String> valueList = new ArrayList<>();
		StringTokenizer listTokenizer = new StringTokenizer(value, ITEM_DELIMITER);
		while (listTokenizer.hasMoreTokens()) {
			String item = readString(listTokenizer);
			valueList.add(item);
		}
		return valueList;
	}

	private void writeList(StringBuilder stringBuilder, List<String> valueList) {
		if (valueList != null) {
			StringBuilder listBuilder = new StringBuilder();
			for (String value : valueList) {
				writeString(ITEM_DELIMITER, listBuilder, value);
			}
			writeString(stringBuilder, listBuilder.toString());
		} else {
			writeString(stringBuilder, null);
		}
	}

	private Boolean readBoolean(StringTokenizer stringTokenizer) {
		String value = readString(stringTokenizer);
		if (value == null) {
			return null;
		}
		return "1".equals(value);
	}

	private void writeBoolean(StringBuilder stringBuilder, Boolean value) {
		if (value == null) {
			writeString(stringBuilder, null);
		} else {
			writeString(stringBuilder, value ? "1" : "0");
		}
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		}
		return value;
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		writeString(DELIMITER, stringBuilder, value);
	}

	private void writeString(String delimiter, StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(delimiter);
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value);
		}
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
