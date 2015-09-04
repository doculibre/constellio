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

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;

/**
 * Adapted from https://vaadin.com/forum#!/thread/897171/9060502
 *
 * @author Vincent
 *
 * @param <T>
 *            Type of the selected object
 */
public class BaseAutocompleteField<T> extends ComboBox {

	public static final String STYLE_NAME = "autocomplete";

	private static final String ERROR_STYLE_NAME = STYLE_NAME + "-error";

	private static final String CAPTION_PROPERTY_ID = "caption";

	private AutocompleteSuggestionsProvider<T> suggestionsProvider;

	private Converter<String, T> itemConverter;

	private int prefixSize = 0; // Default value

	private AutocompleteContainer autocompleteContainer;

	public BaseAutocompleteField(AutocompleteSuggestionsProvider<T> suggestionsProvider) {
		super();
		this.suggestionsProvider = suggestionsProvider;

		addStyleName(STYLE_NAME);
		setImmediate(true);
		setFilteringMode(FilteringMode.STARTSWITH);
		setInputPrompt($("BaseAutocompleteField.inputPrompt"));
		setTextInputAllowed(true);
		setNullSelectionAllowed(true);
		setItemCaptionMode(ItemCaptionMode.PROPERTY);
		setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
		autocompleteContainer = new AutocompleteContainer(prefixSize);
		setContainerDataSource(autocompleteContainer);

		addValueChangeListener(new ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				T newValue = (T) event.getProperty().getValue();
				if (newValue != null && !autocompleteContainer.contains(newValue)) {
					autocompleteContainer.addContainerFilter(null);
					autocompleteContainer.addSuggestion(newValue);
				}
			}
		});
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		super.setRequiredError(requiredMessage);
		addStyleName(ERROR_STYLE_NAME);
	}

	@Override
	public void validate()
			throws InvalidValueException {
		try {
			super.validate();
			removeStyleName(ERROR_STYLE_NAME);
		} catch (InvalidValueException e) {
			throw e;
		}
	}

	public AutocompleteSuggestionsProvider<T> getSuggestionsProvider() {
		return suggestionsProvider;
	}

	public int getPrefixSize() {
		return prefixSize;
	}

	public void setPrefixSize(int prefixSize) {
		this.prefixSize = prefixSize;
	}

	public Converter<String, T> getItemConverter() {
		return itemConverter;
	}

	public void setItemConverter(Converter<String, T> itemConverter) {
		this.itemConverter = itemConverter;
	}

	private String getCaption(T object) {
		String caption;
		if (object != null) {
			if (itemConverter != null) {
				Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
				caption = itemConverter.convertToPresentation(object, String.class, locale);
			} else {
				caption = object.toString();
			}
		} else {
			caption = "";
		}
		return caption;
	}

	public interface AutocompleteSuggestionsProvider<T> extends Serializable {

		List<T> suggest(String text);

	}

	private class AutocompleteContainer extends IndexedContainer {

		private final int subStringSize;
		private String querySubString;

		public AutocompleteContainer(int prefixSize) {
			this.subStringSize = prefixSize;
			addContainerProperty(CAPTION_PROPERTY_ID, String.class, null);
		}

		@SuppressWarnings("unchecked")
		private boolean contains(T object) {
			boolean inSuggestions = false;
			for (Object itemId : getItemIds()) {
				T suggestion = (T) itemId;
				inSuggestions = object.equals(suggestion);
				if (inSuggestions) {
					break;
				}
			}
			return inSuggestions;
		}

		@Override
		protected boolean passesFilters(Object itemId) {
			// The combobox only contains filtered data
			return true;
		}

		public void addContainerFilter(Filter filter)
				throws UnsupportedFilterException {
			// Check to see if the filter is equal to null
			if (filter == null) {
				// remove the items
				removeAllItems();
				querySubString = null;
			} else {
				// remove the items
				removeAllItems();
				// check to see what type of filter we have
				if (filter instanceof SimpleStringFilter) {
					// get the query subString from the filter
					String newFilterString = ((SimpleStringFilter) filter).getFilterString();
					// check to make sure the substring isn't null and its not equal to the last one
					if (newFilterString != null && !(newFilterString.equals(querySubString))) {
						// set the sub string
						querySubString = newFilterString;
						// check the size on the sub string
						if (querySubString.length() >= subStringSize) {
							// get the results
							querySuggestionsProvider(newFilterString);
							// add the filter
							super.addContainerFilter(filter);
						} // end if the substring langth is long enough
					}// end if newFilterString doesnt equal null and newFilterString doesnt equal last value
				} // end if and instance of Simple Filter
			} // end else
		} // end addCoontainerFilter method

		private void querySuggestionsProvider(String text) {
			// Query the database here with the code you want
			// Store it how ever you want this example uses a list to demonstrate how to get the data to display
			List<T> dataList = suggestionsProvider.suggest(text);

			// add the results to the container
			int i = 0;
			Iterator<T> iterDataList = dataList.iterator();
			while (iterDataList.hasNext()) {
				T suggestion = iterDataList.next();
				addSuggestion(suggestion);
				i++;
			}// end while iter has next

		}// end queryDataBase method

		@SuppressWarnings("unchecked")
		private void addSuggestion(T suggestion) {
			String suggestionCaption = getCaption(suggestion);
			Item item = addItem(suggestion);
			item.getItemProperty(CAPTION_PROPERTY_ID).setValue(suggestionCaption);
		}

	} // end CustomLazyContainer class

}
