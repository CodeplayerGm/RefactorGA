package nsga.termination;

import nsga.datastructure.Population;

@FunctionalInterface
public interface TerminatingCriterion {
	boolean shouldRun(Population population, int generationCount, int maxGenerations);
}
