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
		int selectedTab = -1;
		int configDefaultTab = -1;

		PresenterService presenterService = new PresenterService(ConstellioFactories.getInstance().getModelLayerFactory());
		User currentUser = presenterService.getCurrentUser(ConstellioUI.getCurrentSessionContext());
		String userDefaultTaxonomy = currentUser.getDefaultTaxonomy();
		String configDefaultTaxonomy = presenterService.getSystemConfigs().getDefaultTaxonomy();

		PlaceHolder firstPlaceHolder = null;

		for (int i = 0; i < dataProviders.size(); i++) {
			RecordLazyTreeDataProvider dataProvider = dataProviders.get(i);
			String taxonomyCode = dataProvider.getTaxonomyCode();
			String lazyTreeCaption = getCaptionForTaxonomyCode(taxonomyCode);

			if (taxonomyCode.equals(userDefaultTaxonomy)) {
				selectedTab = i;
			}
			if (taxonomyCode.equals(configDefaultTaxonomy)) {
				configDefaultTab = selectedTab;
			}
			PlaceHolder placeHolder = new PlaceHolder();
			if (i == 0) {
				firstPlaceHolder = placeHolder;
			}

			addTab(placeHolder, lazyTreeCaption);
		}
		if (selectedTab == -1 && configDefaultTab != -1) {
			selectedTab = configDefaultTab;
		}

		addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				selectTab(getTab(getSelectedTab()));
			}
		});
		if (selectedTab == -1) {
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
