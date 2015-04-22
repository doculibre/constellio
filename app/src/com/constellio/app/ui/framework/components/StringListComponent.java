/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
