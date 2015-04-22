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
package com.constellio.data.dao.managers.config;

@SuppressWarnings("serial")
public class ConfigManagerRuntimeException extends RuntimeException {

	public ConfigManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigManagerRuntimeException(String message) {
		super(message);
	}

	public static class NoSuchConfiguration extends ConfigManagerRuntimeException {

		public NoSuchConfiguration(String path) {
			super("No such configuration '" + path + "'");
		}
	}

	public static class ConfigurationAlreadyExists extends ConfigManagerRuntimeException {

		public ConfigurationAlreadyExists(String path) {
			super("Could not add configuration '" + path + "' since it already exists");
		}
	}

	public static class CannotCompleteOperation extends ConfigManagerRuntimeException {
		public CannotCompleteOperation(String operation, Exception e) {
			super("Cannot complete operation '" + operation + "'", e);
		}
	}

	public static class CannotHashTheFile extends ConfigManagerRuntimeException {
		public CannotHashTheFile(String file, Exception e) {
			super("Could not hash the file '" + file + "'", e);
		}
	}

	public static class WrongVersion extends ConfigManagerRuntimeException {
		public WrongVersion(String version) {
			super("Cannot update a file with the same version as previously : " + version);
		}
	}

}
