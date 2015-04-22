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
package com.constellio.model.services.parser;

import org.apache.tika.fork.ForkParser;
import org.apache.tika.parser.AutoDetectParser;

import com.constellio.data.dao.managers.StatefulService;

public class ForkParsers implements StatefulService {

	private ForkParser forkParser;

	private int forkParserPoolSize;

	public ForkParsers(int forkParserPoolSize) {
		super();
		this.forkParserPoolSize = forkParserPoolSize;
	}

	public synchronized ForkParser getForkParser() {
		if (forkParser == null) {
			forkParser = newForkParser();
			forkParser.setPoolSize(forkParserPoolSize);
		}

		return forkParser;
	}

	public ForkParser newForkParser() {
		AutoDetectParser parser = new AutoDetectParser();
		return new ForkParser(getClass().getClassLoader(), parser);

	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {
		if (forkParser != null) {
			forkParser.close();
		}
		forkParser = null;
	}

}