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