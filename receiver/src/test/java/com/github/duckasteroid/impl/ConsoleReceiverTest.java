package com.github.duckasteroid.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConsoleReceiverTest {

	private static final String EXPECTED = "Test";

	@Test
	public void test() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ConsoleReceiver receiver = new ConsoleReceiver(new PrintStream(output, true, StandardCharsets.UTF_8));
		assertNotNull(receiver);
		receiver.receive(EXPECTED);
		String result = output.toString(StandardCharsets.UTF_8);
		assertEquals(EXPECTED + System.lineSeparator(), result);
	}

}
