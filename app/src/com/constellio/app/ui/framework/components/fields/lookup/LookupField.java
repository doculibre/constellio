package com.constellio.app.ui.framework.components.fields.lookup;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.converters.ConverterWithCache;
import com.constellio.app.ui.framework.components.converters.RecordIdToDescriptionContent;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField.AutocompleteSuggestionsProvider;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.converter.ConverterUtil;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Extension;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class LookupField<T extends Serializable> extends CustomField<Object> {

	public static final String STYLE_NAME = "lookup";
	public static final String ERROR_STYLE_NAME = STYLE_NAME + "-error";
	public static final String AUTOCOMPLETE_FIELD_STYLE_NAME = STYLE_NAME + "-autocomplete-field";
	public static final String OPEN_WINDOW_BUTTON_STYLE_NAME = STYLE_NAME + "-open-window-button";
	public static final String CLEAR_BUTTON_STYLE_NAME = STYLE_NAME + "-clear-button";
	public static final String LOOKUP_WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String LOOKUP_WINDOW_CONTENT_STYLE_NAME = LOOKUP_WINDOW_STYLE_NAME + "-content";
	private static final String SELECT_PROPERTY_ID = "select";
	private static final String CAPTION_PROPERTY_ID = "caption";

	protected TextInputDataProvider<T> suggestInputDataProvider;
	private List<LookupTreeDataProvider<T>> lookupTreeDataProviders = new ArrayList<>();
	private BaseAutocompleteField<T> autoCompleteField;
	private WindowButton lookupWindowButton;
	private Button clearButton;
	private ConverterWithCache<String, T> itemConverter;
	private ConverterWithCache<String, String> niceTitleItemConverter;
	private ConstellioEIMConfigs configs;
	private String readOnlyMessageI18NKey = null;

	protected boolean isShowDeactivated = true;

	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

	/**
	 * The component should receive focus (if {@link Focusable}) when attached.
	 */
	private boolean delayedFocus;
	private Integer windowZIndex;
	private boolean multiValue;
	private boolean itemInformation = false;

	protected static final Resource INFORMATION_ICON = new ThemeResource("images/commun/information.png");

	private ValueChangeListener autoCompleteChangeListener;

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

	@SuppressWarnings("unchecked")
	public void setProviders(TextInputDataProvider<T> suggestInputDataProvider,
							 LookupTreeDataProvider<T>... lookupTreeDataProviders) {
		this.suggestInputDataProvider = suggestInputDataProvider;
		if (lookupTreeDataProviders != null) {
			this.lookupTreeDataProviders.removeAll(this.lookupTreeDataProviders);
			for (LookupTreeDataProvider<T> lookupTreeDataProvider : lookupTreeDataProviders) {
				this.lookupTreeDataProviders.add(lookupTreeDataProvider);
			}
		}
	}

	public ConverterWithCache getNiceTitleItemConverter() {
		return new ConverterWithCache(new RecordIdToDescriptionContent());
	}

	public final Integer getWindowZIndex() {
		return windowZIndex;
	}

	public final void setWindowZIndex(Integer windowZIndex) {
		this.windowZIndex = windowZIndex;
	}

	public int getTreeBufferSize() {
		if (configs == null) {
			configs = ConstellioFactories.getInstance().getModelLayerFactory().getSystemConfigs();
		}
		return configs.getLazyTreeBufferSize();
	}

	public List<LookupTreeDataProvider<T>> getLookupTreeDataProviders() {
		return lookupTreeDataProviders;
	}

	public String getNiceTitle(Object property) {

		String recordId = null;

		if (this.niceTitleItemConverter == null) {
			this.niceTitleItemConverter = getNiceTitleItemConverter();
		}

		if (property != null && property instanceof String) {
			recordId = (String) property;
		}

		if (niceTitleItemConverter != null && recordId != null) {
			Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
			return niceTitleItemConverter.convertToPresentation(recordId, String.class, locale);
		}

		return "";
	}

	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	public boolean isItemInformation() {
		return itemInformation;
	}

	public void setItemInformation(boolean itemInformation) {
		this.itemInformation = itemInformation;
	}

	@Override
	public Object getValue() {
		Object value;
		if (multiValue) {
			Property<?> dataSource = getPropertyDataSource();
			if (dataSource != null) {
				Object dataSourceValue = dataSource.getValue();
				if (dataSourceValue != null && !(dataSourceValue instanceof List)) {
					value = ensureList(dataSourceValue);
				} else {
					value = getInternalValue();
				}
			} else {
				value = getInternalValue();
			}
		} else {
			value = super.getValue();
		}
		return value;
	}

	@Override
	protected Object getInternalValue() {
		Object internalValue;
		Object superInternalValue = super.getInternalValue();
		if (multiValue && superInternalValue != null && !(superInternalValue instanceof List)) {
			internalValue = ensureList(superInternalValue);
		} else {
			internalValue = superInternalValue;
		}
		return internalValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getConvertedValue() {
		Object convertedValue;
		if (!multiValue) {
			convertedValue = super.getConvertedValue();
		} else {
			Locale locale = getLocale();
			List<Object> listValue = (List<Object>) getValue();
			if (listValue != null) {
				List<Object> convertedListValue = new ArrayList<>();
				for (Object listItem : listValue) {
					Class<?> modelType = getModelType();
					try {
						Object convertedListItem = ConverterUtil.convertToModel(listItem,
								(Class<Object>) modelType, getConverter(), locale);
						convertedListValue.add(convertedListItem);
					} catch (ConversionException e) {
						throw new ConversionException(getConversionError(modelType, e), e);
					}
				}
				convertedValue = convertedListValue;
			} else {
				convertedValue = null;
			}
		}
		return convertedValue;
	}

	@Override
	protected void setInternalValue(Object newValue) {
		super.setInternalValue(newValue);
		if (!multiValue && autoCompleteField != null) {
			autoCompleteField.removeValueChangeListener(autoCompleteChangeListener);
			autoCompleteField.setValue(newValue);
			autoCompleteField.addValueChangeListener(autoCompleteChangeListener);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Object> ensureList(Object value) {
		List<Object> listValue;
		if (value instanceof List) {
			listValue = (List<Object>) value;
		} else {
			listValue = new ArrayList<>();
			if (value != null) {
				listValue.add(value);
			}
		}
		return listValue;
	}

	@Override
	protected Component initContent() {
		addStyleName(STYLE_NAME);
		setSizeFull();

		AutocompleteSuggestionsProvider<T> suggestionsProvider = new AutocompleteSuggestionsProvider<T>() {
			@Override
			public List<T> suggest(String text) {
				List<T> values = new ArrayList<>(suggestInputDataProvider.getData(text, 0, getBufferSize()));
				//				if (itemConverter != null) {
				//					Collections.sort(values, new Comparator<T>() {
				//						@Override
				//						public int compare(T o1, T o2) {
				//							String s1 = itemConverter.convertToPresentation(o1, String.class, getLocale());
				//							String s2 = itemConverter.convertToPresentation(o2, String.class, getLocale());
				//							return s1.compareTo(s2);
				//						}
				//					});
				//				}
				return values;
			}


			@Override
			public int getBufferSize() {
				ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
				ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
				return modelLayerFactory.getSystemConfigs().getAutocompleteSize();
			}
		};

		autoCompleteChangeListener = new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				Object newValue = event.getProperty().getValue();
				if (multiValue && newValue != null) {
					LookupField.this.setValue(ensureList(newValue));
				} else {
					LookupField.this.setValue(newValue);
				}
			}
		};

		autoCompleteField = newAutocompleteField(suggestionsProvider);
		autoCompleteField.addStyleName(AUTOCOMPLETE_FIELD_STYLE_NAME);
		autoCompleteField.setItemConverter(itemConverter);
		if (!multiValue) {
			autoCompleteField.setValue(super.getValue());
		}
		autoCompleteField.addValueChangeListener(autoCompleteChangeListener);

		createLookupWindowButton();

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
				LookupField.this.setValue(null);
			}
		});
		clearButton.setVisible(isClearButtonVisible());

		I18NHorizontalLayout mainLayout = new I18NHorizontalLayout(autoCompleteField, lookupWindowButton, clearButton);
		mainLayout.setExpandRatio(autoCompleteField, 1);
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		return mainLayout;
	}

	protected Component initItemInformationContent(T item) {
		return new Label("" + item);
	}

	@Override
	public void commit() throws SourceException, InvalidValueException {
		super.commit();
		if (multiValue && autoCompleteField != null) {
			autoCompleteField.removeValueChangeListener(autoCompleteChangeListener);
			autoCompleteField.setValue(null);
			autoCompleteField.addValueChangeListener(autoCompleteChangeListener);
		}
	}

	@NotNull
	public WindowButton createLookupWindowButton() {
		lookupWindowButton = new WindowButton(null, $("search")) {
			@Override
			protected BaseWindow newWindow(String windowCaption) {
				return new LookupWindow(windowCaption);
			}

			@Override
			protected Component buildWindowContent() {
				return new LookupWindowContent(getWindow());
			}

			@Override
			protected boolean acceptWindowOpen(ClickEvent event) {
				if (LookupField.this.isReadOnly()) {
					showReadOnlyMessage();
					return false;
				} else {
					return super.acceptWindowOpen(event);
				}
			}
		};
		lookupWindowButton.setIcon(new ThemeResource("images/icons/actions/view.png"));
		lookupWindowButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		lookupWindowButton.addStyleName(OPEN_WINDOW_BUTTON_STYLE_NAME);
		lookupWindowButton.setZIndex(windowZIndex);
		return lookupWindowButton;
	}

	public LookupField<T> setReadOnlyMessageI18NKey(String readOnlyMessageI18NKey) {
		this.readOnlyMessageI18NKey = readOnlyMessageI18NKey;
		return this;
	}

	protected String getReadOnlyMessage() {
		if (!StringUtils.isBlank(readOnlyMessageI18NKey)) {
			return $(readOnlyMessageI18NKey);
		}
		return $("readOnlyComponent");
	}

	private void showReadOnlyMessage() {
		Notification notification = new Notification(getReadOnlyMessage(), Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	protected BaseAutocompleteField<T> newAutocompleteField(AutocompleteSuggestionsProvider<T> suggestionsProvider) {
		return new BaseAutocompleteField<>(suggestionsProvider);
	}

	protected Component newItemCaptionAndInfoLayout(final T object, Component itemCaptionComponent) {
		Image informationImage = new Image(null, INFORMATION_ICON);
		informationImage.addStyleName("lookup-information-icon");
		informationImage.addClickListener(new MouseEvents.ClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void click(MouseEvents.ClickEvent event) {
				LookupWindowContent lookupWindowContent = (LookupWindowContent) lookupWindowButton.getWindow().getContent();
				lookupWindowContent.loadInformation(object);
			}
		});

		// Not multiValue
		if (itemCaptionComponent == null) {
			Button selectButton = new BaseButton(getCaption(object)) {
				@Override
				protected void buttonClick(ClickEvent event) {
					setValue(object);
				}
			};
			selectButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectButton.addStyleName(ValoTheme.BUTTON_QUIET);
			itemCaptionComponent = selectButton;
		}
		I18NHorizontalLayout itemCaptionAndInfoLayout = new I18NHorizontalLayout(itemCaptionComponent, informationImage);
		itemCaptionAndInfoLayout.setComponentAlignment(informationImage, Alignment.MIDDLE_LEFT);
		itemCaptionAndInfoLayout.setSpacing(true);
		return itemCaptionAndInfoLayout;
	}

	protected LazyTree<T> newLazyTree(final LookupTreeDataProvider<T> lookupTreeDataProvider, int treeBufferSize,
									  boolean multiValue) {
		return new LazyTree<T>(lookupTreeDataProvider, treeBufferSize, multiValue) {
			@Override
			public String getItemCaption(T itemId) {
				return LookupField.this.getCaption(itemId);
			}

			@Override
			protected boolean isSelectable(T object) {
				return lookupTreeDataProvider.isSelectable(object);
			}

			@Override
			protected Component getItemCaptionComponent(T object) {
				Component itemCaptionComponent;
				Component superItemCaptionComponent = super.getItemCaptionComponent(object);
				if (isItemInformation() && isSelectable(object)) {
					itemCaptionComponent = newItemCaptionAndInfoLayout(object, superItemCaptionComponent);
				} else {
					itemCaptionComponent = superItemCaptionComponent;
				}
				return itemCaptionComponent;
			}
		};
	}

	public Converter<String, T> getItemConverter() {
		return itemConverter;
	}

	public void setItemConverter(Converter<String, T> itemConverter) {
		if (itemConverter != null && suggestInputDataProvider != null) {
			this.itemConverter = new ConverterWithCache<>(itemConverter);
			suggestInputDataProvider.setConverterWithCache(this.itemConverter);
		}
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
			if (!multiValue) {
				super.validate();
			} else {
				if (isRequired() && isEmpty()) {
					throw new Validator.EmptyValueException(getRequiredError());
				}
				validate(getValue());
			}
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

	public BaseAutocompleteField<T> getAutoCompleteField() {
		return autoCompleteField;
	}

	public List<SelectionChangeListener> getSelectionChangeListeners() {
		return Collections.unmodifiableList(selectionChangeListeners);
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		if (!selectionChangeListeners.contains(listener)) {
			selectionChangeListeners.add(listener);
		}
	}

	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		selectionChangeListeners.remove(listener);
	}

	public class LookupWindow extends BaseWindow {

		public LookupWindow() {
			super();
		}

		public LookupWindow(String caption, Component content) {
			super(caption, content);
		}

		public LookupWindow(String caption) {
			super(caption);
		}

	}

	protected class LookupWindowContent extends VerticalLayout {

		private I18NHorizontalLayout searchFieldLayout;

		private TextField searchField;

		private Button searchButton;

		private Button selectButton;

		private I18NHorizontalLayout itemsAndInformationLayout;

		private VerticalLayout informationLayout;

		private Button closeInformationButton;

		private Component currentInformationComponent;

		private Component lookupTreeComponent;

		private Table searchResultsTable;

		@SuppressWarnings("unchecked")
		public LookupWindowContent(Window window) {
			super();

			setSizeFull();
			setHeight("95%");

			window.setWidth("80%");
			window.setHeight("80%");
			window.setResizable(true);
			window.addStyleName(LOOKUP_WINDOW_STYLE_NAME);

			addStyleName(LOOKUP_WINDOW_CONTENT_STYLE_NAME);
			setSpacing(true);

			searchFieldLayout = new I18NHorizontalLayout();
			//			searchFieldLayout.setWidthUndefined();
			searchFieldLayout.setSpacing(true);

			searchField = new BaseTextField();
			searchField.setWidth("100%");
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

			selectButton = new Button($("select"));
			selectButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					List<Object> newSelection;
					if (lookupTreeComponent != null) {
						newSelection = (List<Object>) getCurrentLazyTree().getValue();
						LookupField.this.setValue(newSelection);
					} else {
						newSelection = null;
					}
					for (SelectionChangeListener selectionChangeListener : selectionChangeListeners) {
						selectionChangeListener.selectionChanged(newSelection);
					}
					if (lookupWindowButton.getWindow() != null) {
						Window lookupWindow = lookupWindowButton.getWindow();
						lookupWindow.close();
					}
				}
			});

			if (!getLookupTreeDataProviders().isEmpty()) {
				if (getLookupTreeDataProviders().size() > 1) {
					lookupTreeComponent = new TabSheet();
				}
				for (final LookupTreeDataProvider<T> lookupTreeDataProvider : getLookupTreeDataProviders()) {
					LazyTree<T> lazyTree = newLazyTree(lookupTreeDataProvider, getTreeBufferSize(), multiValue);
					lazyTree.setWidth("100%");
					lazyTree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
					lazyTree.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
					if (!multiValue) {
						lazyTree.addItemClickListener(new ItemClickListener() {
							@Override
							public void itemClick(ItemClickEvent event) {
								T objectClicked = (T) event.getItemId();
								if (lookupTreeDataProvider.isSelectable(objectClicked)) {
									if (LookupField.this.getValue() != null && LookupField.this.getValue().equals(objectClicked)) {
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
					}

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

			searchResultsTable = new BaseTable(UUID.randomUUID().toString());
			searchResultsTable.setWidth("100%");
			searchResultsTable.setHeight("98%");
			searchResultsTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

			if (itemInformation) {
				itemsAndInformationLayout = new I18NHorizontalLayout();
				itemsAndInformationLayout.setSpacing(true);
				itemsAndInformationLayout.setSizeFull();

				informationLayout = new VerticalLayout();
				informationLayout.setSpacing(true);

				closeInformationButton = new BaseButton($("close")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						itemsAndInformationLayout.removeComponent(informationLayout);
					}
				};
				closeInformationButton.setHeight("40px");

				informationLayout.addComponents(closeInformationButton);
				informationLayout.setComponentAlignment(closeInformationButton, Alignment.TOP_RIGHT);
			}

			addComponent(searchFieldLayout);
			if (isItemInformation()) {
				addComponent(itemsAndInformationLayout);
			}
			if (!getLookupTreeDataProviders().isEmpty()) {
				if (isItemInformation()) {
					itemsAndInformationLayout.addComponent(lookupTreeComponent);
				} else {
					addComponent(lookupTreeComponent);
				}
				selectButton.setVisible(multiValue);
			} else {
				Container searchResultsContainer = new LookupSearchResultContainer(geSuggestInputDataProvider(), searchField);
				searchResultsTable.setContainerDataSource(searchResultsContainer);
				if (isItemInformation()) {
					itemsAndInformationLayout.addComponent(searchResultsTable);
				} else {
					addComponent(searchResultsTable);
				}
				selectButton.setVisible(false);
			}

			searchFieldLayout.setWidth("100%");
			searchFieldLayout.addComponents(searchField, searchButton, selectButton);
			searchFieldLayout.setComponentAlignment(selectButton, Alignment.TOP_RIGHT);

			if (!getLookupTreeDataProviders().isEmpty()) {
				if (itemInformation) {
					setExpandRatio(itemsAndInformationLayout, 1);
				} else {
					setExpandRatio(lookupTreeComponent, 1);
				}
			} else {
				if (itemInformation) {
					setExpandRatio(itemsAndInformationLayout, 1);
				} else {
					setExpandRatio(searchResultsTable, 1);
				}
			}
			searchFieldLayout.setExpandRatio(searchField, 1);
		}

		public void loadInformation(T object) {
			itemsAndInformationLayout.removeComponent(informationLayout);

			if (currentInformationComponent != null) {
				informationLayout.removeComponent(currentInformationComponent);
			}
			currentInformationComponent = initItemInformationContent(object);
			currentInformationComponent.setWidth("200px");
			informationLayout.addComponent(currentInformationComponent);
			itemsAndInformationLayout.addComponent(informationLayout);
		}

		@SuppressWarnings("unchecked")
		private LazyTree<T> getCurrentLazyTree() {
			Panel currentLazyTreePanel;
			if (lookupTreeComponent instanceof TabSheet) {
				TabSheet tabSheet = (TabSheet) lookupTreeComponent;
				currentLazyTreePanel = (Panel) tabSheet.getSelectedTab();
			} else {
				currentLazyTreePanel = (Panel) lookupTreeComponent;
			}
			return (LazyTree<T>) currentLazyTreePanel.getContent();
		}

		private LookupTreeDataProvider<T> getCurrentTreeDataProvider() {
			LazyTree<T> currentLazyTree = getCurrentLazyTree();
			return (LookupTreeDataProvider<T>) currentLazyTree.getDataProvider();
		}

		public List<LookupTreeDataProvider<T>> getLookupTreeDataProviders() {
			return lookupTreeDataProviders;
		}

		@SuppressWarnings("unchecked")
		private void search() {
			String text = searchField.getValue();
			if (lookupTreeComponent != null) {
				if (StringUtils.isNotBlank(text)) {
					if (lookupTreeComponent.isVisible()) {
						lookupTreeComponent.setVisible(false);
						searchResultsTable.setVisible(true);
						selectButton.setVisible(false);
					}
					LookupTreeDataProvider<T> currentDataProvider = getCurrentTreeDataProvider();
					Container searchResultsContainer;
					if (isShowDeactivated) {
						searchResultsContainer = new LookupSearchResultContainer(currentDataProvider.search(), searchField);
					} else {
						searchResultsContainer = new LookupSearchResultContainer(currentDataProvider.searchWithoutDisabled(),
								searchField);
					}

					searchResultsTable.setContainerDataSource(searchResultsContainer);
					if (itemInformation) {
						itemsAndInformationLayout.replaceComponent(lookupTreeComponent, searchResultsTable);
						setExpandRatio(itemsAndInformationLayout, 1);
					} else {
						replaceComponent(lookupTreeComponent, searchResultsTable);
						setExpandRatio(searchResultsTable, 1);
					}
				} else {
					lookupTreeComponent.setVisible(true);
					searchResultsTable.setVisible(false);
					if (itemInformation) {
						itemsAndInformationLayout.replaceComponent(searchResultsTable, lookupTreeComponent);
						setExpandRatio(itemsAndInformationLayout, 1);
					} else {
						replaceComponent(searchResultsTable, lookupTreeComponent);
						setExpandRatio(lookupTreeComponent, 1);
					}
					selectButton.setVisible(multiValue);
				}
			} else {
				Container searchResultsContainer = new LookupSearchResultContainer(geSuggestInputDataProvider(), searchField);
				searchResultsTable.setContainerDataSource(searchResultsContainer);
			}
		}

		@SuppressWarnings("rawtypes")
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
			if (taxonomyCode != null && taxonomyCode.equals(userDefaultTaxonomy)) {
				tabSheet.setSelectedTab(lazyTreePanel);
			}
		} else if (taxonomyCode != null && taxonomyCode.equals(configDefaultTaxonomy)) {
			tabSheet.setSelectedTab(lazyTreePanel);
		}
	}

	protected boolean isClearButtonVisible() {
		return true;
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

		@SuppressWarnings("rawtypes")
		public TextInputDataProvider setConverterWithCache(
				ConverterWithCache<String, T> converterWithCache) {
			return this;
		}
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

				ExtensibleButton selectButton = new ExtensibleButton(caption);

				String tooltip = getNiceTitle(item);
				if (tooltip != null) {
					NiceTitle niceTitle = new NiceTitle(tooltip);
					selectButton.addExtension(niceTitle);
				}
				selectButton.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if (LookupField.this.getValue() != null && LookupField.this.getValue().equals(item)) {
							if (lookupWindowButton.getWindow() != null) {
								Window lookupWindow = lookupWindowButton.getWindow();
								lookupWindow.close();
							}
						} else {
							if (!LookupField.this.isReadOnly()) {
								LookupField.this.setValue(item);
								if (multiValue) {
									for (SelectionChangeListener selectionChangeListener : selectionChangeListeners) {
										List<Object> objectList = new ArrayList<>();
										objectList.add(item);
										selectionChangeListener.selectionChanged(objectList);
									}
								}
							}
						}
					}
				});
				selectButton.addStyleName(ValoTheme.BUTTON_LINK);
				if (itemInformation) {
					Component buttonAndInfoComponent = newItemCaptionAndInfoLayout(item, selectButton);
					property = new ObjectProperty<Component>(buttonAndInfoComponent);
				} else {
					property = new ObjectProperty<Button>(selectButton);
				}
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

	private class ExtensibleButton extends Button {
		public ExtensibleButton(String caption) {
			super(caption);
		}

		@Override
		public void addExtension(Extension extension) {
			super.addExtension(extension);
		}
	}

	private interface SerializableQuery extends Query, Serializable {

	}

	public static interface SelectionChangeListener {

		void selectionChanged(List<Object> newSelection);

	}

}
