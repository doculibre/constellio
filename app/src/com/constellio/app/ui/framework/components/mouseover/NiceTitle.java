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
package com.constellio.app.ui.framework.components.mouseover;

import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;

@com.vaadin.annotations.JavaScript("nicetitle.js")
@com.vaadin.annotations.StyleSheet("nicetitle.css")
public class NiceTitle extends AbstractExtension {
	
	private Component component;
	
	private String title;
	
	public NiceTitle(Component component, String title) {
		this.component = component;
		this.title = title;
	}

	@Override
	public void attach() {
		super.attach();
		
		if (StringUtils.isNotBlank(title)) {
			String titleEscaped = StringEscapeUtils.escapeJavaScript(title);
			component.setEnabled(true);
			String componentId = component.getId();
			if (componentId == null) {
				componentId = UUID.randomUUID().toString();
				component.setId(componentId);
			}
			JavaScript javascript = JavaScript.getCurrent();
			javascript.execute("document.getElementById(\"" + componentId + "\").setAttribute(\"title\", \"" + titleEscaped + "\")");
			javascript.execute("makeNiceTitleA(document.getElementById(\"" + componentId + "\"))");
		}
	}

	@Override
	public void detach() {
		JavaScript javascript = JavaScript.getCurrent();
		javascript.execute("hideNiceTitle()");
		super.detach();
	}

}
