package com.github.duckasteroid.impl;

import com.github.duckasteroid.Receiver;

import java.io.PrintStream;

/**
 * A receiver that writes to the console {@link System#out}
 */
public class ConsoleReceiver implements Receiver {
	private final PrintStream out;

	public ConsoleReceiver(PrintStream out) {
		this.out = out;
	}

	public ConsoleReceiver() {
		this(System.out);
	}


	@Override
	public void receive(String msg) {
		out.println(msg);
	}
}
