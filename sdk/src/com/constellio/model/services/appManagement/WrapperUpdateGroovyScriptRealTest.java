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
package com.constellio.model.services.appManagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class WrapperUpdateGroovyScriptRealTest extends ConstellioTest {

	String windowsWebappWrapperConf = "...before...\n" + "wrapper.java.classpath.1 = ..\\\\webapp\\\\WEB-INF\\\\lib\\\\*.jar\n"
			+ "wrapper.java.classpath.2 = ..\\\\webapp\\\\WEB-INF\\\\classes\n" + "...after...\n";

	String windowsWebapp2WrapperConf = "...before...\n"
			+ "wrapper.java.classpath.1 = ..\\\\webapp-2\\\\WEB-INF\\\\lib\\\\*.jar\n"
			+ "wrapper.java.classpath.2 = ..\\\\webapp-2\\\\WEB-INF\\\\classes\n" + "...after...\n";

	String windowsWebapp3WrapperConf = "...before...\n"
			+ "wrapper.java.classpath.1 = ..\\\\webapp-3\\\\WEB-INF\\\\lib\\\\*.jar\n"
			+ "wrapper.java.classpath.2 = ..\\\\webapp-3\\\\WEB-INF\\\\classes\n" + "...after...\n";

	String linuxWebappWrapperConf = "...before...\n" + "wrapper.java.classpath.1 = ..\\/webapp\\/WEB-INF\\/lib\\/*.jar\n"
			+ "wrapper.java.classpath.2 = ..\\/webapp\\/WEB-INF\\/classes\n" + "...after...\n";

	String linuxWebapp2WrapperConf = "...before...\n" + "wrapper.java.classpath.1 = ..\\/webapp-2\\/WEB-INF\\/lib\\/*.jar\n"
			+ "wrapper.java.classpath.2 = ..\\/webapp-2\\/WEB-INF\\/classes\n" + "...after...\n";

	String linuxWebapp3WrapperConf = "...before...\n" + "wrapper.java.classpath.1 = ..\\/webapp-3\\/WEB-INF\\/lib\\/*.jar\n"
			+ "wrapper.java.classpath.2 = ..\\/webapp-3\\/WEB-INF\\/classes\n" + "...after...\n";

	File wrapperConf;

	File previousWrapperConf;

	WrapperUpdateGroovyScript script;

	@Before
	public void setUp() {
		File tempDir = newTempFolder();
		wrapperConf = new File(tempDir, "wrapper.conf");
		previousWrapperConf = new File(tempDir, "wrapper.conf.bck");
		script = new WrapperUpdateGroovyScript();
	}

	@Test
	public void givenLinuxWebappThenSetToWebapp2()
			throws Exception {
		FileUtils.write(wrapperConf, linuxWebappWrapperConf);
		callScript();
		assertThat(wrapperConf).hasContent(linuxWebapp2WrapperConf);
		assertThat(previousWrapperConf).hasContent(linuxWebappWrapperConf);
	}

	@Test
	public void givenLinuxWebapp2ThenSetToWebapp3()
			throws Exception {
		FileUtils.write(wrapperConf, linuxWebapp2WrapperConf);
		callScript();
		assertThat(wrapperConf).hasContent(linuxWebapp3WrapperConf);
		assertThat(previousWrapperConf).hasContent(linuxWebapp2WrapperConf);
	}

	@Test
	public void givenWindowsWebappThenSetToWebapp2()
			throws Exception {
		FileUtils.write(wrapperConf, windowsWebappWrapperConf);
		callScript();
		assertThat(wrapperConf).hasContent(windowsWebapp2WrapperConf);
		assertThat(previousWrapperConf).hasContent(windowsWebappWrapperConf);
	}

	@Test
	public void givenWindowsWebapp2ThenSetToWebapp3()
			throws Exception {
		FileUtils.write(wrapperConf, windowsWebapp2WrapperConf);
		callScript();
		assertThat(wrapperConf).hasContent(windowsWebapp3WrapperConf);
		assertThat(previousWrapperConf).hasContent(windowsWebapp2WrapperConf);
	}

	private void callScript()
			throws IOException {
		script.callGroovyScript(wrapperConf, previousWrapperConf);
	}

}
