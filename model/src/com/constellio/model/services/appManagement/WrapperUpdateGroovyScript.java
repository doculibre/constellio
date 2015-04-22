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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WrapperUpdateGroovyScript {

	public void callGroovyScript(File wrapperConf, File previousWrapperConf)
			throws IOException {

		// ***** UNCOMMENT FIRST LINES AND COPY SCRIPT IN THE CORRECT SECTION IN commandCondition.gv *****

		// File wrapperConf = new File("."+ File.separator + "conf" + File.separator + "wrapper.conf");
		// File previousWrapperConf = new File(wrapperConf.getParentFile(), wrapperConf.getName().replace(".conf", ".conf.bck"));

		List<String> lines = new ArrayList<String>();
		java.io.BufferedReader br = null;
		try {
			br = new java.io.BufferedReader(new java.io.FileReader(wrapperConf));
			String line = null;

			while ((line = br.readLine()) != null) {

				int windowsWebappIndex = line.indexOf("\\\\webapp");
				if (windowsWebappIndex != -1) {
					String currentWebapp = null;
					String newWebapp = null;
					int indexBackSlash = line.indexOf("\\\\", windowsWebappIndex + 2);
					currentWebapp = line.substring(windowsWebappIndex + 2, indexBackSlash);
					if (currentWebapp.contains("-")) {
						newWebapp = "webapp-" + (Integer.valueOf(currentWebapp.split("-")[1]) + 1);
					} else {
						newWebapp = "webapp-2";
					}
					line = line.replace(currentWebapp, newWebapp);
				}

				int linuxWebappIndex = line.indexOf("\\/webapp");
				if (linuxWebappIndex != -1) {
					String currentWebapp = null;
					String newWebapp = null;
					int indexBackSlash = line.indexOf("\\/", linuxWebappIndex + 2);
					currentWebapp = line.substring(linuxWebappIndex + 2, indexBackSlash);
					if (currentWebapp.contains("-")) {
						newWebapp = "webapp-" + (Integer.valueOf(currentWebapp.split("-")[1]) + 1);
					} else {
						newWebapp = "webapp-2";
					}
					line = line.replace(currentWebapp, newWebapp);
				}
				lines.add(line);

			}

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
				}
			}
		}

		if (previousWrapperConf.exists()) {
			previousWrapperConf.delete();
		}
		wrapperConf.renameTo(previousWrapperConf);

		java.io.BufferedWriter bw = null;
		try {
			bw = new java.io.BufferedWriter(new java.io.FileWriter(wrapperConf, false));
			for (String line : lines) {
				bw.write(line);
				bw.newLine();
			}

		} finally {
			bw.close();
		}

		// process.restart();

		// ***** UNCOMMENT FIRST LINES AND COPY SCRIPT IN THE CORRECT SECTION IN commandCondition.gv *****
	}
}
