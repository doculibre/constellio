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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;

import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;

public interface ConfigManager {

	PropertiesAlteration EMPTY_PROPERTY_ALTERATION = new PropertiesAlteration() {
		@Override
		public void alter(Map<String, String> properties) {
		}
	};

	BinaryConfiguration getBinary(String path);

	XMLConfiguration getXML(String path);

	TextConfiguration getText(String path);

	PropertiesConfiguration getProperties(String path);

	boolean exist(String path);

	List<String> list(String path);

	void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration);

	void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration);

	void delete(String path);

	void delete(String path, String hash)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void updateXML(String path, DocumentAlteration documentAlteration);

	void updateProperties(String path, PropertiesAlteration propertiesAlteration);

	void add(String path, InputStream newBinaryStream);

	void add(String path, Document newDocument);

	void add(String path, Map<String, String> newProperties);

	void update(String path, String hash, InputStream newBinaryStream)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void update(String path, String hash, Document newDocument)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void update(String path, String hash, Map<String, String> newProperties)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void registerListener(String path, ConfigEventListener listener);

	void deleteAllConfigsIn(String collection);
}
