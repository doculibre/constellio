package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

public class StringListComponent extends Panel {
    private List<String> values;
    protected TextArea content;

    public StringListComponent() {
        setSizeFull();
        addStyleName(ValoTheme.PANEL_BORDERLESS);
        content = new TextArea();
        content.setSizeFull();
        setContent(content);
    }

    public List<String> getValues() {
        String text = content.getValue();
        String[] newValues = StringUtils.split(text, System.getProperty("line.separator"));
        values= Arrays.asList(newValues);
        return values;
    }

    public void setValues(List<String> values) {
        if (values != null){
            String text = StringUtils.join(values, System.getProperty("line.separator"));
            content.setValue(text);
            this.values = values;
        }else{
            content.setValue("");

        }
    }

    public void setRequired(boolean required) {
        content.setRequired(required);
    }
}
