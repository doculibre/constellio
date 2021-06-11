package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.SolrFeatureLazyContainer;
import com.constellio.app.ui.framework.data.SolrFeatureDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.vaadin.data.Container.Filterable;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.ltr.feature.SolrFeature;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class SolrFeatureViewImpl extends BaseViewImpl implements SolrFeatureView {
	private SolrFeaturePresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;

	private Table table;

	public SolrFeatureViewImpl() {
		this.presenter = new SolrFeaturePresenter(this);
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected String getTitle() {
		return $("SolrFeatureView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		table = buildTable();

		viewLayout.addComponents(table);
		viewLayout.setExpandRatio(table, 1);

		return viewLayout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	Table buildTable() {
		final SolrFeatureDataProvider dataProvider = presenter.newDataProvider();

		List<SolrFeature> features = dataProvider.listFeatures();
		dataProvider.setFeatures(features);

		Filterable tableContainer = new SolrFeatureLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(tableContainer, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		tableContainer = buttonsContainer;

		Table table = new Table($("SolrFeatureView.viewTitle"), tableContainer);
		table.setPageLength(Math.min(15, dataProvider.size()));
		table.setWidth("100%");
		table.setColumnHeader("name", $("SolrFeatureView.nameColumn"));
		table.setColumnHeader("query", $("SolrFeatureView.queryColumn"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	private void addButtons(final SolrFeatureDataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button button = buildAddEditForm(presenter.getFeature((Integer) itemId, provider));
				button.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				button.addStyleName(EditButton.BUTTON_STYLE);
				button.setIcon(EditButton.ICON_RESOURCE);
				return button;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						SolrFeature feature = presenter.getFeature((Integer) itemId, provider);
						presenter.deleteButtonClicked(feature);

					}
				};
			}
		});
	}

	public void refreshTable() {
		Table newTable = buildTable();
		viewLayout.replaceComponent(table, newTable);
		table = newTable;
	}

	@Override
	public List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> result = super.buildActionMenuButtons(event);

		Button add = buildAddEditForm(null);
		add.setCaption($("SolrFeatureView.add"));

		result.add(add);
		return result;
	}

	public Button buildAddEditForm(final SolrFeature feature) {
		return new WindowButton("",
				$("SolrFeatureView.addEdit")) {
			@Override
			protected Component buildWindowContent() {

				final TextField labelField = new BaseTextField($("SolrFeatureView.labelField"));
				labelField.setRequired(true);
				labelField.setId("labelField");
				labelField.addStyleName("labelField");
				labelField.setWidth("100%");
				if (feature != null) {
					labelField.setValue(feature.getName());
				}

				final ComboBox queryTypeField = new ComboBox($("SolrFeatureView.queryTypeField"));
				queryTypeField.addItem("q");
				queryTypeField.addItem("fq");
				queryTypeField.setRequired(true);
				queryTypeField.setId("queryTypeField");
				queryTypeField.addStyleName("queryTypeField");
				queryTypeField.setWidth("100%");
				if (feature != null) {
					if (StringUtils.isNotBlank(feature.getQ())) {
						queryTypeField.setValue("q");
					} else if (!CollectionUtils.isEmpty(feature.getFq())) {
						queryTypeField.setValue("fq");
					}
				}

				final TextField queryField = new BaseTextField($("SolrFeatureView.queryField"));
				queryField.setRequired(true);
				queryField.setId("queryField");
				queryField.addStyleName("queryField");
				queryField.setWidth("90%");
				if (feature != null) {
					if (!CollectionUtils.isEmpty(feature.getFq())) {
						queryField.setValue(StringUtils.join(feature.getFq(), "; "));
					} else if (StringUtils.isNotBlank(feature.getQ())) {
						queryField.setValue(feature.getQ());
					}
				}

				BaseButton addButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if (StringUtils.isBlank(labelField.getValue())) {
							showErrorMessage($("SolrFeatureView.invalidName"));
							return;
						}

						if (StringUtils.isBlank((String) queryTypeField.getValue())) {
							// SolrFeature: Q or FQ must be provided
							showErrorMessage($("SolrFeatureView.invalidQueryType"));
							return;
						}

						if (StringUtils.isBlank(queryField.getValue())) {
							// SolrFeature: Q or FQ must be provided
							showErrorMessage($("SolrFeatureView.invalidQorFQ"));
							return;
						}

						SolrFeature newFeature = new SolrFeature(labelField.getValue(), null);

						if ("fq".equals(queryTypeField.getValue())) {
							Splitter splitter = Splitter.on(CharMatcher.anyOf(";")).trimResults().omitEmptyStrings();
							Iterable<String> split = splitter.split(StringUtils.trimToEmpty(queryField.getValue()));
							newFeature.setFq(Lists.newArrayList(split));
						} else if ("q".equals(queryTypeField.getValue())) {
							newFeature.setQ(queryField.getValue());
						}

						if (feature != null) {
							presenter.editButtonClicked(newFeature, feature);
						} else {


							MyTest mt = new MyTest();
							mt.setName(newFeature.getName());
							mt.setClazz(newFeature.getClass().getCanonicalName());
							mt.setQuery(newFeature.paramsToMap());

							String json = new GsonBuilder().create().toJson(mt);

							presenter.addButtonClicked(newFeature);
						}

						getWindow().close();
					}
				};
				addButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayoutButtons = new HorizontalLayout();
				horizontalLayoutButtons.setSpacing(true);
				horizontalLayoutButtons.addComponents(addButton, cancelButton);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(labelField, queryTypeField);

				//				HorizontalLayout horizontalLayoutQuery = new HorizontalLayout();
				//				horizontalLayoutQuery.setSpacing(true);
				//				horizontalLayoutQuery.addComponents(keyField);
				//				horizontalLayoutQuery.setExpandRatio(keyField, 1);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout
						.addComponents(horizontalLayout, queryField, horizontalLayoutButtons);
				verticalLayout.setExpandRatio(queryField, 1);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
	}

	public static class MyTest {
		@SerializedName("name")
		String name;
		@SerializedName("class")
		String clazz;
		@SerializedName("params")
		Map<String, Object> query = new HashMap<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getClazz() {
			return clazz;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

		public Map<String, Object> getQuery() {
			return query;
		}

		public void setQuery(Map<String, Object> query) {
			this.query = query;
		}
	}
}
