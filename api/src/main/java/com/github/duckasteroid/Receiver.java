package com.github.duckasteroid;

/**
 * A service that can receive a message and do something with it
 */
public interface Receiver {
	/**
	 * Receive a message
	 * @param msg the message to receive
	 */
	void receive(String msg);
}
