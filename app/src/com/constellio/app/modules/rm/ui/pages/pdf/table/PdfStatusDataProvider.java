package com.constellio.app.modules.rm.ui.pages.pdf.table;

import com.constellio.app.ui.framework.data.DataProvider;

import java.util.Collection;
import java.util.List;

public interface PdfStatusDataProvider<T> extends DataProvider {
	public List<T> listPdfStatus();

	public Collection<?> getOwnContainerPropertyIds();

	public Class<?> getOwnType(Object propertyId);

	public Object getOwnValue(Object itemId, Object propertyId);
}
