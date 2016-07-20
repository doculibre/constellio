package com.constellio.sdk.tests;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;

public class SDKPanel extends HorizontalLayout {

	public SDKPanel() {

		addComponent(newReindexButton());
		addComponent(newSaveWindowPositionButtom());
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

}
