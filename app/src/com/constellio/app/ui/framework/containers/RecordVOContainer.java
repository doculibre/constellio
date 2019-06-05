package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.PropertySetChangeNotifier;
import com.vaadin.data.Container.Sortable;

import java.util.List;

public interface RecordVOContainer extends Indexed, Sortable, Filterable, PropertySetChangeNotifier, ItemSetChangeNotifier, RefreshableContainer {

	RecordVO getRecordVO(Object itemId);

	List<MetadataSchemaVO> getSchemas();

}
