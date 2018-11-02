package com.constellio.app.utils;

import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.Metadata;

public class MetadataUtil {
	public static boolean isMetadataAccessbile(MetadataVO metadataVO, List<Metadata> acceptedMetadataVO) {
		for(Metadata metadata : acceptedMetadataVO) {
			if(metadata.getCode().equals(metadataVO.getCode())) {
				return true;
			}
		}

		return false;
	}

	public static boolean isMetadataVOAccessbile(MetadataVO metadataVO, List<String> excludedMetadataCodeList) {

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
