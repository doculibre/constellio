package com.constellio.app.ui.pages.progress;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class BaseProgressView extends BaseViewImpl {
	
	private boolean done;
	
	private ProgressBar progressBar;
	
	private Label statusLabel;
	
	private TextArea errorMessagesField;
	
	private ProgressInfo progressInfo;
	
	private List<String> lastErrorMessages = Collections.synchronizedList(new ArrayList<String>());

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return buildMainLayout();
	}
	
	protected VerticalLayout buildMainLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		
		progressBar = new ProgressBar();
		progressBar.addStyleName(ValoTheme.PROGRESSBAR_POINT);
		progressBar.setCaption(progressInfo.getTask());
		progressBar.setEnabled(true);
		progressBar.setWidth("400px");
		
		statusLabel = new Label();
		
		errorMessagesField = new TextArea();
		errorMessagesField.setWidth("100%");
		errorMessagesField.setCaption($("BaseProgressView.errors"));
		
		String errorMessages = "";
		for (String errorMessage : progressInfo.getErrorMessages()) {
			errorMessages += "\n" + errorMessage;
		}
		errorMessagesField.setValue(errorMessages);
		
		mainLayout.addComponents(statusLabel, progressBar, errorMessagesField);
		mainLayout.setExpandRatio(errorMessagesField, 1);
		return mainLayout;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Label getStatusLabel() {
		return statusLabel;
	}

	public TextArea getErrorMessagesField() {
		return errorMessagesField;
	}

	public ProgressInfo getProgressInfo() {
		return progressInfo;
	}

	public void setProgressInfo(ProgressInfo progressInfo) {
		this.progressInfo = progressInfo;
		
		progressInfo.getErrorMessages().addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) {
				int startIndex = e.getIndex0();
				int endIndex = e.getIndex1();
				@SuppressWarnings("unchecked")
				List<String> sourceList = (List<String>) e.getSource();
				List<String> sourceSubList = sourceList.subList(startIndex, endIndex + 1);
				lastErrorMessages.addAll(sourceSubList);
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) {
			}
		});
	}

	@Override
	protected boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		if (!done) {
			Float progress = progressInfo.getProgress();
			if (progress <= 1f) {
				String task = progressInfo.getTask();
				long currentState = progressInfo.getCurrentState();
				long end = progressInfo.getEnd();
				String progressStr = $("BaseProgressView.progress", currentState, end, progress * 100);
				
				progressBar.setValue(progress);
				progressBar.setCaption(progressStr);
				
				statusLabel.setValue(task);
				if (StringUtils.isNotBlank(task)) {
					if (!statusLabel.isVisible()) {
						statusLabel.setVisible(true);
					}
				} else {
					if (statusLabel.isVisible()) {
						statusLabel.setVisible(false);
					}
				}
				
				if (!lastErrorMessages.isEmpty()) {
					String errorMessages = errorMessagesField != null ? errorMessagesField.getValue() : "";
					List<String> newErrorMessages = new ArrayList<String>(lastErrorMessages);
					lastErrorMessages.clear();
					for (String newErrorMessage : newErrorMessages) {
						errorMessages = newErrorMessage +  "\n" + errorMessages;
					}
					errorMessagesField.setValue(errorMessages);
					errorMessagesField.focus();
				}
				if (progress == 1f) {
					done = true;
					notifyDone();
				}
			} else {
				done = true;
				notifyDone();
			}
		}
	}
	
	private void notifyDone() {
		new Thread() {
			@Override
			public void run() {
				onDone();
			}
		}.start();
	}
	
	protected void onDone() {
		
	}

	@Override
	protected String getTitle() {
		return $("BaseProgressView.viewTitle");
	}

}
