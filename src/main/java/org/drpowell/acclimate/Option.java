package org.drpowell.acclimate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Option {
	/**
	 * The long version(s) of the option name
	 */
	String [] aliases() default {};

	/**
	 * The short version of an option (typically single-character)
	 */
	String name() default "";

	/**
	 * A helpful, descriptive message for the user
	 */
	String usage() default "";

	/**
	 * Whether the argument can be called more than once
	 */
	boolean multiple() default false;

	/**
	 * An (optional) priority for arguments. Lower-priority arguments will be
	 * sorted to the front prior to execution
	 */
	int priority() default 0;
	
	/**
	 * Whether the argument is required
	 */
	boolean required() default false;

	/**
	 * For required options, the argument(s) to be used if none are given on command line
	 */
	String [] defaultArguments() default {};
	
}
