package com.github.duckasteroid.impl;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLocatorTest {

	interface NoNameField {

	}

	interface NamedField {
		String NAME = "TestName";
	}

	@Test
	void testMatch() {
		assertEquals("SIMPLE_NAME", ServiceLocator.match(NoNameField.class, "NoNameField"));
		assertEquals("SIMPLE_NAME", ServiceLocator.match(NamedField.class, "NamedField"));
		assertEquals("NAME_FIELD", ServiceLocator.match(NamedField.class, "TestName"));
		assertEquals("NO_NAME", ServiceLocator.match(NamedField.class, null));
		assertNull(ServiceLocator.match(NamedField.class, "WrongName"));

	}

}
