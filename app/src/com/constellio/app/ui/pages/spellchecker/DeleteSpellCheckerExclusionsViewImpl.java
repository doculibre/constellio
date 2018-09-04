package com.constellio.app.ui.pages.spellchecker;

import com.constellio.app.ui.entities.ExclusionCollectionVO;
import com.constellio.app.ui.framework.buttons.EnableButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DeleteSpellCheckerExclusionsViewImpl extends BaseViewImpl implements DeleteSpellCheckerExclusionsView {

	BaseTable exclusionsTable;
	ButtonsContainer<?> buttonsContainer;
	IndexedContainer indexedContainer;

	public static final String SPACE_4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
	public static final String SPACES_8 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	public static final String INFORMATION = "information";

	Map<Integer, Object> containerMapperWithElevationObject = new HashMap<>();
	DeleteSpellCheckerExclusionsPresenter presenter;

	public DeleteSpellCheckerExclusionsViewImpl() {
		this.presenter = new DeleteSpellCheckerExclusionsPresenter(this);
	}

	@Override
	public String getTitle() {
		return $("DeleteSpellCheckerExclusionsView.title");
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	protected boolean hasPageAccess(String params, User user) {
		return false;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		Label infoLabel = new Label($("DeleteSpellCheckerExclusionsView.info"));

		indexedContainer = new IndexedContainer();
		buttonsContainer = new ButtonsContainer<>(indexedContainer);

		indexedContainer.addContainerProperty(INFORMATION, Label.class, null);

		exclusionsTable = new BaseTable(DeleteSpellCheckerExclusionsViewImpl.class.getName());

		exclusionsTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		exclusionsTable.setColumnHeader(INFORMATION, $("DeleteSpellCheckerExclusionsView.excluded"));

		exclusionsTable.setContainerDataSource(buttonsContainer);

		exclusionsTable.setSizeFull();
		exclusionsTable.setSortEnabled(false);

		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button restoreButton = new EnableButton($("DeleteSpellCheckerExclusionsView.restore")) {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						ExclusionCollectionVO exclusion = (ExclusionCollectionVO) containerMapperWithElevationObject.get(itemId);
						presenter.deleteExclusionButtonClicked(exclusion);
					}
				};
				return restoreButton;
			}
		});

		for (ExclusionCollectionVO exclusionCollectionVO : presenter.getExcluded()) {
			exclusionsTable.setColumnExpandRatio(INFORMATION, 1);
			exclusionsTable.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 60);

			addExclusionTo(exclusionCollectionVO);
		}


		mainLayout.addComponents(infoLabel, exclusionsTable);
		mainLayout.setExpandRatio(exclusionsTable, 1);

		return mainLayout;
	}

	private void addExclusionTo(ExclusionCollectionVO exclusion) {
		Object addedItemNumber = exclusionsTable.addItem();

		containerMapperWithElevationObject.put((Integer) addedItemNumber, exclusion);
		Label label = new Label(exclusion.getExclusion());
		label.setContentMode(ContentMode.HTML);
		indexedContainer.getContainerProperty(addedItemNumber, INFORMATION).setValue(label);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
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
}
