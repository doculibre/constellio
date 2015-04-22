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
package com.constellio.app.ui.tools.vaadin;

import java.util.Date;

import com.constellio.app.ui.handlers.ConstellioErrorHandler;
import com.constellio.app.ui.tools.ServerThrowableContext;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeWriter;
import com.vaadin.server.ErrorEvent;

@SuppressWarnings("serial")
public class TestErrorHandler extends ConstellioErrorHandler {
	
	@Override
	public void error(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		ServerThrowableContext.LAST_THROWABLE.set(throwable);
//		throwable.printStackTrace();
		
		new PageLoadTimeWriter().write(new Date());
		
		super.error(event);
	}

}
