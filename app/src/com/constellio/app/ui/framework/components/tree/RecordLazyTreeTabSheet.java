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

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;

public class RecordLazyTreeTabSheet extends TabSheet {

	private int bufferSize;
	private List<RecordLazyTreeDataProvider> dataProviders;
	private TaxonomyCodeToCaptionConverter captionConverter = new TaxonomyCodeToCaptionConverter();

	public RecordLazyTreeTabSheet(String[] taxonomyCodes) {
		this(taxonomyCodes, Integer.MAX_VALUE);
	}

	public RecordLazyTreeTabSheet(String[] taxonomyCodes, int bufferSize) {
		this(toDataProviders(taxonomyCodes), bufferSize);
	}

	public RecordLazyTreeTabSheet(List<RecordLazyTreeDataProvider> dataProviders) {
		this(dataProviders, 20);
	}

	public RecordLazyTreeTabSheet(List<RecordLazyTreeDataProvider> dataProviders, int bufferSize) {
		this.dataProviders = dataProviders;
		this.bufferSize = bufferSize;
		int selectedTab = 0;

		User currentUser = new PresenterService(ConstellioFactories.getInstance().getModelLayerFactory())
				.getCurrentUser(ConstellioUI.getCurrentSessionContext());

		PlaceHolder firstPlaceHolder = null;

		for (int i = 0; i < dataProviders.size(); i++) {
			RecordLazyTreeDataProvider dataProvider = dataProviders.get(i);
			String taxonomyCode = dataProvider.getTaxonomyCode();
			String lazyTreeCaption = getCaptionForTaxonomyCode(taxonomyCode);

			if (taxonomyCode.equals(currentUser.getDefaultTaxonomy())) {
				selectedTab = i;
			}
			PlaceHolder placeHolder = new PlaceHolder();
			if (i == 0) {
				firstPlaceHolder = placeHolder;
			}

			addTab(placeHolder, lazyTreeCaption);
		}

		addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				selectTab(getTab(getSelectedTab()));
			}
		});
		if (selectedTab == 0) {
			firstPlaceHolder.setCompositionRoot(newLazyTree(dataProviders.get(0), bufferSize));
		} else {
			setSelectedTab(getTab(selectedTab));
		}
	}

	private void selectTab(Tab tab) {
		if (tab == null) {
			return;
		}

		int position = getTabPosition(tab);
		setSelectedTab(position);

		PlaceHolder tabComponent = (PlaceHolder) tab.getComponent();
		if (tabComponent.getComponentCount() == 0) {
			tabComponent.setCompositionRoot(newLazyTree(dataProviders.get(position), bufferSize));
		}

	}

	private static List<RecordLazyTreeDataProvider> toDataProviders(String[] taxonomyCodes) {
		List<RecordLazyTreeDataProvider> dataProviders = new ArrayList<>();
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

	private static class PlaceHolder extends CustomComponent {
		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}
	}
}
