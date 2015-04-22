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
package com.constellio.app.api.pluginManagerTestResources.pluginImplementation;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.constellio.app.api.APlugin;

@PluginImplementation
public class APluginImplementation implements APlugin {

	public static final String ID = "A plugin implementation id";
	public static final String NAME = "A plugin implementation";
	private static boolean started = false;

	public static boolean isStarted() {
		return started;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getPublisher() {
		return DOCULIBRE;
	}

	@Override
	public void doSomething(String withParameter) {
	}
}
