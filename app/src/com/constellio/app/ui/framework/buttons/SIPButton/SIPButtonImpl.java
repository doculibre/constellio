package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.pages.SIP.BagInfoSIPForm;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SIPButtonImpl extends WindowButton {

	private List<RecordVO> objectList = new ArrayList<>();

	private ConstellioHeader view;
	private SIPButtonPresenter presenter;
	private boolean showDeleteButton;

	public SIPButtonImpl(String caption, String windowCaption, ConstellioHeader view, boolean showDeleteButton) {
		super(FontAwesome.FILE_ARCHIVE_O, caption, windowCaption, false, new WindowConfiguration(true, true, "75%", "75%"));
		this.view = view;
		this.presenter = new SIPButtonPresenter(this, objectList, view.getSessionContext().getCurrentLocale());
		this.showDeleteButton = showDeleteButton;
	}

	@Override
	protected Component buildWindowContent() {
		return new BagInfoSIPForm(showDeleteButton) {
			@Override
			protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {
				presenter.saveButtonClick(viewObject);
			}
		};
	}

	protected void showMessage(String value) {
		this.view.getCurrentView().showMessage(value);
	}

	protected void closeAllWindows() {
		this.view.getCurrentView().closeAllWindows();
	}

	public void showErrorMessage(String value) {
		this.view.getCurrentView().showErrorMessage(value);
	}

	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
	}

	public ConstellioHeader getView() {
		return view;
	}

	public void addAllObject(RecordVO... objects) {
		objectList.addAll(asList(objects));
	}

	public void setAllObject(RecordVO... objects) {
		objectList.clear();
		objectList.addAll(asList(objects));
	}
}
