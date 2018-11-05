package com.constellio.app.ui.util;

import com.constellio.app.ui.entities.MetadataVO;

import java.util.List;

public class SchemaVOUtils {
	public static boolean isMetadataPresentInList(MetadataVO metadataVO, List<String> excludedMetadataCodeList) {

		if(excludedMetadataCodeList == null || 0 >= excludedMetadataCodeList.size()) {
			return true;
		}

		for(String metadataCode : excludedMetadataCodeList) {
			if(metadataCode.equals(metadataVO.getCode())) {
				return false;
			}
		}

		return true;
	}
}
