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
package com.constellio.data.io.factories;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.EncodingService;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.sdk.tests.ConstellioTest;

public class IOServicesFactoryTest extends ConstellioTest {

	private IOServicesFactory factory;
	@Mock private File tempFolder;

	@Before
	public void setUp()
			throws Exception {
		factory = spy(new IOServicesFactory(tempFolder));
	}

	@Test
	public void whenGettingNewEncodingServiceThenEncodingServiceCreated() {
		EncodingService returnedService = factory.newEncodingService();
		assertTrue(returnedService instanceof EncodingService);
	}

	@Test
	public void whenGettingNewFileServiceThenFileServiceCreated() {
		FileService returnedService = factory.newFileService();
		assertTrue(returnedService instanceof FileService);
	}

	@Test
	public void whenGettingNewIOServicesThenIOServicesCreated() {
		IOServices returnedService = factory.newIOServices();
		assertTrue(returnedService instanceof IOServices);
	}

	@Test
	public void whenGettingNewZipServiceThenZipServiceCreated() {
		ZipService returnedService = factory.newZipService();
		assertTrue(returnedService instanceof ZipService);
	}

}
