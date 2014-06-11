package test.simplefly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import core.Firefly;

/**
 * Our simple agent implementation
 * 
 * @author jorquera
 *
 */
public final class SimpleFly implements Firefly<Environment, Action, Double, SimpleFly> {

	/**
	 * the unique id of the agent
	 */
	final String id;

	/**
	 * the neighbors of the agent (should include itself)
	 * 
	 * NOTE: in this application, the neighborhood is static. When it is not the
	 * case, it should be included in the environment and not in the agent
	 * itself (agents should be stateless).
	 * 
	 */
	private final List<String> neighbors;

	/*
	 * the possible actions for the agent
	 */
	private final Action incr;
	private final Action decr;

	public SimpleFly(String id, List<String> neighbors) {
		super();

		this.id = id;
		this.neighbors = new ArrayList<>(neighbors);

		// adding itself to the neighbors (important)
		this.neighbors.add(id);

		this.incr = new Increase(id);
		this.decr = new Decrease(id);
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public List<SimpleFly> predictedNeighbors(Environment e, Set<Action> a) {
		// since the neighborhood is static, this function is very simple
		// in this case we simply map the neighbors id to their references
		return neighbors.stream().map(s -> e.refs.get(s))
				.collect(Collectors.toList());
	}

	@Override
	public Set<Action> possibleActions(Environment e) {
		// in this application there is only two potential actions, which
		// are only possible if the agent is not at the corresponding
		// boundary

		int currentValue = e.values.get(id);

		Set<Action> possibleActions = new HashSet<>();
		if (currentValue < Environment.maxValue) {
			possibleActions.add(incr);
		}
		if (currentValue > Environment.minValue) {
			possibleActions.add(decr);
		}

		return possibleActions;

	}

	@Override
	public Set<Action> contradictoryActions(Environment env, Set<Action> actions) {
		// the two potential actions are mutually exclusive. If one is
		// selected the other must be excluded
		Set<Action> contradictActions = new HashSet<>();
		if (actions.contains(incr)) {
			contradictActions.add(decr);
		}
		if (actions.contains(decr)) {
			contradictActions.add(incr);
		}
		return contradictActions;
	}

	/*
	 * Since this application is very simple, we will directly define the
	 * criticality here. It will allow us to use it in the predictedCriticality
	 * function (instead of doing it the other way, as it is the default in the
	 * interface)
	 */
	@Override
	public Double criticality(Environment env) {
		// the criticality is the biggest distance between the value of the
		// agent and the ones of its neighbors, divided by the max possible
		// gap

		final int value = env.values.get(id);

		final int maxDist = this.predictedNeighbors(env, new HashSet<>())
				.stream()
				// map neighbors to their values and convert to distances
				.map(f -> Math.abs(value - env.values.get(f.id)))
				// get the biggest distance
				.max((v1, v2) -> v1.compareTo(v2)).get();

		// convert to criticality
		return maxDist
				/ new Double((Environment.maxValue - Environment.minValue));
	}

	@Override
	public Double predictedCriticality(Environment env, Set<Action> actions,
			Firefly<Environment, Action, Double, SimpleFly> agent) {
		// here we simulate a very simple prediction by directly calling the
		// criticality function of the agent on the anticipated environment

		// There should be at most one selected action
		// since there are only two, contradictory actions possible
		assert actions.size() <= 1;

		Environment predictedEnv = env;
		if (!actions.isEmpty()) {
			// apply the selected action to the current environment
			predictedEnv = actions.iterator().next().apply(env);
		}

		// return the criticality of the agent in the predicted
		// environment
		return agent.criticality(predictedEnv);

	}

}
