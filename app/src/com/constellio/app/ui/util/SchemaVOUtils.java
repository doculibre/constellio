package com.constellio.app.ui.util;

import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;

public class SchemaVOUtils {
	public static boolean isMetadataNotPresentInList(MetadataVO metadataVO, List<String> excludedMetadataCodeList) {

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
