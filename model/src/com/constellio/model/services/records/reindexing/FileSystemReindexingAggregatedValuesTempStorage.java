package com.constellio.model.services.records.reindexing;

import com.constellio.data.utils.KeyIntMap;
import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;
import com.constellio.model.services.records.reindexing.SystemReindexingConsumptionInfos.SystemReindexingConsumptionHeapInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.data.utils.LangUtils.sizeOf;
import static java.lang.Class.forName;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class FileSystemReindexingAggregatedValuesTempStorage implements ReindexingAggregatedValuesTempStorage {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemReindexingAggregatedValuesTempStorage.class);

	private Map<String, KeyIntMap<String>> referenceCounts = new HashMap<>();

	private File baseFolder;
	Set<String> idsWithFile = new HashSet<>();

	public FileSystemReindexingAggregatedValuesTempStorage(File baseFolder) {
		this.baseFolder = baseFolder;
	}

	@Override
	public void addOrReplace(String recordIdAggregatingValues, String recordId, String inputMetadataLocalCode,
							 List<Object> values) {

		idsWithFile.add(recordIdAggregatingValues);
		File file = new File(baseFolder, recordIdAggregatingValues);

		try {
			FileUtils.writeStringToFile(file, recordId + ":" + inputMetadataLocalCode + ":" + toString(values) + "\n", "UTF-8",
					true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private String toString(List<Object> values) {

		StringBuilder sb = new StringBuilder();

		for (Object value : values) {
			if (value != null) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(toScalarString(value));
			}
		}

		return sb.toString();
	}

	private String toScalarString(Object value) {

		if (value instanceof Double) {
			return "double:" + value;

		} else if (value instanceof Boolean) {
			return "boolean:" + value;

		} else if (value instanceof Enum) {
			return "enum:" + value.getClass().getName() + ":" + ((Enum) value).name();

		} else if (value instanceof String) {
			return "string:" + ((String) value).replace("\n", "\\~n~").replace(",", "\\~;~");

		} else if (value instanceof LocalDate) {
			return "date:" + ((LocalDate) value).toString();

		} else if (value instanceof LocalDateTime) {
			return "datetime:" + ((LocalDateTime) value).toString();

		} else {
			throw new RuntimeException("Unsupported type : " + value.getClass());
		}

	}

	private Object toScalarValue(String strValue) {
		strValue = strValue.replace("\\~;~", ",").replace("\\~n~", "\n");
		String type = substringBefore(strValue, ":");
		String restOfValue = substringAfter(strValue, ":");

		if ("double".equals(type)) {
			return Double.valueOf(restOfValue);

		} else if ("boolean".equals(type)) {
			return Boolean.valueOf(restOfValue);

		} else if ("enum".equals(type)) {
			String enumClass = substringBefore(restOfValue, ":");
			String enumValueName = substringAfter(restOfValue, ":");
			try {
				return getEnum((Class) forName(enumClass), enumValueName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

		} else if ("string".equals(type)) {
			return restOfValue;

		} else if ("date".equals(type)) {
			return LocalDate.parse(restOfValue);

		} else if ("datetime".equals(type)) {
			return LocalDateTime.parse(restOfValue);

		} else {
			throw new RuntimeException("Unsupported type : " + type);
		}
	}

	private List<Object> toValues(String str) {

		String[] split = str.split(",");
		List<Object> values = new ArrayList<>();
		for (String strValue : split) {
			values.add(toScalarValue(strValue));
		}

		return values;
	}

	@Override
	public List<Object> getAllValues(String recordIdAggregatingValues, String inputMetadataLocalCode) {

		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = getEntries(recordIdAggregatingValues);

		List<Object> returnedValues;
		if (entriesOfAggregatingRecord == null) {
			returnedValues = Collections.emptyList();

		} else {
			Map<String, List<Object>> entriesOfAggregatingRecordInputMetadata = entriesOfAggregatingRecord
					.get(inputMetadataLocalCode);
			if (entriesOfAggregatingRecordInputMetadata == null) {
				returnedValues = Collections.emptyList();

			} else {
				returnedValues = new ArrayList<>();
				for (List<Object> values : entriesOfAggregatingRecordInputMetadata.values()) {
					returnedValues.addAll(values);
				}
			}
		}

		return returnedValues;
	}

	@Override
	public void clear() {
		referenceCounts.clear();
		try {
			FileUtils.deleteDirectory(baseFolder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<AggregatedValuesEntry> getAllEntriesWithValues(String recordIdAggregatingValues) {

		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = getEntries(recordIdAggregatingValues);

		if (entriesOfAggregatingRecord == null) {
			return Collections.emptyList();
		} else {
			List<AggregatedValuesEntry> returnedEntries = new ArrayList<>();
			for (Map.Entry<String, Map<String, List<Object>>> entry : entriesOfAggregatingRecord.entrySet()) {

				String metadata = entry.getKey();
				for (Map.Entry<String, List<Object>> entry2 : entry.getValue().entrySet()) {
					String recordId = entry2.getKey();
					List<Object> values = entry2.getValue();
					returnedEntries.add(new AggregatedValuesEntry(recordId, metadata, values));
				}

			}

			return returnedEntries;
		}
	}

	private Map<String, Map<String, List<Object>>> getEntries(String recordIdAggregatingValues) {

		Map<String, Map<String, List<Object>>> entriesOfAggregatingRecord = new HashMap<>();


		if (idsWithFile.contains(recordIdAggregatingValues)) {

			File file = new File(baseFolder, recordIdAggregatingValues);
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {

				String line;
				while ((line = bufferedReader.readLine()) != null) {

					String recordId = StringUtils.substringBefore(line, ":");
					String restOfLine = StringUtils.substringAfter(line, ":");
					String inputMetadataLocalCode = StringUtils.substringBefore(restOfLine, ":");
					String valuesStr = StringUtils.substringAfter(restOfLine, ":");
					List<Object> values = valuesStr.isEmpty() ? new ArrayList<>() : toValues(valuesStr);

					Map<String, List<Object>> entriesOfAggregatingRecordInputMetadata = entriesOfAggregatingRecord
							.get(inputMetadataLocalCode);
					if (entriesOfAggregatingRecordInputMetadata == null) {
						entriesOfAggregatingRecordInputMetadata = new HashMap<>();
						entriesOfAggregatingRecord.put(inputMetadataLocalCode, entriesOfAggregatingRecordInputMetadata);
					}

					entriesOfAggregatingRecordInputMetadata.put(recordId, values);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return entriesOfAggregatingRecord;
	}

	@Override
	public void incrementReferenceCount(String recordIdAggregatingValues, String aggregatedMetadataLocalCode) {
		if (!referenceCounts.containsKey(recordIdAggregatingValues)) {
			referenceCounts.put(recordIdAggregatingValues, new KeyIntMap<String>());
		}
		referenceCounts.get(recordIdAggregatingValues).increment(aggregatedMetadataLocalCode);
	}

	@Override
	public int getReferenceCount(String recordIdAggregatingValues, String aggregatedMetadataLocalCode) {
		KeyIntMap<String> keyIntMap = referenceCounts.get(recordIdAggregatingValues);
		return keyIntMap != null ? keyIntMap.get(aggregatedMetadataLocalCode) : 0;
	}

	@Override
	public void populateCacheConsumptionInfos(SystemReindexingConsumptionInfos infos) {


		infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo(
				"FileSystemReindexingAggregatedValuesTempStorage.referenceCounts", sizeOf(referenceCounts)));

		infos.getHeapInfos().add(new SystemReindexingConsumptionHeapInfo(
				"FileSystemReindexingAggregatedValuesTempStorage.idsWithFileConsumption", sizeOf(idsWithFile)));

	}

}
