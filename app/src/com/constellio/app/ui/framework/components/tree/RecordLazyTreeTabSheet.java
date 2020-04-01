package com.constellio.app.ui.framework.components.tree;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.TreeDataProviderFactory;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordLazyTreeTabSheet extends TabSheet {

	private int bufferSize;
	private List<LazyTreeDataProvider<String>> dataProviders;
	private TaxonomyCodeToCaptionConverter captionConverter = new TaxonomyCodeToCaptionConverter();

	private static int getBufferSizeFromConfig() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		return modelLayerFactory.getSystemConfigs().getLazyTreeBufferSize();
	}

	public RecordLazyTreeTabSheet(String[] taxonomyCodes) {
		this(taxonomyCodes, getBufferSizeFromConfig());
	}

	public RecordLazyTreeTabSheet(String[] taxonomyCodes, int bufferSize) {
		this(toDataProviders(taxonomyCodes), bufferSize);
	}

	public RecordLazyTreeTabSheet(List<LazyTreeDataProvider<String>> dataProviders) {
		this(dataProviders, getBufferSizeFromConfig());
	}

	public RecordLazyTreeTabSheet(List<LazyTreeDataProvider<String>> dataProviders, int bufferSize) {
		this.dataProviders = dataProviders;
		this.bufferSize = bufferSize;
		int selectedTab = -1;
		int configDefaultTab = -1;

		PresenterService presenterService = new PresenterService(ConstellioFactories.getInstance().getModelLayerFactory());
		User currentUser = presenterService.getCurrentUser(ConstellioUI.getCurrentSessionContext());
		String userDefaultTaxonomy = currentUser.getDefaultTaxonomy();
		String configDefaultTaxonomy = presenterService.getSystemConfigs().getDefaultTaxonomy();

		//		PlaceHolder firstPlaceHolder = null;
		for (int i = 0; i < dataProviders.size(); i++) {
			LazyTreeDataProvider<String> dataProvider = dataProviders.get(i);
			String taxonomyCode = dataProvider.getTaxonomyCode();
			String lazyTreeCaption = getCaptionForTaxonomyCode(taxonomyCode);

			if (taxonomyCode.equals(userDefaultTaxonomy)) {
				selectedTab = i;
			}
			if (taxonomyCode.equals(configDefaultTaxonomy)) {
				configDefaultTab = selectedTab;
			}
			PlaceHolder placeHolder = new PlaceHolder();
			//			if (i == 0) {
			//				firstPlaceHolder = placeHolder;
			//			}

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
	}

	private void selectTab(Tab tab) {
		if (tab == null) {
			return;
		}

		int position = getTabPosition(tab);
		setSelectedTab(position);

		PlaceHolder tabComponent = (PlaceHolder) getSelectedTab();
		if (tabComponent.getComponentCount() == 0) {
			tabComponent.setCompositionRoot(newLazyTree(dataProviders.get(position), bufferSize));
		}
	}

	@Override
	public Locale getLocale() {
		Locale locale = super.getLocale();
		if (locale == null) {
			locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		}
		return locale;
	}

	@Override
	public void attach() {
		super.attach();

		PlaceHolder tabComponent = (PlaceHolder) getSelectedTab();
		if (tabComponent.getComponentCount() == 0) {
			Tab tab = getTab(tabComponent);
			int position = getTabPosition(tab);
			tabComponent.setCompositionRoot(newLazyTree(dataProviders.get(position), bufferSize));
		}
	}

	private static List<LazyTreeDataProvider<String>> toDataProviders(String[] taxonomyCodes) {
		List<LazyTreeDataProvider<String>> dataProviders = new ArrayList<>();
		for (String taxonomyCode : taxonomyCodes) {
			LazyTreeDataProvider<String> dataProvider = TreeDataProviderFactory.forTaxonomy(taxonomyCode, ConstellioUI.getCurrentSessionContext().getCurrentCollection());
			dataProviders.add(dataProvider);
		}
		return dataProviders;
	}

	protected RecordLazyTree newLazyTree(LazyTreeDataProvider<String> dataProvider, int bufferSize) {
		return new RecordLazyTree(dataProvider, bufferSize);
	}

	protected String getCaptionForTaxonomyCode(String taxonomyCode) {
		return captionConverter.convertToPresentation(taxonomyCode, String.class, getLocale());
	}

	public static class PlaceHolder extends CustomComponent {
		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}

		@Override
		public Component getCompositionRoot() {
			return super.getCompositionRoot();
		}
	}
}
