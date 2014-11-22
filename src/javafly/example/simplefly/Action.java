package javafly.example.simplefly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 
 * The common interface for the possible actions of the agents. Only used for
 * type consistency
 * 
 * @author jorquera
 *
 */
public interface Action extends Function<Environment, Environment> {
}

/**
 * 
 * An action where the agent increases its own value
 * 
 * @author jorquera
 *
 */
final class Increase implements Action {
	private final String agentId;

	public Increase(String agentId) {
		super();
		this.agentId = agentId;
	}

	@Override
	public Environment apply(Environment t) {
		Map<String, Integer> res = new HashMap<>(t.values);

		// increase the value of the agent by 1 (if possible)
		res.put(agentId,
				Math.min(t.values.get(agentId) + 1, Environment.maxValue));
		return new Environment(t.refs, res);
	}

	@Override
	public String toString() {
		return "Increase";
	}

}

/**
 * 
 * An action where the agent decreases its own value
 * 
 * @author jorquera
 *
 */
final class Decrease implements Action {
	private final String agentId;

	public Decrease(String agentId) {
		super();
		this.agentId = agentId;
	}

	@Override
	public Environment apply(Environment t) {
		Map<String, Integer> res = new HashMap<>(t.values);

		// decrease the value of the agent by 1 (if possible)
		res.put(agentId,
				Math.max(t.values.get(agentId) - 1, Environment.minValue));
		return new Environment(t.refs, res);
	}

	@Override
	public String toString() {
		return "Decrease";
	}

}
