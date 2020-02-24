package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.StringRecordId;
import com.constellio.data.dao.dto.records.StringRecordIdLegacyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class StringRecordIdLegacyMemoryMapping implements StringRecordIdLegacyMapping {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringRecordIdLegacyMemoryMapping.class);
	private static Map<Integer, String> isMapping = new HashMap<>();
	private static Map<String, Integer> siMapping = new HashMap<>();
	private static int seq;
	private Runnable unfoundIdMappingRunnable;

	public StringRecordIdLegacyMemoryMapping(Runnable unfoundIdMappingRunnable) {
		this.unfoundIdMappingRunnable = unfoundIdMappingRunnable;
	}

	public int getIntId(String id) {
		Integer intId = siMapping.get(id);
		if (intId == null) {
			synchronized (StringRecordIdLegacyMemoryMapping.class) {
				//LOGGER.warn("String id '" + id + "' is mapped in memory, mapping will be lost on reboot");
				intId = siMapping.get(id);
				if (intId == null) {
					intId = seq--;

					isMapping.put(intId, id);
					siMapping.put(id, intId);
				}
			}
			return siMapping.get(id);
		}
		return intId;
	}

	public String getStringId(int intId) {
		if (intId > 0) {
			throw new IllegalArgumentException("Id is not legacy : " + intId);
		} else {
			String stringId = isMapping.get(intId);
			if (stringId == null) {
				unfoundIdMappingRunnable.run();
				stringId = StringRecordId.INVALID_ID;
			}
			return stringId;
		}
	}

}
