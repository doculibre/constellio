package com.constellio.app.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ZookeeperClient {

	private String address;

	private int port;

	public ZookeeperClient(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public String stat() throws IOException {

		StringBuilder stat = new StringBuilder();

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(address, port), 10_000);
			try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				out.println("stat");
				String line;
				while ((line = input.readLine()) != null) {
					stat.append(line + "\n");
				}
			}
		}
		return stat.toString();
	}
}
