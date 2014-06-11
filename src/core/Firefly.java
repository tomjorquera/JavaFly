package core;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The interface corresponding to a cooperative agent.
 * 
 * This agent representation is purely functional. All the mutable state is
 * delegated to the environment. Consequently, these agents can be seen as pure
 * decision functions which select the next actions to be applied in order to
 * mutate the environment.
 * <p/>
 * It provides default implementations for several functions, notably the
 * decision function, which contains the decision algorithm where the actions to
 * execute are selected in a way which minimizes the criticality of its
 * neighborhood, based on a lexicographical comparison of the anticipated
 * criticalities of the neighbors.
 * <p/>
 * This default decision function require the implementation of several
 * domain-dependent (functional) methods which are defined in the interface:
 * <ul>
 * <li>predictedNeighbors, which predict the neighborhood of an agent</li>
 * <li>possibleActions, which gives the current possible actions of the agent</li>
 * <li>contradictoryActions, which indicate which actions are contradictory to
 * others</li>
 * <li>predictedCriticality, which predict the criticality of an agent</li>
 * </ul>
 * <p/>
 * Some other default functions are provided:
 * <ul>
 * <li>act: which apply a set of actions sequentially to the environment</li>
 * <li>criticality: which return the current criticality using the
 * predictedCriticality function</li>
 * </ul>
 * 
 * @author jorquera
 *
 * @param <Env>
 *            the environment of the agents
 * @param <Action>
 *            the type of actions available to the agent
 * @param <Criticality>
 *            the criticality representation
 */
public interface Firefly<Env, Action extends Function<Env, Env>, Criticality extends Comparable<Criticality>, Agent extends Firefly<Env, Action, Criticality, Agent>> {

	/**
	 * This method takes in argument the current environment and a set of
	 * actions, and returns a list of predicted neighbors.
	 * 
	 * @param env
	 *            the current environment
	 * @param actions
	 *            the actions to be applied
	 * @return the predicted list of neighbors
	 */
	public List<Agent> predictedNeighbors(Env env, Set<Action> actions);

	/**
	 * This function takes in argument the current environment, and returns a
	 * set of possible actions.
	 * 
	 * @param env
	 *            the current environment
	 * @return the possible actions
	 */
	public Set<Action> possibleActions(Env env);

	/**
	 * This function takes in argument the current environment and a set of
	 * actions, and returns the set of actions which are conflicting with the
	 * currently selected actions in the given environment.
	 * 
	 * @param env
	 *            the current environment
	 * @param actions
	 *            the selected actions
	 * @return the contradictory actions
	 */
	public Set<Action> contradictoryActions(Env env, Set<Action> actions);

	/**
	 * TODO: DOCUMENT
	 * 
	 * @param env
	 * @param actions
	 * @param action
	 * @return
	 */
	default boolean isCompatible(Env env, Set<Action> actions, Action action) {
		return !contradictoryActions(env, actions).contains(action);
	}

	/**
	 * This function takes in argument the current environment, a set of actions
	 * and an agent, and returns the predicted criticality for this agent if the
	 * actions are applied to the environment.
	 * 
	 * @param env
	 *            the current environment
	 * @param actions
	 *            the actions to be applied
	 * @param agent
	 *            the agent to examine
	 * @return the predicted criticality of the examinated agent
	 */
	public Criticality predictedCriticality(Env env, Set<Action> actions,
			Firefly<Env, Action, Criticality, Agent> agent);

	/**
	 * This function takes in argument the current environment, and return a set
	 * of actions to be applied.
	 * 
	 * The default implementation for this method returns a decision function
	 * which select the actions in order to minimize the predicted maximum
	 * criticality among the (predicted) neighbors of the agents, based on the
	 * lexicographic order of the criticalities of the agents for two different
	 * possible actions.
	 * 
	 * @param env
	 *            the current environment
	 * @return the actions to be applied
	 */
	default Set<Action> decision(Env env) {
		Set<Action> candidateActions = possibleActions(env);
		Set<Action> selectedActions = new HashSet<>();

//		List<Action> tmp = new ArrayList<>(candidateActions);
//		tmp.sort(actionComparator(env, selectedActions));
//		System.out.println(tmp);

		boolean stop = false;

		while (!candidateActions.isEmpty() && !stop) {
			Action bestAction = getBestAction(candidateActions, env,
					selectedActions);

			HashSet<Action> testedActions1 = new HashSet<>(selectedActions);
			testedActions1.add(bestAction);

			List<Criticality> c1 = predictedNeighbors(env, testedActions1)
					.stream()
					.map(n -> predictedCriticality(env, testedActions1, n))
					.collect(Collectors.toList());

			List<Criticality> c2 = predictedNeighbors(env, selectedActions)
					.stream()
					.map(n -> predictedCriticality(env, selectedActions, n))
					.collect(Collectors.toList());

			if (criticalitiesComparator().compare(c1, c2) > 0) {
				stop = true;
			} else {
				selectedActions.add(bestAction);
				candidateActions.remove(bestAction);
				candidateActions = candidateActions.stream()
						.filter(a -> isCompatible(env, selectedActions, a))
						.collect(Collectors.toSet());
			}
		}

		return selectedActions;
	}

	/**
	 * This method applies a set of actions to the given environment.
	 * 
	 * In the default implementation, the actions are applied sequentially.
	 * 
	 * @param env
	 *            the starting environment
	 * @param actions
	 *            the actions to be applied
	 * @return the new environment
	 */
	default Env act(Env env, Set<Action> actions) {
		Env newEnv = env;

		for (Action action : actions) {
			newEnv = action.apply(newEnv);
		}

		return newEnv;
	}

	/**
	 * This method gives the criticality of the agent in the given environment.
	 * 
	 * @param env
	 *            the environment in which to evaluate the criticality
	 * @return the criticality
	 */
	default Criticality criticality(Env env) {
		return predictedCriticality(env, new HashSet<Action>(), this);
	}

	/*
	 * ///////////////////////// UTILITY FUNCTIONS ////////////////////////////
	 */

	/**
	 * 
	 * Returns the best possible action to be done in a given environment and in
	 * regard to a set of already selected actions.
	 * 
	 * The default implementation returns the action which is minimal based on
	 * the comparator returned by the method actionComparator.
	 * 
	 * @param actions
	 *            the candidate actions
	 * @param env
	 *            the current environment
	 * @param selectedActions
	 *            the already selected action
	 * @return the best possible action
	 */
	default Action getBestAction(Set<Action> actions, Env env,
			Set<Action> selectedActions) {
		return actions.stream().min(actionComparator(env, selectedActions))
				.get();
	}

	/**
	 * Provides a comparator of actions, in a given environment and in regard to
	 * a set of already selected actions.
	 * 
	 * The default implementation maps the candidate actions to the
	 * corresponding predicted criticality of the neighbors of the agent and
	 * uses the comparator returned by criticalitiesComparator to compare them.
	 * 
	 * @param env
	 *            the current environment
	 * @param selectedActions
	 *            the already selected action
	 * @return an action comparator
	 */
	default Comparator<Action> actionComparator(Env env,
			Set<Action> selectedActions) {
		return (o1, o2) -> {

			HashSet<Action> testedActions1 = new HashSet<>(selectedActions);
			testedActions1.add(o1);

			HashSet<Action> testedActions2 = new HashSet<>(selectedActions);
			testedActions2.add(o2);

			// convert the actions to predicted neighborhoods, then to
			// criticalities
			List<Criticality> c1 = predictedNeighbors(env, testedActions1)
					.stream()
					.map(n -> predictedCriticality(env, testedActions1, n))
					.collect(Collectors.toList());

			List<Criticality> c2 = predictedNeighbors(env, testedActions2)
					.stream()
					.map(n -> predictedCriticality(env, testedActions2, n))
					.collect(Collectors.toList());

			return criticalitiesComparator().compare(c1, c2);
		};
	}

	/**
	 * Provides a comparator of lists of criticalities.
	 * 
	 * The default implementation sorts criticalities in decreasing order. Then
	 * it compare them using the lexicographical ordering (the first ones are
	 * compared, then if they are equal the second ones and so on).
	 * 
	 * TODO: what is the correct behavior when the two lists have different
	 * sizes ? (see TODOs inside method body)
	 * 
	 * @return a comparator for lists of criticalities
	 */
	default Comparator<List<Criticality>> criticalitiesComparator() {
		return (o1, o2) -> {

			// sort the lists in decreasing order and convert them to iterators
			Iterator<Criticality> l1 = o1.stream()
					.sorted((c1, c2) -> c2.compareTo(c1)).iterator();
			Iterator<Criticality> l2 = o2.stream()
					.sorted((c1, c2) -> c2.compareTo(c1)).iterator();

			while (l1.hasNext()) {

				Criticality c1 = l1.next();

				if (l2.hasNext()) {

					Criticality c2 = l2.next();

					if (!(c1.compareTo(c2) == 0)) {
						return c1.compareTo(c2);
					} else {
						// continue
					}

				} else {
					return 0; // TODO: correct ?
				}

			}

			if (l2.hasNext()) {
				return 0; // TODO: correct ?
			} else {
				return 0;
			}
		};

	}
}
