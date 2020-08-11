package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplay;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayRetentionRuleViewImpl extends BaseViewImpl implements DisplayRetentionRuleView {

	public static final String STYLE_NAME = "display-folder";

	private RetentionRuleVO retentionRuleVO;

	private VerticalLayout mainLayout;

	private RetentionRuleDisplay recordDisplay;

	private Button editButton, deleteButton;

	private DisplayRetentionRulePresenter presenter;

	public DisplayRetentionRuleViewImpl() {
		presenter = new DisplayRetentionRulePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setRetentionRule(RetentionRuleVO retentionRuleVO) {
		this.retentionRuleVO = retentionRuleVO;
	}

	@Override
	protected String getTitle() {
		return $("DisplayRetentionRuleView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		recordDisplay = new RetentionRuleDisplay(presenter, retentionRuleVO, getSessionContext().getCurrentLocale());
		recordDisplay.setWidth("100%");

		mainLayout.addComponent(recordDisplay);

		Component component = buildAdditionalComponent();
		mainLayout.addComponent(component);

		return mainLayout;
	}

	private Component buildAdditionalComponent() {
		Label foldersNumberCaptionLabel = new Label($("DisplayRetentionRuleView.foldersNumber"));
		foldersNumberCaptionLabel.setId("foldersNumber");
		foldersNumberCaptionLabel.addStyleName("foldersNumber");
		Label foldersNumberDisplayComponent = new Label(presenter.getFoldersNumber());
		foldersNumberDisplayComponent.addStyleName("display-value-foldersNumber");

		List<CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(foldersNumberCaptionLabel, foldersNumberDisplayComponent));
		return new BaseDisplay(captionsAndComponents);
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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = super.buildActionMenuButtons(event);

		if (presenter.isManageRetentionRulesOnSomething()) {
			editButton = new EditButton(false) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.editButtonClicked();
				}
			};

			deleteButton = new DeleteButton(false) {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					presenter.deleteButtonClicked();
				}
			};
			deleteButton.setEnabled(presenter.validateDeletable(retentionRuleVO).isEmpty());

			actionMenuButtons.add(editButton);
			actionMenuButtons.add(deleteButton);
		}

		return actionMenuButtons;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Arrays.asList(new IntermediateBreadCrumbTailItem() {
					@Override
					public String getTitle() {
						return $("ListRetentionRulesView.viewTitle");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to(RMViews.class).listRetentionRules();
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				});
			}
		};
	}

}
