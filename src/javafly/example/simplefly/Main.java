package javafly.example.simplefly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a very simple example where the agents try to synchronize their
 * values by increasing or decreasing them.
 * 
 * @author jorquera
 *
 */
public final class Main {

	public static void main(String[] args) {

		// initialize the environment
		Map<String, SimpleFly> refs = new HashMap<>();
		refs.put("a", new SimpleFly("a", Arrays.asList("b")));
		refs.put("b", new SimpleFly("b", Arrays.asList("a", "c")));
		refs.put("c", new SimpleFly("c", Arrays.asList("b", "d")));
		refs.put("d", new SimpleFly("d", Arrays.asList("c")));

		Map<String, Integer> values = new HashMap<>();
		values.put("a", 2);
		values.put("b", 9);
		values.put("c", 3);
		values.put("d", 6);

		Environment env = new Environment(refs, values);

		System.out.println("--- INITIAL STATE");
		printEnv(env);

		// run the system until it has converged
		// (all criticalities are equal to 0)
		int turn = 0;
		boolean converged = false;
		while (!converged) {

			turn++;
			System.out.println("### TURN " + turn);

			// execute the decision-action of each agent in sequence, in order
			// to change the environment
			for (SimpleFly f : refs.values()) {
				env = f.act(env, f.decision(env));
			}
			// check if the system has converged
			converged = hasConverged(env);

			// display the environment state
			printEnv(env);
		}
		System.out.println("--- SUCCESS !");

	}

	/**
	 * Check if the system has converged, that is, all the criticalities are
	 * equal to 0)
	 * 
	 * @param env
	 *            the current environment
	 * @return true if the system has converged
	 */
	private static boolean hasConverged(final Environment env) {
		return env.refs.values().stream()
				.allMatch(f -> f.criticality(env) == 0);
	}

	/**
	 * Print the environment stats
	 * 
	 * @param env
	 *            the environment to display
	 */
	private static void printEnv(final Environment env) {
		for (String s : env.refs.keySet()) {
			System.out.print(s + ": ( value: " + env.values.get(s) + ", crit: "
					+ env.refs.get(s).criticality(env) + " ) ");

		}
		System.out.println("\nmax criticality: "
				+ env.refs.values().stream().map(f -> f.criticality(env))
						.max((v1, v2) -> v1.compareTo(v2)).get() + "\n");
	}

}
