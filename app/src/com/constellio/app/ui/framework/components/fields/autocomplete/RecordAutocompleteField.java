package com.constellio.app.ui.framework.components.fields.autocomplete;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.ConverterWithCache;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.List;

public class RecordAutocompleteField extends BaseAutocompleteField<String> {

	public RecordAutocompleteField(String schemaTypeCode, ConverterWithCache<String, String> converterWithCache) {
		this(schemaTypeCode, false, converterWithCache);
	}

	public RecordAutocompleteField(String schemaTypeCode, boolean writeAccess,
								   ConverterWithCache<String, String> converterWithCache) {
		super(new RecordAutocompleteSuggestionsProvider(schemaTypeCode, writeAccess, converterWithCache));
		init();
	}

	public RecordAutocompleteField(AutocompleteSuggestionsProvider<String> suggestionsProvider) {
		super(suggestionsProvider);
		init();
	}

	private void init() {
		//		setItemConverter(new RecordIdToCaptionConverter());
	}

	private static class RecordAutocompleteSuggestionsProvider implements AutocompleteSuggestionsProvider<String> {

		private RecordTextInputDataProvider suggestionsProvider;

		private RecordAutocompleteSuggestionsProvider(String schemaTypeCode, boolean writeAccess,
													  ConverterWithCache<String, String> converterWithCache) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			this.suggestionsProvider = new RecordTextInputDataProvider(constellioFactories, sessionContext, schemaTypeCode,
					writeAccess).setConverterWithCache(converterWithCache);
		}

		@Override
		public List<String> suggest(String text) {
			return suggestionsProvider.getData(text, 0, getBufferSize());
		}

		@Override
		public int getBufferSize() {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			return modelLayerFactory.getSystemConfigs().getAutocompleteSize();
		}

		@Override
		public Class<String> getModelType() {
			return String.class;
		}

	}

}
