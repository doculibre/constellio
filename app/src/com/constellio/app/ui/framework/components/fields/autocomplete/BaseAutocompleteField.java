package com.constellio.app.ui.framework.components.fields.autocomplete;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.*;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
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
public class BaseAutocompleteField<T> extends BaseComboBox {

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
		autocompleteContainer = new AutocompleteContainer();
		setContainerDataSource(autocompleteContainer);
		setPageLength(suggestionsProvider.getBufferSize());

		addValueChangeListener(new ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				T newValue = (T) event.getProperty().getValue();
				if (newValue != null && !autocompleteContainer.contains(newValue)) {
					autocompleteContainer.addContainerFilter(null);
					addSuggestion(newValue, 0);
				}
			}
		});
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		String newFilter;
		String filterstring = null;
	    if ((newFilter = (String) variables.get("filter")) != null) {
	        // this is a filter request
	        filterstring = newFilter;
	        if (filterstring != null) {
	            filterstring = filterstring.toLowerCase(getLocale());
	        }
	        updateItems(filterstring);
	    }
		super.changeVariables(source, variables);
	}

	private int subStringSize = prefixSize;
	private String querySubString;
	
	private void updateItems(String newFilterString) {
		if (newFilterString == null) {
			removeAllItems();
		} else if (newFilterString != null && !(newFilterString.equals(querySubString))) {
			// check to make sure the substring isn't null and its not equal to the last one
			setInternalValue(null);
			removeAllItems();
			// set the sub string
			querySubString = newFilterString;
			// check the size on the sub string
			if (querySubString.length() >= subStringSize) {
				// get the results
				querySuggestionsProvider(newFilterString);
			} // end if the substring langth is long enough
		}// end if newFilterString doesnt equal null and newFilterString doesnt equal last value
	}

	protected void querySuggestionsProvider(String text) {
		// Query the database here with the code you want
		// Store it how ever you want this example uses a list to demonstrate how to get the data to display
		List<T> dataList = suggestionsProvider.suggest(text);

		// add the results to the container
//		int i = 0;
		Iterator<T> iterDataList = dataList.iterator();
		while (iterDataList.hasNext()) {
			T suggestion = iterDataList.next();
			addSuggestion(suggestion);
//			i++;
		}// end while iter has next

	}// end queryDataBase method

	protected void addSuggestion(T suggestion) {
		addSuggestion(suggestion, -1);
	}

	@SuppressWarnings("unchecked")
	protected void addSuggestion(T suggestion, int index) {
		if (autocompleteContainer.containsId(suggestion)) {
			autocompleteContainer.removeItem(suggestion);
		}
		String suggestionCaption = getCaption(suggestion);
		Item item;
		if (index >= 0) {
			item = autocompleteContainer.addItemAt(index, suggestion);
		} else {
			item = autocompleteContainer.addItem(suggestion);
		}
		item.getItemProperty(CAPTION_PROPERTY_ID).setValue(suggestionCaption);
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
	
	protected void suggestionRequested(String text) {
		
	}

	public interface AutocompleteSuggestionsProvider<T> extends Serializable {

		List<T> suggest(String text);

		int getBufferSize();

	}

	private class AutocompleteContainer extends IndexedContainer {

		public AutocompleteContainer() {
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
			if (filter != null) {
				super.addContainerFilter(filter);
			}
		} 

	} 

}
