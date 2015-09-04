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

		private RecordTextInputDataProvider suggestionsProvider;

		private RecordAutocompleteSuggestionsProvider(String schemaTypeCode, boolean writeAccess) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			this.suggestionsProvider = new RecordTextInputDataProvider(constellioFactories, sessionContext, schemaTypeCode,
					writeAccess);
		}

		@Override
		public List<String> suggest(String text) {
			return suggestionsProvider.getData(text, 0, 10);
		}

	}

}
