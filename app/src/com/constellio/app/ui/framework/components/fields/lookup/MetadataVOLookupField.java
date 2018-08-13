package com.constellio.app.ui.framework.components.fields.lookup;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;

public class MetadataVOLookupField extends LookupField<MetadataVO> {
	public MetadataVOLookupField(List<MetadataVO> options) {
		this(options, null);
	}

	@SuppressWarnings("unchecked")
	public MetadataVOLookupField(List<MetadataVO> options, Converter<String, MetadataVO> itemConverter) {
		super(new MetadataVOTextInputDataProvider(options, itemConverter), new MetadataVOLookupTreeDataProvider(options, itemConverter));
		setItemConverter(itemConverter);
	}

	public void setOptions(List<MetadataVO> options) {
		for(LookupTreeDataProvider<MetadataVO> lookUpTreeDataProviders : this.getLookupTreeDataProviders()) {
			if(lookUpTreeDataProviders instanceof  MetadataVOLookupTreeDataProvider) {
				(((MetadataVOLookupTreeDataProvider) lookUpTreeDataProviders).getTextInputDataProvider()).setOptions(options);
			}
		}
		((MetadataVOTextInputDataProvider)this.suggestInputDataProvider).setOptions(options);
	}



	@Override
	public Class<? extends MetadataVO> getType() {
		return MetadataVO.class;
	}

	private static class MetadataVOTextInputDataProvider extends TextInputDataProvider<MetadataVO> {

		private List<MetadataVO> options;

		private Converter<String, MetadataVO> itemConverter;

		private MetadataVOTextInputDataProvider(List<MetadataVO> options, Converter<String, MetadataVO> itemConverter) {
			this.options = options;
			this.itemConverter = itemConverter;
		}

		public List<MetadataVO> getOptions() {
			return options;
		}

		public void setOptions(List<MetadataVO> options) {
			this.options = options;
		}

		private List<MetadataVO> filter(String text) {
			List<MetadataVO> results = new ArrayList<>();
			if (text != null) {
				text = AccentApostropheCleaner.cleanAll(text.toLowerCase());
				for (MetadataVO option : options) {
					String optionText;

					optionText = option.getLabel(ConstellioUI.getCurrent().getLocale());
					optionText = AccentApostropheCleaner.cleanAll(optionText);
					if (optionText.toLowerCase().contains(text)) {
						results.add(option);
					}
				}
			} else {
				results.addAll(options);
			}
			return results;
		}

		@Override
		public List<MetadataVO> getData(String text, int startIndex, int count) {
			List<MetadataVO> results = filter(text);
			int end = Math.min(startIndex + count, results.size());
			return results.subList(startIndex, end);
		}

		@Override
		public ModelLayerFactory getModelLayerFactory() {
			return ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
		}

		@Override
		public int size(String text) {
			return filter(text).size();
		}

		@Override
		public User getCurrentUser() {
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			ModelLayerFactory modelLayerFactory = getModelLayerFactory();
			String currentCollection = sessionContext.getCurrentCollection();
			UserVO currentUserVO = sessionContext.getCurrentUser();
			UserServices userServices = modelLayerFactory.newUserServices();
			return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
		}

		@Override
		public void setOnlyLinkables(boolean onlyLinkables) {
			// Ignore
		}
	}

	private static class MetadataVOLookupTreeDataProvider implements LookupTreeDataProvider<MetadataVO> {

		private Converter<String, MetadataVO> itemConverter;

		private MetadataVOLookupField.MetadataVOTextInputDataProvider textInputDataProvider;

		private MetadataVOLookupTreeDataProvider(List<MetadataVO> options, Converter<String, MetadataVO> itemConverter) {
			this.itemConverter = itemConverter;
			this.textInputDataProvider = new MetadataVOLookupField.MetadataVOTextInputDataProvider(options, itemConverter);
		}

		public MetadataVOLookupField.MetadataVOTextInputDataProvider getTextInputDataProvider() {
			return textInputDataProvider;
		}

		@Override
		public ObjectsResponse<MetadataVO> getRootObjects(int start, int maxSize) {
			List<MetadataVO> results = textInputDataProvider.getData(null, start, maxSize);
			return new ObjectsResponse<>(results, new Long(results.size()));
		}

		@Override
		public MetadataVO getParent(MetadataVO child) {
			return null;
		}

		@Override
		public ObjectsResponse<MetadataVO> getChildren(MetadataVO parent, int start, int maxSize) {
			return new ObjectsResponse<MetadataVO>(new ArrayList<MetadataVO>(), 0L);
		}

		@Override
		public boolean hasChildren(MetadataVO parent) {
			return false;
		}

		@Override
		public boolean isLeaf(MetadataVO object) {
			return true;
		}

		@Override
		public String getTaxonomyCode() {
			return null;
		}

		@Override
		public String getCaption(MetadataVO id) {
			return id.getLabel();
		}

		@Override
		public String getDescription(MetadataVO id) {
			return null;
		}

		@Override
		public Resource getIcon(MetadataVO id, boolean expanded) {
			return null;
		}

		@Override
		public int getEstimatedRootNodesCount() {
			return textInputDataProvider.size(null);
		}

		@Override
		public int getEstimatedChildrenNodesCount(MetadataVO parent) {
			return 0;
		}

		@Override
		public void addDataRefreshListener(DataRefreshListener dataRefreshListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<DataRefreshListener> getDataRefreshListeners() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeDataRefreshListener(DataRefreshListener dataRefreshListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void fireDataRefreshEvent() {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider<MetadataVO> search() {
			return textInputDataProvider;
		}

		@Override
		public com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider<MetadataVO> searchWithoutDisabled() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isSelectable(MetadataVO selection) {
			return true;
		}
	}
}
