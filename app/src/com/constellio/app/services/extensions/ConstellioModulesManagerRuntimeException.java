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

import com.constellio.app.entities.modules.InstallableModule;

@SuppressWarnings("serial")
public class ConstellioModulesManagerRuntimeException extends RuntimeException {

	public ConstellioModulesManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioModulesManagerRuntimeException(String message) {
		super(message);
	}

	public ConstellioModulesManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AlreadyStarted extends ConstellioModulesManagerRuntimeException {

		public AlreadyStarted(InstallableModule module) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' is already started");
		}

	}

	public static class FailedToStart extends ConstellioModulesManagerRuntimeException {

		public FailedToStart(InstallableModule module, Exception e) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' failed to start", e);
		}

	}

	public static class FailedToStop extends ConstellioModulesManagerRuntimeException {

		public FailedToStop(InstallableModule module, Exception e) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' failed to stop", e);
		}

	}

	public static class FailedToInstall extends ConstellioModulesManagerRuntimeException {

		public FailedToInstall(InstallableModule module, Exception e) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' failed to install", e);
		}

	}

}
