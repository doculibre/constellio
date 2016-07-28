package com.constellio.app.ui.framework.components;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class CollectionsSelectionPanel extends Panel {
	
    List<CheckBox> collectionCheckBoxes = new ArrayList<>();
    
    public CollectionsSelectionPanel(String title, List<String> collections) {
        VerticalLayout layout = new VerticalLayout();
        Label titleLabel = new Label(title);
        layout.addComponent(titleLabel);
        for(String collection: collections){
            addCollectionCheckBox(layout, collection);
        }
        setContent(layout);
        addStyleName(ValoTheme.PANEL_BORDERLESS);
    }

    private void addCollectionCheckBox(Layout layout, String collection) {
        CheckBox checkBox = new CheckBox(collection);
        collectionCheckBoxes.add(checkBox);
        layout.addComponent(checkBox);
    }

    public List<String> getSelectedCollections() {
        List<String> selectedCollections = new ArrayList<>();
        for (CheckBox collectionCheckBox: collectionCheckBoxes){
            if(collectionCheckBox.getValue()){
                selectedCollections.add(collectionCheckBox.getCaption());
            }
        }
        return selectedCollections;
    }
}
