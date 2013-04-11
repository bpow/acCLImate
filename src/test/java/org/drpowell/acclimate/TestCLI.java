package org.drpowell.acclimate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.drpowell.acclimate.CLIParser;
import org.drpowell.acclimate.Option;
import org.junit.Test;

public class TestCLI {
	
	private class TestClient {
		private boolean showHelp = false;
		private String input;
		private String output;
		@Option(aliases="-help", name="-h", usage="Show this help message", priority=-2)
		public void showHelp() {
			showHelp = true;
		}
		@Option(name="-i", usage="input file", required=true, priority=-1, defaultArguments={"-"})
		public void setInput(String input) {
			this.input = input;
		}
		@Option(name="-o", usage="output file", required=true)
		public void setOutput(String output) {
			this.output = output;
		}
	}
	
	@Test
	public void testRequired() {
		TestClient client = new TestClient();
		try {
			CLIParser<TestClient> cli = new CLIParser<TestClient>(client).interpret("-i", "input.txt", "-h");
			cli.apply();
			fail("Should have thrown IllegalArgumentException for missing option");
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().indexOf("-o")>=0);
		}
	}
	
	@Test
	public void testDefaultArgs() {
		TestClient client = new TestClient();
		try {
			CLIParser<TestClient> cli = new CLIParser<TestClient>(client).interpret("-o", "output.txt", "-h");
			cli.apply();
			assertEquals("-", cli.getClient().input);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testParseStringArray() {
		CLIParser<TestClient> cli = new CLIParser<TestClient>(new TestClient()).interpret("-i", "testInput.txt", "-o", "output.txt").apply();
		assertEquals("testInput.txt", cli.getClient().input);
	}

	@Test
	public void testPriority() {
		TestClient client = new TestClient();
		CLIParser<TestClient> cli = new CLIParser<TestClient>(client).interpret("-o", "output.txt", "-i", "testInput.txt", "-h");
		assertFalse(client.showHelp);
		cli.apply();
		assertTrue(client.showHelp);
		assertEquals("testInput.txt", client.input);
		assertEquals("output.txt", client.output);
		assertEquals("-h", cli.getReorderedOptions().get(0));
		assertArrayEquals(Arrays.asList("-h", "-i", "testInput.txt", "-o", "output.txt").toArray(new String[5]), cli.getReorderedOptions().toArray(new String[5]));
	}

	@Test
	public void testUsage() {
		CLIParser<TestClient> cli = new CLIParser<TestClient>(new TestClient()).interpret("-o", "output.txt", "-i", "testInput.txt", "-h");
		String usage = cli.usage(true);
		System.err.print(usage);
		assertTrue(usage.indexOf("Show this help message") >= 0);
	}
}
