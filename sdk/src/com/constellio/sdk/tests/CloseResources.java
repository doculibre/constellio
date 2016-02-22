package com.constellio.sdk.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.constellio.data.io.concurrent.filesystem.AbstractAtomicFileSystem;

public class CloseResources implements TestRule{
	public final class AtomicFileSystemTest extends Statement {
		private final Statement statement;

		public AtomicFileSystemTest(Statement aStatement) {
			statement = aStatement;
		}

		@Override
		public void evaluate()
				throws Throwable {
			assertThat(AbstractAtomicFileSystem.getOpenedFileSystem()).isEqualTo(0);
			statement.evaluate();
			assertThat(AbstractAtomicFileSystem.getOpenedFileSystem()).isEqualTo(0);
		}

	}


	@Override
	public Statement apply(Statement base, Description description) {
		
		return new AtomicFileSystemTest(base);
	}

}
