package com.constellio.app.ui.framework.components.fields.autocomplete;

import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;

public class RecordAutocompleteField extends BaseAutocompleteField<String> {

	public RecordAutocompleteField(String schemaTypeCode) {
		this(schemaTypeCode, false);
	}

	public RecordAutocompleteField(String schemaTypeCode, boolean writeAccess) {
		super(new RecordAutocompleteSuggestionsProvider(schemaTypeCode, writeAccess));
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
		
		private static final int BUFFER_SIZE = 100;

		private RecordTextInputDataProvider suggestionsProvider;

		private RecordAutocompleteSuggestionsProvider(String schemaTypeCode, boolean writeAccess) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			this.suggestionsProvider = new RecordTextInputDataProvider(constellioFactories, sessionContext, schemaTypeCode,
					writeAccess);
		}

		@Override
		public List<String> suggest(String text) {
			return suggestionsProvider.getData(text, 0, BUFFER_SIZE);
		}

		@Override
		public int getBufferSize() {
			return BUFFER_SIZE;
		}

	}

}
