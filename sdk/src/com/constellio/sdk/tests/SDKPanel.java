package com.constellio.sdk.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.SystemLoadSimulator.SystemLoadLevel;
import com.constellio.sdk.tests.TestPagesComponentsExtensions.BottomPanelBigVaultServerExtension;
import com.constellio.sdk.tests.TestPagesComponentsExtensions.LoggedItem;
import com.constellio.sdk.tests.TestPagesComponentsExtensions.LoggedItemCall;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class SDKPanel extends HorizontalLayout {

	Label loadLabel;

	public SDKPanel() {

		loadLabel = newCountersLabel();
		addComponent(newLoadSimulationButton());
		addComponent(newShowQueriesButton());
		addComponent(newShowUpdatesButton());
		addComponent(newResetButton(loadLabel));
		addComponent(newReindexButton());
		addComponent(newSaveWindowPositionButtom());
		addComponent(loadLabel);
	}

	private Button newReindexButton() {
		final Button resetButton = new Button("Reindex");
		resetButton.addClickListener(new ClickListener() {

			@Override
			public synchronized void buttonClick(ClickEvent event) {
				ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
				modelLayerFactory.newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

			}

		});
		return resetButton;
	}

	private Button newSaveWindowPositionButtom() {
		final Button resetButton = new Button("Set as prefered window position and size");
		resetButton.addClickListener(new ClickListener() {

			@Override
			public synchronized void buttonClick(ClickEvent event) {

				WebDriver webDriver = ConstellioTestSession.get().getSeleniumTestFeatures().getLastWebDriver();
				Point position = webDriver.manage().window().getPosition();
				Dimension dimension = webDriver.manage().window().getSize();

				Map<String, String> params = new HashMap<String, String>();
				params.put("window.position.x", "" + position.getX());
				params.put("window.position.y", "" + position.getY());
				params.put("window.width", "" + dimension.getWidth());
				params.put("window.height", "" + dimension.getHeight());

				ConstellioTest.sdkPropertiesLoader.writeValues(params);

			}

		});
		return resetButton;
	}

	private Component newShowQueriesButton() {
		final WindowButton showQueries = new WindowButton("Show queries", "Queries",
				WindowConfiguration.modalDialog("90%", "90%")) {

			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				Table table = createTable();
				table.addItemClickListener(getListener("Request"));
				layout.addComponent(table);
				return layout;
			}
		};
		return showQueries;
	}

	private Component newShowUpdatesButton() {
		final WindowButton showQueries = new WindowButton("Show updates", "Updates",
				WindowConfiguration.modalDialog("90%", "90%")) {

			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				Table table = createTable();
				table.addItemClickListener(getListener("Update"));
				layout.addComponent(table);
				return layout;
			}

		};
		return showQueries;
	}

	private Table createTable() {
		Table table = new Table();
		table.setContainerDataSource(new BeanItemContainer<>(bigVaultServerExtension().queries));
		table.removeContainerProperty(LoggedItem.CALLS);
		table.setSortEnabled(true);
		return table;
	}

	private ItemClickListener getListener(final String eventType) {
		return new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {

				Item item = event.getItem();
				LoggedItem loggedItem = (LoggedItem) ((BeanItem) item).getBean();
				StringBuilder message = new StringBuilder();
				message.append("==========================================================================\n");
				message.append(eventType + "#" + propertyValue(item, LoggedItem.ID));
				message.append(" (" + propertyValue(item, LoggedItem.QTIME) + "ms) ");
				message.append(" (repeated " + propertyValue(item, LoggedItem.REPEATED) + " times) ");
				message.append(propertyValue(item, LoggedItem.TITLE) + "\n\n");

				java.util.List<LoggedItemCall> calls = sortedCalls(loggedItem.getCalls());
				for (int i = 0; i < calls.size(); i++) {
					LoggedItemCall call = calls.get(i);
					if (call.getRepeated() == 1) {
						message.append("\n--Call #" + (i + 1) + " (" + call.getQtime() + "ms)");
					} else {
						message.append("\n--Call #" + (i + 1) + " repeated " + call.getRepeated()
								+ " times (" + call.getQtime() + "ms)");
					}

					if (!call.getDescription().isEmpty()) {
						message.append(call.getDescription() + "\n\n");
					}
					message.append(call.getStack() + "\n\n");
				}

				System.out.println(message);

				Notification.show("Stack traces were printed in the terminal", Type.WARNING_MESSAGE);
			}

			private List<LoggedItemCall> sortedCalls(List<LoggedItemCall> calls) {
				List<LoggedItemCall> sortedCalls = new ArrayList<>(calls);

				Collections.sort(sortedCalls, new Comparator<LoggedItemCall>() {
					@Override
					public int compare(LoggedItemCall c1, LoggedItemCall c2) {
						Long repeated1 = c1.getQtime();
						Long repeated2 = c2.getQtime();

						return repeated1.compareTo(repeated2);
					}
				});

				return sortedCalls;
			}
		};
	}

	private Object propertyValue(Item item, String propertyId) {
		Property property = item.getItemProperty(propertyId);
		return property != null ? property.getValue() : "No " + propertyId;
	}

	private BottomPanelBigVaultServerExtension bigVaultServerExtension() {
		Iterator<BigVaultServerExtension> extensionIterator = ConstellioFactories.getInstance().getDataLayerFactory()
				.getExtensions().getSystemWideExtensions().getBigVaultServerExtension().iterator();
		while (extensionIterator.hasNext()) {
			BigVaultServerExtension next = extensionIterator.next();
			if (next instanceof BottomPanelBigVaultServerExtension) {
				return (BottomPanelBigVaultServerExtension) next;
			}
		}
		throw new ImpossibleRuntimeException("No such extension");
	}

	private String getCountersLabelCaption() {
		BottomPanelBigVaultServerExtension extension = bigVaultServerExtension();
		StringBuilder captionBuilder = new StringBuilder();
		captionBuilder.append("Queries : " + extension.getQueryCount());
		captionBuilder.append(" (" + extension.queryTimeCount + "ms)");
		captionBuilder.append("\nUpdates : " + extension.getUpdateCount());
		captionBuilder.append(" (" + extension.updateTimeCount + "ms)");
		return captionBuilder.toString();
	}

	private Label newCountersLabel() {
		final Label infoLabel = new Label();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				VaadinSession.getCurrent().lock();

				infoLabel.setCaption(getCountersLabelCaption());
				infoLabel.markAsDirty();
				VaadinSession.getCurrent().unlock();
			}
		};
		Timer t = new Timer(true);
		t.scheduleAtFixedRate(tt, 0, 1000);
		return infoLabel;
	}

	private Component newResetButton(final Label loadLabel) {

		final Button resetButton = new Button("Reset counters");
		resetButton.addClickListener(new ClickListener() {

			@Override
			public synchronized void buttonClick(ClickEvent event) {
				BottomPanelBigVaultServerExtension extension = bigVaultServerExtension();
				extension.updates.clear();
				extension.updateTimeCount.set(0);
				extension.queries.clear();
				extension.queriesCount.set(0);
				extension.queryTimeCount.set(0);
				loadLabel.setCaption(getCountersLabelCaption());
			}

		});
		return resetButton;
	}

	private Component newLoadSimulationButton() {

		final Button loadSimulationButton = new Button(getLoadSimulationButtonCaption(SystemLoadLevel.OFF));
		loadSimulationButton.addClickListener(new ClickListener() {

			@Override
			public synchronized void buttonClick(ClickEvent event) {
				BottomPanelBigVaultServerExtension extension = bigVaultServerExtension();
				AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
				BigVaultServer vaultServer = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getRecordsVaultServer();
				SystemLoadLevel newValue = extension.loadLevel.toggle();
				extension.loadLevel = newValue;
				loadSimulationButton.setCaption(getLoadSimulationButtonCaption(newValue));
			}

		});
		return loadSimulationButton;
	}

	private String getLoadSimulationButtonCaption(SystemLoadLevel newValue) {
		return "Load simulation : " + (newValue.name());
	}

}
