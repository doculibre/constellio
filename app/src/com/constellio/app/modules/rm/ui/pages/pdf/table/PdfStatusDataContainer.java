package com.constellio.app.modules.rm.ui.pages.pdf.table;

import com.constellio.app.ui.framework.containers.DataContainer;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

import java.util.Collection;
import java.util.List;

public class PdfStatusDataContainer extends DataContainer<PdfStatusDataProvider<?>> {

	public PdfStatusDataContainer(PdfStatusDataProvider<?> dataProvider) {
		super(dataProvider);
	}

	@Override
	protected void populateFromData(PdfStatusDataProvider<?> dataProvider) {
		List<?> pdfStatus = dataProvider.listPdfStatus();
		for (Object t : pdfStatus) {
			addItem(t);
		}
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		return getDataProvider().getOwnContainerPropertyIds();
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		return Label.class;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		String value = (String) getDataProvider().getOwnValue(itemId, propertyId);
		Label valueAsLabel = new Label(value, ContentMode.HTML);
		return new ObjectProperty(valueAsLabel, Label.class);
	}


}
