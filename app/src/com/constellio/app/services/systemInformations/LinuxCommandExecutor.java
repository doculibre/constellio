package com.constellio.app.services.systemInformations;

import com.constellio.app.services.systemInformations.exceptions.LinuxCommandExecutionFailedException;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class LinuxCommandExecutor {

	public String executeCommand(String command) throws LinuxCommandExecutionFailedException {
		BufferedReader bufferedReader = null;
		try {
			String[] arguments = new String[]{"/bin/sh", "-c", command};
			Process process = Runtime.getRuntime().exec(arguments);

			InputStream input = process.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			bufferedReader = new BufferedReader(reader);

			StringBuilder eb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				eb.append(line);
			}

			Worker worker = new Worker(process);
			worker.start();
			try {
				worker.join(15000);
				if (worker.exit != null) {
					return eb.toString();
				} else {
					throw new TimeoutException();
				}
			} catch (InterruptedException e) {
				worker.interrupt();
				Thread.currentThread().interrupt();
				throw e;
			} finally {
				process.destroy();
				IOUtils.closeQuietly(bufferedReader);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new LinuxCommandExecutionFailedException(e);
		}
	}

	private class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
			}
		}
	}
}
