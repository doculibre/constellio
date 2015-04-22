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
package com.constellio.app.services.extensions;

import com.constellio.model.entities.modules.ConstellioPlugin;

@SuppressWarnings("serial")
public class PluginServicesRuntimeException extends RuntimeException {

	public PluginServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PluginServicesRuntimeException(String message) {
		super(message);
	}

	public PluginServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CannotStartPlugin extends PluginServicesRuntimeException {

		public CannotStartPlugin(ConstellioPlugin plugin, Exception e) {
			super("Cannot start plugin '" + plugin.getName() + "' published by '" + plugin.getPublisher() + "'", e);
		}

	}

	public static class CannotStopPlugin extends PluginServicesRuntimeException {

		public CannotStopPlugin(ConstellioPlugin plugin, Exception e) {
			super("Cannot stop plugin '" + plugin.getName() + "' published by '" + plugin.getPublisher() + "'", e);
		}

	}

}
