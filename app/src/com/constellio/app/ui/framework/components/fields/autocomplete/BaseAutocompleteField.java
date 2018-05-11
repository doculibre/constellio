package com.constellio.app.ui.framework.components.fields.autocomplete;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter;

import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteQuery;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestion;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestionProvider;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteTextField;

/**
 * Adapted from https://vaadin.com/forum#!/thread/897171/9060502
 *
 * @author Vincent
 *
 * @param <T>
 *            Type of the selected object
 */
public class BaseAutocompleteField<T> extends AutocompleteTextField {

	public static final String STYLE_NAME = "autocomplete";

	private static final String ERROR_STYLE_NAME = STYLE_NAME + "-error";

	private AutocompleteSuggestionsProvider<T> suggestionsProvider;

	private Converter<String, T> itemConverter;

	private int prefixSize = 0; // Default value

	public BaseAutocompleteField(final AutocompleteSuggestionsProvider<T> suggestionsProvider) {
		super();
		this.suggestionsProvider = suggestionsProvider;
		
		setMinChars(1);
		setSuggestionProvider(new AutocompleteSuggestionProvider() {
			@Override
			public Collection<AutocompleteSuggestion> querySuggestions(AutocompleteQuery query) {
				List<AutocompleteSuggestion> suggestions = new ArrayList<>();
				
				String term = query.getTerm();
				List<T> objectSuggestions = suggestionsProvider.suggest(term);
				for (T objectSuggestion : objectSuggestions) {
					String stringSuggestion = getCaption(objectSuggestion);
					if (!StringUtils.equalsIgnoreCase(stringSuggestion, term)) {
						suggestions.add(new AutocompleteSuggestion(stringSuggestion));
					}
				}
				return suggestions;
			}
		});

		addStyleName(STYLE_NAME);
		setInputPrompt($("BaseAutocompleteField.inputPrompt"));
		setNullRepresentation("");
//		setPageLength(suggestionsProvider.getBufferSize());
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
		setMinChars(prefixSize);
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

}
