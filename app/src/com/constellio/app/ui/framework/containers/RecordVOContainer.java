package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.PropertySetChangeNotifier;
import com.vaadin.data.Container.Sortable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface RecordVOContainer extends Indexed, Sortable, Filterable, PropertySetChangeNotifier, ItemSetChangeNotifier, RefreshableContainer {

	RecordVO getRecordVO(Object itemId);

	default List<RecordVO> getRecordsVO(List<Object> itemIds) {
		List<RecordVO> recordVOS = new ArrayList<>();

		for (Object itemId : itemIds) {
			recordVOS.add(getRecordVO(itemId));
		}

		return recordVOS;
	}

	List<MetadataSchemaVO> getSchemas();

	Map<String, List<String>> getHighlights(Object itemId);

}
