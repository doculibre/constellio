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
package com.constellio.app.ui.framework.components.fields.lookup;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField.AutocompleteSuggestionsProvider;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public abstract class LookupField<T extends Serializable> extends CustomField<T> {

	public static final String STYLE_NAME = "lookup";

	public static final String ERROR_STYLE_NAME = STYLE_NAME + "-error";

	public static final String AUTOCOMPLETE_FIELD_STYLE_NAME = STYLE_NAME + "-autocomplete-field";

	public static final String OPEN_WINDOW_BUTTON_STYLE_NAME = STYLE_NAME + "-open-window-button";

	public static final String LOOKUP_WINDOW_STYLE_NAME = STYLE_NAME + "-window";

	public static final String LOOKUP_WINDOW_CONTENT_STYLE_NAME = LOOKUP_WINDOW_STYLE_NAME + "-content";

	private static final String CAPTION_PROPERTY_ID = "caption";

	private HorizontalLayout mainLayout;

	private TextInputDataProvider<T> suggestInputDataProvider;

	private List<LookupTreeDataProvider<T>> lookupTreeDataProviders = new ArrayList<LookupField.LookupTreeDataProvider<T>>();

	private BaseAutocompleteField<T> autoCompleteField;

	private WindowButton lookupWindowButton;

	private Converter<String, T> itemConverter;

	private int treeBufferSize = 100;

	/**
	 * The component should receive focus (if {@link Focusable}) when attached.
	 */
	private boolean delayedFocus;

	@SuppressWarnings("unchecked")
	public LookupField(
			TextInputDataProvider<T> suggestInputDataProvider,
			LookupTreeDataProvider<T>... lookupTreeDataProviders) {
		this.suggestInputDataProvider = suggestInputDataProvider;
		if (lookupTreeDataProviders != null) {
			for (LookupTreeDataProvider<T> lookupTreeDataProvider : lookupTreeDataProviders) {
				this.lookupTreeDataProviders.add(lookupTreeDataProvider);
			}
		}
	}

	public int getTreeBufferSize() {
		return treeBufferSize;
	}

	public void setTreeBufferSize(int treeBufferSize) {
		this.treeBufferSize = treeBufferSize;
	}

	@Override
	protected Component initContent() {
		addStyleName(STYLE_NAME);
		setSizeFull();

		mainLayout = new HorizontalLayout();
		mainLayout.setSpacing(true);

		AutocompleteSuggestionsProvider<T> suggestionsProvider = new AutocompleteSuggestionsProvider<T>() {
			@Override
			public List<T> suggest(String text) {
				return suggestInputDataProvider.getData(text, 0, 10);
			}
		};
		autoCompleteField = newAutocompleteField(suggestionsProvider);
		autoCompleteField.addStyleName(AUTOCOMPLETE_FIELD_STYLE_NAME);
		autoCompleteField.setItemConverter(itemConverter);
		autoCompleteField.setPropertyDataSource(this);
		if (delayedFocus) {
			autoCompleteField.focus();
		}

		if (!lookupTreeDataProviders.isEmpty()) {
			lookupWindowButton = new WindowButton(null, $("search")) {
				@Override
				protected Component buildWindowContent() {
					return new LookupWindowContent(getWindow());
				}
			};
			lookupWindowButton.setIcon(new ThemeResource("images/commun/assistant.gif"));
			lookupWindowButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
			lookupWindowButton.addStyleName(OPEN_WINDOW_BUTTON_STYLE_NAME);
			lookupWindowButton.setZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX);

			addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					if (lookupWindowButton.getWindow() != null) {
						Window lookupWindow = lookupWindowButton.getWindow();
						lookupWindow.close();
					}
				}
			});
		}

		mainLayout.addComponent(autoCompleteField);
		if (!lookupTreeDataProviders.isEmpty()) {
			mainLayout.addComponent(lookupWindowButton);
			mainLayout.setExpandRatio(autoCompleteField, 1);
		}

		return mainLayout;
	}

	protected BaseAutocompleteField<T> newAutocompleteField(AutocompleteSuggestionsProvider<T> suggestionsProvider) {
		return new BaseAutocompleteField<T>(suggestionsProvider);
	}

	@SuppressWarnings("unchecked")
	protected LazyTree<T> newLazyTree(LookupTreeDataProvider<T> lookupTreeDataProvider, int treeBufferSize) {
		return new LazyTree<T>(lookupTreeDataProvider, treeBufferSize) {
			@Override
			public String getItemCaption(T itemId) {
				return LookupField.this.getCaption(itemId);
			}
		};
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

	protected String getCaptionForLazyTree(LazyTreeDataProvider<T> lazyTreeDataProvider) {
		return lazyTreeDataProvider.toString();
	}

	@Override
	public String getRequiredError() {
		return autoCompleteField.getRequiredError();
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		autoCompleteField.setRequiredError(requiredMessage);
		addStyleName(ERROR_STYLE_NAME);
	}

	@Override
	public String getConversionError() {
		return autoCompleteField.getConversionError();
	}

	@Override
	public void setConversionError(String valueConversionError) {
		autoCompleteField.setConversionError(valueConversionError);
		addStyleName(ERROR_STYLE_NAME);
	}

	@Override
	public ErrorMessage getComponentError() {
		return autoCompleteField.getComponentError();
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		autoCompleteField.setComponentError(componentError);
	}

	@Override
	public void validate()
			throws InvalidValueException {
		try {
			autoCompleteField.validate();
			super.validate();
			removeStyleName(ERROR_STYLE_NAME);
		} catch (InvalidValueException e) {
			throw e;
		}
	}

	@Override
	public void focus() {
		super.focus();
		if (autoCompleteField != null) {
			autoCompleteField.focus();
			delayedFocus = false;
		} else {
			delayedFocus = true;
		}
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		for (LookupTreeDataProvider<?> provider : lookupTreeDataProviders) {
			if (provider instanceof RecordLookupTreeDataProvider) {
				((RecordLookupTreeDataProvider) provider).setIgnoreLinkability(ignoreLinkability);
			}
		}
	}

	private class LookupWindowContent extends VerticalLayout {

		private HorizontalLayout searchFieldLayout;

		private TextField searchField;

		private Button searchButton;

		private Component lookupTreeComponent;

		private Table searchResultsTable;

		@SuppressWarnings("unchecked")
		public LookupWindowContent(Window window) {
			super();

			window.setWidth("80%");
			window.setHeight("80%");
			window.setResizable(true);
			window.addStyleName(LOOKUP_WINDOW_STYLE_NAME);

			addStyleName(LOOKUP_WINDOW_CONTENT_STYLE_NAME);
			setSpacing(true);

			searchFieldLayout = new HorizontalLayout();
			searchFieldLayout.setWidthUndefined();
			searchFieldLayout.setSpacing(true);

			searchField = new BaseTextField();
			searchField.focus();
			OnEnterKeyHandler onEnterKeyHandler = new OnEnterKeyHandler() {
				@Override
				public void onEnterKeyPressed() {
					search();
				}
			};
			onEnterKeyHandler.installOn(searchField);

			searchButton = new Button($("search"));
			searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			searchButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					search();
				}
			});

			if (lookupTreeDataProviders.size() > 1) {
				lookupTreeComponent = new TabSheet();
			}
			for (final LookupTreeDataProvider<T> lookupTreeDataProvider : lookupTreeDataProviders) {
				LazyTree<T> lazyTree = newLazyTree(lookupTreeDataProvider, treeBufferSize);
				lazyTree.setWidth("100%");
				lazyTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
				lazyTree.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
				lazyTree.addItemClickListener(new ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						T objectClicked = (T) event.getItemId();
						if (lookupTreeDataProvider.isSelectable(objectClicked)) {
							LookupField.this.setValue(objectClicked);
						}
					}
				});

				if (lookupTreeComponent == null) {
					lookupTreeComponent = lazyTree;
				} else {
					TabSheet tabSheet = (TabSheet) lookupTreeComponent;
					String lazyTreeCaption = getCaptionForLazyTree(lookupTreeDataProvider);
					tabSheet.addTab(lazyTree, lazyTreeCaption);
					selectDefaultUserTaxonomyTab(lazyTree, tabSheet);
				}
			}

			searchResultsTable = new Table();
			searchResultsTable.setWidth("100%");
			searchResultsTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

			addComponents(searchFieldLayout, lookupTreeComponent);
			searchFieldLayout.addComponents(searchField, searchButton);

			setExpandRatio(lookupTreeComponent, 1);
			searchFieldLayout.setExpandRatio(searchField, 1);
		}

		@SuppressWarnings("unchecked")
		private LookupTreeDataProvider<T> getCurrentTreeDataProvider() {
			LazyTree<T> currentLazyTree;
			if (lookupTreeComponent instanceof TabSheet) {
				TabSheet tabSheet = (TabSheet) lookupTreeComponent;
				currentLazyTree = (LazyTree<T>) tabSheet.getSelectedTab();
			} else {
				currentLazyTree = (LazyTree<T>) lookupTreeComponent;
			}
			return (LookupTreeDataProvider<T>) currentLazyTree.getDataProvider();
		}

		private void search() {
			String text = searchField.getValue();
			if (StringUtils.isNotBlank(text)) {
				if (lookupTreeComponent.isVisible()) {
					lookupTreeComponent.setVisible(false);
					searchResultsTable.setVisible(true);
				}
				LookupTreeDataProvider<T> currentDataProvider = getCurrentTreeDataProvider();
				Container searchResultsContainer = new LookupSearchResultContainer(currentDataProvider.search(), searchField);
				searchResultsTable.setContainerDataSource(searchResultsContainer);
				replaceComponent(lookupTreeComponent, searchResultsTable);
				setExpandRatio(searchResultsTable, 0);
			} else {
				lookupTreeComponent.setVisible(true);
				searchResultsTable.setVisible(false);
				replaceComponent(searchResultsTable, lookupTreeComponent);
				setExpandRatio(lookupTreeComponent, 1);
			}
		}

	}

	private void selectDefaultUserTaxonomyTab(LazyTree<T> lazyTree, TabSheet tabSheet) {
		RecordTextInputDataProvider recordTextInputDataProvider = (RecordTextInputDataProvider) suggestInputDataProvider;
		User user = recordTextInputDataProvider.getCurrentUser();
		RecordLazyTree recordLazyTree = (RecordLazyTree) lazyTree;
		RecordLookupTreeDataProvider recordLookupTreeDataProvider = (RecordLookupTreeDataProvider) recordLazyTree
				.getDataProvider();
		if (recordLookupTreeDataProvider.getTaxonomyCode().equals(user.getDefaultTaxonomy())) {
			tabSheet.setSelectedTab(lazyTree);
		}
	}

	public static interface LookupTreeDataProvider<T extends Serializable> extends LazyTreeDataProvider<T> {

		TextInputDataProvider<T> search();

		boolean isSelectable(T selection);

	}

	public interface TextInputDataProvider<T> extends DataProvider {

		List<T> getData(String text, int startIndex, int count);

		int size(String text);

	}

	private class LookupSearchResultContainer extends LazyQueryContainer {

		public LookupSearchResultContainer(TextInputDataProvider<T> lookupData, Property<String> property) {
			super(new LookupSearchResultLazyQueryDefinition(), new LookupSearchResultLazyQueryFactory(lookupData, property));
		}

	}

	private class LookupSearchResultLazyQueryDefinition extends LazyQueryDefinition {

		public LookupSearchResultLazyQueryDefinition() {
			super(true, 100, null);
			addProperty(CAPTION_PROPERTY_ID, Button.class, null, true, false);
		}

	}

	private class LookupSearchResultLazyQueryFactory implements QueryFactory, Serializable {

		private TextInputDataProvider<T> lookupData;

		private Property<String> property;

		public LookupSearchResultLazyQueryFactory(TextInputDataProvider<T> lookupData, Property<String> property) {
			this.lookupData = lookupData;
			this.property = property;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			return new SerializableQuery() {
				@Override
				public int size() {
					int size;
					String text = property.getValue();
					if (StringUtils.isNotBlank(text)) {
						size = lookupData.size(text);
					} else {
						size = 0;
					}
					return size;
				}

				@Override
				public List<Item> loadItems(int startIndex, int count) {
					List<Item> dataItems = new ArrayList<Item>();
					String text = property.getValue();
					if (StringUtils.isNotBlank(text)) {
						List<T> dataObjects = lookupData.getData(text, startIndex, count);
						for (T dataObject : dataObjects) {
							Item dataItem = new DataItem(dataObject);
							dataItems.add(dataItem);
						}
					}
					return dataItems;
				}

				@Override
				public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public boolean deleteAllItems() {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public Item constructItem() {
					throw new UnsupportedOperationException("Query is read-only");
				}
			};
		}

	}

	private class DataItem implements Item {

		private T item;

		public DataItem(T item) {
			this.item = item;
		}

		@Override
		public Property<?> getItemProperty(Object id) {
			Property<?> property;
			if (CAPTION_PROPERTY_ID.equals(id)) {
				String caption = getCaption(item);
				Button selectButton = new Button(caption);
				selectButton.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						LookupField.this.setValue(item);
					}
				});
				selectButton.addStyleName(ValoTheme.BUTTON_LINK);
				property = new ObjectProperty<Button>(selectButton);
			} else {
				property = null;
			}
			return property;
		}

		@Override
		public Collection<?> getItemPropertyIds() {
			return Arrays.asList(CAPTION_PROPERTY_ID);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean addItemProperty(Object id, Property property)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Item is read-only");
		}

		@Override
		public boolean removeItemProperty(Object id)
				throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Item is read-only");
		}

	}

	private interface SerializableQuery extends Query, Serializable {

	}

}
