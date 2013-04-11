package org.drpowell.acclimate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class CLIParser<T> {
	private final T client;
	private ArrayList<InterpretedOption> interpretedOptions;
	private ArrayList<String> inputArgs;

	private HashMap<String, Method> optionNames = new LinkedHashMap<String, Method>();
	private HashMap<String, Method> optionAliases = new HashMap<String, Method>();
	

	public CLIParser(T client) {
		this.client = client;
		for (Method m : client.getClass().getDeclaredMethods()) {
			Option opt = m.getAnnotation(Option.class);
			if (opt != null) {
				addParseableOpt(m, optionNames, opt.name());
				addParseableOpt(m, optionAliases, opt.aliases());
			}
		}
	}

	private void addParseableOpt(Method m, HashMap<String, Method> collection, String... opts) {
		for (String opt : opts) {
			if (optionNames.containsKey(opt) || optionAliases.containsKey(opt)) {
				throw new IllegalStateException("Duplicate items added for option " + opt);
			}
			collection.put(opt, m);
		}
	}

	public CLIParser<T> interpret(String... opts) {
		return interpret(Arrays.asList(opts));
	}

	public CLIParser<T> interpret(Collection<String> opts) {
		inputArgs = new ArrayList<String>(opts);
		Iterator<String> it = inputArgs.iterator();
		Set<Method> required = getRequired();
		ArrayList<InterpretedOption> iOpts = new ArrayList<InterpretedOption>();
		while (it.hasNext()) {
			String flag = it.next();
			if (!flag.startsWith("-")) {
				throw new IllegalArgumentException("Option " + flag + " does not start with a '-'");
			}
			Method m = optionNames.get(flag);
			if (m == null) {
				throw new IllegalArgumentException("Option " + flag + " is not defined");
			}
			required.remove(m);
			String[] args = new String[m.getParameterTypes().length];
			for (int i = 0; i < args.length; i++) {
				try {
					args[i] = it.next();
				} catch (NoSuchElementException nsee) {
					throw new IllegalArgumentException(String.format(
							"There are not enough arguments for '%s' (%d expected)", flag, args.length));
				}
			}
			iOpts.add(new InterpretedOption(flag, m, args));
		}
		if (!required.isEmpty()) {
			for (Iterator<Method> reqIterator = required.iterator(); reqIterator.hasNext();) {
				Method m = reqIterator.next();
				Option option = m.getAnnotation(Option.class);
				if (option.defaultArguments().length > 0) {
					iOpts.add(new InterpretedOption(option.name(), m, option.defaultArguments()));
					reqIterator.remove();
				}
			}
			if (!required.isEmpty()) {
				StringBuilder message = new StringBuilder("Required argument(s) left undefined:");
				for (Method m : required) {
					message.append(" ").append(m.getAnnotation(Option.class).name());
				}
				throw new IllegalArgumentException(message.toString());
			}
		}
		Collections.sort(iOpts);
		interpretedOptions = iOpts;
		return this;
	}
	
	public CLIParser<T> apply() throws IllegalArgumentException {
		for (InterpretedOption optarg : interpretedOptions) {
			try {
				optarg.invoke();
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Unable to access method: " + optarg.method, e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException("Unable to invoke method: " + optarg.method, e);
			}
		}
		return this;
	}
	
	public Set<Method> getRequired() {
		Set<Method> out = new HashSet<Method>();
		for (Method m : optionNames.values()) {
			if (m.getAnnotation(Option.class).required()) out.add(m);
		}
		return out;
	}
	
	public T getClient() {
		return client;
	}
	
	public List<String> getReorderedOptions() {
		ArrayList<String> out = new ArrayList<String>();
		for (InterpretedOption optarg : interpretedOptions) {
			out.add(optarg.flag);
			out.addAll(Arrays.asList(optarg.args));
		}
		return out;
	}
	
	public String usage(boolean verbose) {
		StringBuilder sb = new StringBuilder(1024);
		String eol = System.getProperty("line.separator");
		sb.append(client.getClass().getCanonicalName()).append(":").append(eol);
		sb.append("  Available options:").append(eol);
		for (Method m : client.getClass().getDeclaredMethods()) {
			Option opt = m.getAnnotation(Option.class);
			if (opt != null) {
				sb.append(String.format("%10s | %s%s", opt.name(), opt.usage(), eol));
				if (verbose) {
					sb.append(String.format("%10s |   %s%s", "", "Calls " + m.getName(), eol));
				}
			}
		}
		sb.append(eol).append("  Provided arguments:");
		for (String arg: inputArgs) {
			sb.append(" ").append(arg);
		}
		sb.append(eol).append("  Interpreted arguments:").append(eol);
		for (InterpretedOption iOpt: interpretedOptions) {
			sb.append("    ").append(iOpt).append(eol);
		}

		return sb.toString();
	}

	public class InterpretedOption implements Comparable<InterpretedOption>{
		public final String flag;
		public final Method method;
		public final String [] args;
		public InterpretedOption(String flag, Method m, String... args) {
			this.flag = flag;
			method = m;
			this.args = args;
		}
		
		public Option getOption() {
			return method.getAnnotation(Option.class);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(flag);
			for (String arg : args) {
				sb.append(" ").append(arg);
			}
			return sb.toString();
		}

		public int compareTo(InterpretedOption o) {
			return Double.compare(getOption().priority(), o.getOption().priority());
		}

		public Object invoke() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			return method.invoke(client, (Object []) args);
		}
	}

}
