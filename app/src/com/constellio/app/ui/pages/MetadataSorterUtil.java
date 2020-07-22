package com.constellio.app.ui.pages;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.AccentApostropheCleaner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MetadataSorterUtil {
	public static void sort(List<MetadataVO> metadataVOs, SessionContext sessionContext) {
		Collections.sort(metadataVOs, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				String firstLabel = AccentApostropheCleaner.removeAccents(o1.getLabel(sessionContext).toLowerCase());
				String secondLabel = AccentApostropheCleaner.removeAccents(o2.getLabel(sessionContext).toLowerCase());
				return firstLabel.compareTo(secondLabel);
			}
		});
	}

}
