package test.simplefly;

import java.util.Map;

/**
 * The Environment object contains the mutable state of the entire system.
 * 
 * The environment in this case is composed of two maps containing respectively
 * the references to the agents and to their current value.
 * 
 */
public final class Environment {

	/*
	 * Some global constants
	 */
	static final int maxValue = 10;
	static final int minValue = 0;

	/**
	 * maps the id of the agents to the agent refs
	 */
	public final Map<String, SimpleFly> refs;

	/**
	 * maps the id of the agents to their current value
	 */
	public final Map<String, Integer> values;

	public Environment(Map<String, SimpleFly> refs, Map<String, Integer> values) {
		this.refs = refs;
		this.values = values;
	}

}
