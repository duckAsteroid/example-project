package com.github.duckasteroid.impl;

import com.github.duckasteroid.Emitter;

/**
 * An example implementation of an emitter - just returns
 * "Hello World"
 */
public class HelloWorldEmitter implements Emitter {
	public static final String HELLO_WORLD = "Hello World";
	@Override
	public String emit() {
		return HELLO_WORLD;
	}


}
