package com.constellio.app.ui.framework.components.fields.lookup;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField.AutocompleteSuggestionsProvider;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
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
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.addons.lazyquerycontainer.*;

import java.io.Serializable;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class LookupField<T extends Serializable> extends CustomField<T> {
	public static final String STYLE_NAME = "lookup";
	public static final String ERROR_STYLE_NAME = STYLE_NAME + "-error";
	public static final String AUTOCOMPLETE_FIELD_STYLE_NAME = STYLE_NAME + "-autocomplete-field";
	public static final String OPEN_WINDOW_BUTTON_STYLE_NAME = STYLE_NAME + "-open-window-button";
	public static final String CLEAR_BUTTON_STYLE_NAME = STYLE_NAME + "-clear-button";
	public static final String LOOKUP_WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String LOOKUP_WINDOW_CONTENT_STYLE_NAME = LOOKUP_WINDOW_STYLE_NAME + "-content";
	private static final String CAPTION_PROPERTY_ID = "caption";
	private TextInputDataProvider<T> suggestInputDataProvider;
	private List<LookupTreeDataProvider<T>> lookupTreeDataProviders = new ArrayList<>();
	private BaseAutocompleteField<T> autoCompleteField;
	private WindowButton lookupWindowButton;
	private Button clearButton;
	private Converter<String, T> itemConverter;
	private int treeBufferSize = 100;

	protected boolean isShowDeactivated = true;

	/**
	 * The component should receive focus (if {@link Focusable}) when attached.
	 */
	private boolean delayedFocus;
	private Integer windowZIndex;

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

	public final Integer getWindowZIndex() {
		return windowZIndex;
	}

	public final void setWindowZIndex(Integer windowZIndex) {
		this.windowZIndex = windowZIndex;
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

		final int autoCompleteBuffer = 100;
		AutocompleteSuggestionsProvider<T> suggestionsProvider = new AutocompleteSuggestionsProvider<T>() {
			@Override
			public List<T> suggest(String text) {
				List<T> values = new ArrayList<>(suggestInputDataProvider.getData(text, 0, autoCompleteBuffer));
				if (itemConverter != null) {
					Collections.sort(values, new Comparator<T>() {
						@Override
						public int compare(T o1, T o2) {
							String s1 = itemConverter.convertToPresentation(o1, String.class, getLocale());
							String s2 = itemConverter.convertToPresentation(o2, String.class, getLocale());
							return s1.compareTo(s2);
						}
					});
				}
				return values;
			}

			@Override
			public int getBufferSize() {
				return autoCompleteBuffer;
			}
		};
		autoCompleteField = newAutocompleteField(suggestionsProvider);
		autoCompleteField.addStyleName(AUTOCOMPLETE_FIELD_STYLE_NAME);
		autoCompleteField.setItemConverter(itemConverter);
		autoCompleteField.setPropertyDataSource(this);
		if (delayedFocus) {
			autoCompleteField.focus();
		}

		lookupWindowButton = new WindowButton(null, $("search")) {
			@Override
			protected Component buildWindowContent() {
				return new LookupWindowContent(getWindow());
			}
		};
		lookupWindowButton.setIcon(new ThemeResource("images/icons/actions/view.png"));
		lookupWindowButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		lookupWindowButton.addStyleName(OPEN_WINDOW_BUTTON_STYLE_NAME);
		lookupWindowButton.setZIndex(windowZIndex);

		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (lookupWindowButton.getWindow() != null) {
					Window lookupWindow = lookupWindowButton.getWindow();
					lookupWindow.close();
				}
			}
		});

		clearButton = new Button(DeleteButton.ICON_RESOURCE);
		clearButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		clearButton.addStyleName(CLEAR_BUTTON_STYLE_NAME);
		clearButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				autoCompleteField.setValue(null);
			}
		});

		HorizontalLayout mainLayout = new HorizontalLayout(autoCompleteField, lookupWindowButton, clearButton);
		mainLayout.setExpandRatio(autoCompleteField, 1);
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		return mainLayout;
	}

	protected BaseAutocompleteField<T> newAutocompleteField(AutocompleteSuggestionsProvider<T> suggestionsProvider) {
		return new BaseAutocompleteField<>(suggestionsProvider);
	}

	@SuppressWarnings("unchecked")
	protected LazyTree<T> newLazyTree(LookupTreeDataProvider<T> lookupTreeDataProvider, int treeBufferSize) {
		return new LazyTree<T>(lookupTreeDataProvider, treeBufferSize) {
			@Override
			public String getItemCaption(T itemId) {
				return LookupField.this.getCaption(itemId);
			}

			@Override
			public void addItemClickListener(ItemClickListener listener) {
				super.addItemClickListener(listener);
			}
		};
	}

	public Converter<String, T> getItemConverter() {
		return itemConverter;
	}

	public void setItemConverter(Converter<String, T> itemConverter) {
		this.itemConverter = itemConverter;
	}

	protected String getCaption(T object) {
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
			if (autoCompleteField != null) {
				autoCompleteField.validate();
			}
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

	public void setOnlyLinkables(boolean onlyLinkables) {
		suggestInputDataProvider.setOnlyLinkables(onlyLinkables);
	}

	protected class LookupWindowContent extends VerticalLayout {

		private HorizontalLayout searchFieldLayout;

		private TextField searchField;

		private Button searchButton;

		private Component lookupTreeComponent;

		private Table searchResultsTable;

		@SuppressWarnings("unchecked")
		public LookupWindowContent(Window window) {
			super();

			setSizeFull();

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

			if (!getLookupTreeDataProviders().isEmpty()) {
				if (getLookupTreeDataProviders().size() > 1) {
					lookupTreeComponent = new TabSheet();
				}
				for (final LookupTreeDataProvider<T> lookupTreeDataProvider : getLookupTreeDataProviders()) {
					LazyTree<T> lazyTree = newLazyTree(lookupTreeDataProvider, treeBufferSize);
					lazyTree.setWidth("100%");
					lazyTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
					lazyTree.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
					lazyTree.addItemClickListener(new ItemClickListener() {
						@Override
						public void itemClick(ItemClickEvent event) {
							T objectClicked = (T) event.getItemId();
							if (lookupTreeDataProvider.isSelectable(objectClicked)) {
								if(LookupField.this.getValue() != null && LookupField.this.getValue().equals(objectClicked)) {
									if (lookupWindowButton.getWindow() != null) {
										Window lookupWindow = lookupWindowButton.getWindow();
										lookupWindow.close();
									}
								} else {
									LookupField.this.setValue(objectClicked);
								}
							}
						}
					});

					Panel lazyTreePanel = new Panel(lazyTree);
					lazyTreePanel.setWidth("100%");
					lazyTreePanel.setHeight("100%");

					if (lookupTreeComponent == null) {
						lookupTreeComponent = lazyTreePanel;
					} else {
						TabSheet tabSheet = (TabSheet) lookupTreeComponent;
						String lazyTreeCaption = getCaptionForLazyTree(lookupTreeDataProvider);
						tabSheet.addTab(lazyTreePanel, lazyTreeCaption);
						selectDefaultUserTaxonomyTab(lazyTree, tabSheet, lazyTreePanel);
					}
				}
			}

			searchResultsTable = new Table();
			searchResultsTable.setWidth("100%");
			searchResultsTable.setHeight("98%");
			searchResultsTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

			addComponent(searchFieldLayout);
			if (!getLookupTreeDataProviders().isEmpty()) {
				addComponent(lookupTreeComponent);
			} else {
				Container searchResultsContainer = new LookupSearchResultContainer(geSuggestInputDataProvider(), searchField);
				searchResultsTable.setContainerDataSource(searchResultsContainer);
				addComponent(searchResultsTable);
			}
			searchFieldLayout.addComponents(searchField, searchButton);

			if (!getLookupTreeDataProviders().isEmpty()) {
				setExpandRatio(lookupTreeComponent, 1);
			} else {
				setExpandRatio(searchResultsTable, 1);
			}
			searchFieldLayout.setExpandRatio(searchField, 1);
		}

		@SuppressWarnings("unchecked")
		private LookupTreeDataProvider<T> getCurrentTreeDataProvider() {
			Panel currentLazyTreePanel;
			if (lookupTreeComponent instanceof TabSheet) {
				TabSheet tabSheet = (TabSheet) lookupTreeComponent;
				currentLazyTreePanel = (Panel) tabSheet.getSelectedTab();
			} else {
				currentLazyTreePanel = (Panel) lookupTreeComponent;
			}
			LazyTree<T> currentLazyTree = (LazyTree<T>) currentLazyTreePanel.getContent();
			return (LookupTreeDataProvider<T>) currentLazyTree.getDataProvider();
		}

		public List<LookupTreeDataProvider<T>> getLookupTreeDataProviders() {
			return lookupTreeDataProviders;
		}

		private void search() {
			String text = searchField.getValue();
			if (lookupTreeComponent != null) {
				if (StringUtils.isNotBlank(text)) {
					if (lookupTreeComponent.isVisible()) {
						lookupTreeComponent.setVisible(false);
						searchResultsTable.setVisible(true);
					}
					LookupTreeDataProvider<T> currentDataProvider = getCurrentTreeDataProvider();
					Container searchResultsContainer;
					if(isShowDeactivated) {
						searchResultsContainer = new LookupSearchResultContainer(currentDataProvider.search(), searchField);
					}
					else {
						searchResultsContainer = new LookupSearchResultContainer(currentDataProvider.searchWithoutDisabled(), searchField);
					}

						searchResultsTable.setContainerDataSource(searchResultsContainer);
					replaceComponent(lookupTreeComponent, searchResultsTable);
					setExpandRatio(searchResultsTable, 1);
				} else {
					lookupTreeComponent.setVisible(true);
					searchResultsTable.setVisible(false);
					replaceComponent(searchResultsTable, lookupTreeComponent);
					setExpandRatio(lookupTreeComponent, 1);
				}
			} else {
				Container searchResultsContainer = new LookupSearchResultContainer(geSuggestInputDataProvider(), searchField);
				searchResultsTable.setContainerDataSource(searchResultsContainer);
			}
		}

		public TextInputDataProvider geSuggestInputDataProvider() {
			return suggestInputDataProvider;
		}

	}

	private void selectDefaultUserTaxonomyTab(LazyTree<T> lazyTree, TabSheet tabSheet, Panel lazyTreePanel) {
		User user = suggestInputDataProvider.getCurrentUser();
		String taxonomyCode = lazyTree.getDataProvider().getTaxonomyCode();
		String userDefaultTaxonomy = user.getDefaultTaxonomy();
		PresenterService presenterService = new PresenterService(suggestInputDataProvider.getModelLayerFactory());
		String configDefaultTaxonomy = presenterService.getSystemConfigs().getDefaultTaxonomy();
		if (userDefaultTaxonomy != null) {
			if (taxonomyCode.equals(userDefaultTaxonomy)) {
				tabSheet.setSelectedTab(lazyTreePanel);
			}
		} else if (taxonomyCode.equals(configDefaultTaxonomy)) {
			tabSheet.setSelectedTab(lazyTreePanel);
		}
	}

	public interface LookupTreeDataProvider<T extends Serializable> extends LazyTreeDataProvider<T> {

		TextInputDataProvider<T> search();

		TextInputDataProvider<T> searchWithoutDisabled();

		boolean isSelectable(T selection);
	}

	public static abstract class TextInputDataProvider<T> extends AbstractDataProvider {

		public abstract List<T> getData(String text, int startIndex, int count);

		public abstract ModelLayerFactory getModelLayerFactory();

		public abstract int size(String text);

		public abstract User getCurrentUser();

		public abstract void setOnlyLinkables(boolean onlyLinkables);
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
					//					if (StringUtils.isNotBlank(text)) {
					size = lookupData.size(text);
					//					} else {
					//						size = 0;
					//					}
					return size;
				}

				@Override
				public List<Item> loadItems(int startIndex, int count) {
					List<Item> dataItems = new ArrayList<Item>();
					String text = property.getValue();
					//					if (StringUtils.isNotBlank(text)) {
					List<T> dataObjects = lookupData.getData(text, startIndex, count);
					for (T dataObject : dataObjects) {
						Item dataItem = new DataItem(dataObject);
						dataItems.add(dataItem);
					}
					//					}
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
						if(LookupField.this.getValue() != null && LookupField.this.getValue().equals(item)) {
							if (lookupWindowButton.getWindow() != null) {
								Window lookupWindow = lookupWindowButton.getWindow();
								lookupWindow.close();
							}
						} else {
							LookupField.this.setValue(item);
						}
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
