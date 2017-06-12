package com.constellio.app.ui.pages.management.extractors;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;
import java.util.Locale;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListMetadataExtractorsViewImpl extends BaseViewImpl implements ListMetadataExtractorsView {

	private static final String SCHEMA_PROPERTY = "schema";

	private static final String METADATA_PROPERTY = "metadata";

	private static final String STYLES_PROPERTY = "styles";

	private static final String PROPERTIES_PROPERTY = "properties";

	private static final String REGEXES_PROPERTY = "regexes";

	private List<MetadataExtractorVO> metadataExtractorVOs;

	private VerticalLayout mainLayout;

	private Button addButton;

	private Table table;

	private ListMetadataExtractorsPresenter presenter;

	public ListMetadataExtractorsViewImpl() {
		presenter = new ListMetadataExtractorsPresenter(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		addButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		table = new Table($("ListMetadataExtractorsView.table.title", metadataExtractorVOs.size()));
		table.setWidth("100%");
		table.setPageLength(Math.min(15, metadataExtractorVOs.size()));

		table.addContainerProperty(SCHEMA_PROPERTY, String.class, null);
		table.addContainerProperty(METADATA_PROPERTY, String.class, null);
		table.addContainerProperty(STYLES_PROPERTY, Label.class, null);
		table.addContainerProperty(PROPERTIES_PROPERTY, Label.class, null);
		table.addContainerProperty(REGEXES_PROPERTY, Label.class, null);

		table.setColumnHeader(SCHEMA_PROPERTY, $("ListMetadataExtractorsView.table.schema"));
		table.setColumnHeader(METADATA_PROPERTY, $("ListMetadataExtractorsView.table.metadata"));
		table.setColumnHeader(STYLES_PROPERTY, $("ListMetadataExtractorsView.table.styles"));
		table.setColumnHeader(PROPERTIES_PROPERTY, $("ListMetadataExtractorsView.table.properties"));
		table.setColumnHeader(REGEXES_PROPERTY, $("ListMetadataExtractorsView.table.regexes"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnExpandRatio(METADATA_PROPERTY, 1);

		ButtonsContainer<IndexedContainer> buttonsContainer = new ButtonsContainer<>(
				(IndexedContainer) table.getContainerDataSource());
		table.setContainerDataSource(buttonsContainer);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						MetadataExtractorVO metadataExtractorVO = (MetadataExtractorVO) itemId;
						presenter.editButtonClicked(metadataExtractorVO);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						MetadataExtractorVO metadataExtractorVO = (MetadataExtractorVO) itemId;
						presenter.deleteButtonClicked(metadataExtractorVO);
					}
				};
			}
		});

		for (MetadataExtractorVO metadataExtractorVO : metadataExtractorVOs) {
			MetadataVO metadataVO = metadataExtractorVO.getMetadataVO();

			Locale locale = getLocale();
			String schemaLabel = metadataVO.getSchema().getLabel(locale);
			String metadataLabel = metadataVO.getLabel(locale);
			StringBuffer stylesSB = new StringBuffer();
			for (String style : metadataExtractorVO.getStyles()) {
				if (stylesSB.length() > 0) {
					stylesSB.append("<br />");
				}
				stylesSB.append(style);
			}
			StringBuffer propertiesSB = new StringBuffer();
			for (String property : metadataExtractorVO.getProperties()) {
				if (propertiesSB.length() > 0) {
					propertiesSB.append("<br />");
				}
				propertiesSB.append(property);
			}
			StringBuffer regexSB = new StringBuffer();
			for (RegexConfigVO regexConfigVO : metadataExtractorVO.getRegexes()) {
				if (regexSB.length() > 0) {
					regexSB.append("<br />");
				}
				regexSB.append(regexConfigVO.getInputMetadata() + " - ");
				regexSB.append(regexConfigVO.getRegex() + " - ");
				regexSB.append($("RegexConfigField.RegexConfigType." + regexConfigVO.getRegexConfigType()) + " - ");
				regexSB.append(regexConfigVO.getValue());
			}

			Item item = table.addItem(metadataExtractorVO);
			item.getItemProperty(SCHEMA_PROPERTY).setValue(schemaLabel);
			item.getItemProperty(METADATA_PROPERTY).setValue(metadataLabel);
			item.getItemProperty(STYLES_PROPERTY).setValue(new Label(stylesSB.toString(), ContentMode.HTML));
			item.getItemProperty(PROPERTIES_PROPERTY).setValue(new Label(propertiesSB.toString(), ContentMode.HTML));
			item.getItemProperty(REGEXES_PROPERTY).setValue(new Label(regexSB.toString(), ContentMode.HTML));
		}

		mainLayout.addComponents(addButton, table);
		mainLayout.setExpandRatio(table, 1);
		mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

		return mainLayout;
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

	@Override
	protected String getTitle() {
		return $("ListMetadataExtractorsView.viewTitle");
	}

	@Override
	public void setMetadataExtractorVOs(List<MetadataExtractorVO> metadataExtractorVOs) {
		this.metadataExtractorVOs = metadataExtractorVOs;
	}

	@Override
	public void removeMetadataExtractorVO(MetadataExtractorVO metadataExtractorVO) {
		table.removeItem(metadataExtractorVO);
		metadataExtractorVOs.remove(metadataExtractorVO);
		table.setCaption($("ListMetadataExtractorsView.table.title", metadataExtractorVOs.size()));
	}

}
