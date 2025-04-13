package com.github.duckasteroid;

/**
 * An example service that creates (emits) messages.
 * A change since v0.1.1
 */
public interface Emitter {
	/**
	 * Emit a message
	 * @return the message
	 */
	String emit();
}
