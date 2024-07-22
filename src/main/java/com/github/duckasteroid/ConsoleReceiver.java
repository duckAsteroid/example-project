package com.github.duckasteroid;

import java.io.PrintStream;

public class ConsoleReceiver implements Receiver {
	private final PrintStream out;

	public ConsoleReceiver() {
		this.out = System.out;
	}

	@Override
	public void receive(String msg) {
		out.println(msg);
	}
}
