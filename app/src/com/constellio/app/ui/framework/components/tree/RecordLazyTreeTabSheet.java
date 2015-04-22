/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.components.tree;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.vaadin.ui.TabSheet;

public class RecordLazyTreeTabSheet extends TabSheet {
	
	private TaxonomyCodeToCaptionConverter captionConverter = new TaxonomyCodeToCaptionConverter();
	
	public RecordLazyTreeTabSheet(String[] taxonomyCodes) {
		this(taxonomyCodes, Integer.MAX_VALUE);
	}

	public RecordLazyTreeTabSheet(String[] taxonomyCodes, int bufferSize) {
		this(toDataProviders(taxonomyCodes), bufferSize);
	}
	
	public RecordLazyTreeTabSheet(List<RecordLazyTreeDataProvider> dataProviders) {
		this(dataProviders, Integer.MAX_VALUE);
	}

	public RecordLazyTreeTabSheet(List<RecordLazyTreeDataProvider> dataProviders, int bufferSize) {
		for (RecordLazyTreeDataProvider dataProvider : dataProviders) {
			String taxonomyCode = dataProvider.getTaxonomyCode();
			String lazyTreeCaption = getCaptionForTaxonomyCode(taxonomyCode);
			RecordLazyTree lazyTree = newLazyTree(dataProvider, bufferSize);
			addTab(lazyTree, lazyTreeCaption);
		}
	}
	
	private static List<RecordLazyTreeDataProvider> toDataProviders(String[] taxonomyCodes) {
		List<RecordLazyTreeDataProvider> dataProviders = new ArrayList<RecordLazyTreeDataProvider>();
		for (String taxonomyCode : taxonomyCodes) {
			RecordLazyTreeDataProvider dataProvider = new RecordLazyTreeDataProvider(taxonomyCode);
			dataProviders.add(dataProvider);
		}	
		return dataProviders;
	}
	
	protected RecordLazyTree newLazyTree(RecordLazyTreeDataProvider dataProvider, int bufferSize) {
		return new RecordLazyTree(dataProvider, bufferSize);
	}
	
	protected String getCaptionForTaxonomyCode(String taxonomyCode) {
		return captionConverter.convertToPresentation(taxonomyCode, String.class, getLocale());
	}

}
