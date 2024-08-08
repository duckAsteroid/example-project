package com.github.duckasteroid;

/**
 * An example service that creates (emits) messages
 */
public interface Emitter {
	/**
	 * Emit a message
	 * @return the message
	 */
	String emit();
}
