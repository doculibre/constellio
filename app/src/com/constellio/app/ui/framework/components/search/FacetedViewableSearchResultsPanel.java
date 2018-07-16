package com.constellio.app.ui.framework.components.search;

import com.constellio.app.ui.framework.components.splitpanel.CollapsibleHorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class FacetedViewableSearchResultsPanel extends CollapsibleHorizontalSplitPanel {
	
	private static final int FACETS_PANEL_WIDTH = 300;
	
	private MockViewableSearchResultsPanel viewableSearchResultsPanel;
	
	private FacetsPanel facetsPanel;
	
	public FacetedViewableSearchResultsPanel() {
		super(FacetedViewableSearchResultsPanel.class.getName());
		buildUI();
	}
	
	private void buildUI() {
		setSecondComponentWidth(FACETS_PANEL_WIDTH, Unit.PIXELS);
		
		buildViewableSearchResultsPanel();
		buildFacetsPanel();
		
		setFirstComponent(viewableSearchResultsPanel);
		setSecondComponent(facetsPanel);
	}
	
	private void buildViewableSearchResultsPanel() {
		viewableSearchResultsPanel = new MockViewableSearchResultsPanel();
	}
	
	private void buildFacetsPanel() {
		facetsPanel = new FacetsPanel();
	}
	
	private static class FacetsPanel extends VerticalLayout {
		
		public FacetsPanel() {
			buildUI();
		}
		
		private void buildUI() {
			setSpacing(true);
			setHeight("100%");
			
			String[] panelNames = { "Trier par", "Facette 1", "Facette 2", "Facette 3" };
			for (String panelName : panelNames) {
				Panel facetPanel = new Panel();
				facetPanel.setCaption(panelName);
				addComponent(facetPanel);
			}
		}
		
	}

}
