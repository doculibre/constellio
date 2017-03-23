package com.constellio.app.ui.pages.management.schemas.type;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.SchemaVOLazyContainer;
import com.constellio.app.ui.framework.data.SchemaVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListSchemaViewImpl extends BaseViewImpl implements ListSchemaView, ClickListener {
	ListSchemaPresenter presenter;

	public ListSchemaViewImpl() {
		this.presenter = new ListSchemaPresenter(this);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return this;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		presenter.backButtonClicked();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		String parameters = event.getParameters();
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		presenter.setSchemaTypeCode(paramsMap.get("schemaTypeCode"));
		presenter.setParameters(paramsMap);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		viewLayout.addComponents(addButton, buildTables());
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private Component buildTables() {
		final SchemaVODataProvider dataProvider = presenter.getDataProvider();
		SchemaVOLazyContainer schemaContainer = new SchemaVOLazyContainer(dataProvider);
		ButtonsContainer<SchemaVOLazyContainer> buttonsContainer = new ButtonsContainer<>(schemaContainer, "buttons");

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.editButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new MetadataButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.editMetadataButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new FormOrderButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.orderButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new FormDisplay() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.formButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				TableDisplayButton tableDisplayButton = new TableDisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.tableButtonClicked();
					}

					@Override
					public boolean isVisible() {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						if(entity == null || entity.getCode() == null || !entity.getCode().endsWith("default")) {
							return false;
						}
						return true;
					}
				};
				return tableDisplayButton;
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new SearchDisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.searchButtonClicked(entity);
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
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.deleteButtonClicked(entity.getCode());
					}

					@Override
					public boolean isVisible() {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						return super.isVisible() && presenter.isDeleteButtonVisible(entity.getCode());
					}
				};
			}
		});

		Table table = new Table($("ListSchemaView.tableTitle", schemaContainer.size()), buttonsContainer);
		table.setSizeFull();
		table.setPageLength(Math.min(15, schemaContainer.size()));
		table.setColumnHeader("buttons", "");
		table.setColumnHeader("caption", $("ListSchemaView.caption", schemaContainer.size()));
		table.setColumnExpandRatio("caption", 1);
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Integer index = (Integer) event.getItemId();
				MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
				presenter.editButtonClicked(entity);
			}
		});

		return table;
	}

}
