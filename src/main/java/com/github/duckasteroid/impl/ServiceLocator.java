package com.github.duckasteroid.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * Helps the main class locate and load NAMED services from {@link ServiceLoader}
 * @param <T> type of service to locate
 */
public class ServiceLocator<T> {
	private final Predicate<ServiceLoader.Provider<T>> matcher;
	private final ServiceLoader<T> providers;

	public ServiceLocator(Class<T> type, Predicate<ServiceLoader.Provider<T>> matcher) {
		this.matcher = matcher;
		providers = ServiceLoader.load(type);
	}

	public static <T> ServiceLocator<T> byName(Class<T> c, String name) {
	    return new ServiceLocator<>(c, new NamePredicate<>(c, name));
	}

	private record NamePredicate<X>(Class<X> c, String name) implements Predicate<ServiceLoader.Provider<X>> {
		@Override
			public boolean test(ServiceLoader.Provider<X> p) {
				return match(p.type(), name) != null;
			}

		@Override
		public String toString() {
			return "Matching " + c.toString() + " name=" + name;
		}
	}

	public static String match(Class<?> c, String name) {
	    if (name != null && !name.isEmpty()) {
	        // does the name match the simple class name (ignore case)
	        if (c.getSimpleName().equalsIgnoreCase(name)) {
	            return "SIMPLE_NAME";
	        }
	        // no...
	        try {
	            // try a static field NAME
	            Field nameField = c.getDeclaredField("NAME");
	            if (nameField.getType().equals(String.class) && Modifier.isStatic(nameField.getModifiers()))
	            {
	                nameField.setAccessible(true);
	                String nameValue = (String) nameField.get(null);
	                if (name.equals(nameValue)) {
	                    return "NAME_FIELD";
	                }
	            }
	        }
	        catch (NoSuchFieldException e) {
	            // this is fine just continue
	        } catch (IllegalAccessException e) {
	            System.err.println("Unable to access NAME field" + e.getMessage());
	        }
					return null; // name given but not found
	    }
	    return "NO_NAME";
	}

	public T getService() {
	    return providers.stream()
	            .filter(matcher)
	            .findAny()
	            .map(ServiceLoader.Provider::get)
					    .orElseThrow(() -> new NoSuchElementException("No service found for " + matcher));
	}
}
