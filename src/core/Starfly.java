package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Starfly<Env, Action extends Function<Env, Env>, Criticality extends Comparable<Criticality>, Agent extends Starfly<Env, Action, Criticality, Agent>>
		extends Firefly<Env, Action, Criticality, Agent> {

	/**
	 * 
	 * @return the depth of the decision tree explored by the agent during its
	 *         decision process (1 being an action from the agent, then an
	 *         action from its neighbors)
	 */
	public int searchDepth();

	default Set<Action> decision(Env env, int searchDepth) {
		Set<Action> candidateActions = possibleActions(env);
		Set<Action> selectedActions = new HashSet<>();

		boolean stop = false;

		while (!candidateActions.isEmpty() && !stop) {
			Action bestAction = getBestAction(candidateActions, env,
					selectedActions, searchDepth);

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

	default Set<Action> decision(Env env) {
		return decision(env, searchDepth());
	}

	default Action getBestAction(Set<Action> actions, Env env,
			Set<Action> selectedActions, int searchDepth) {

		if (searchDepth == 0) {
			return getBestAction(actions, env, selectedActions);
		} else {

			Map<Action, List<Criticality>> pred = new HashMap<>();

			// recurse into the decision tree
			for (Action a : actions) {

				// If I choose this action...
				Set<Action> sa = new HashSet<Action>(selectedActions);
				sa.add(a);

				final Env newEnv = a.apply(env);
				List<Agent> neigh = this.predictedNeighbors(newEnv, sa);

				// neighbors actions anticipation
				Iterator<Action> it = neigh
						.stream()
						.<Action> flatMap(
								n -> n.decision(newEnv, searchDepth - 1)
										.stream()).iterator();

				Env predEnv = newEnv;
				while (it.hasNext()) {
					predEnv = it.next().apply(predEnv);
				}

				// now we have the anticipated env taking in account the
				// predicted behavior of the neighbors
				final Env finalPredEnv = predEnv;

				// recursive call with lower depth
				Set<Action> predDecision = decision(predEnv, searchDepth - 1);

				pred.put(
						a,
						predictedNeighbors(finalPredEnv, predDecision)
								.stream()
								.map(n -> predictedCriticality(finalPredEnv,
										selectedActions, n))
								.collect(Collectors.toList()));

			}

			return pred
					.keySet()
					.stream()
					.min((a1, a2) -> criticalitiesComparator().compare(
							pred.get(a1), pred.get(a2))).get();

		}

	}
}
