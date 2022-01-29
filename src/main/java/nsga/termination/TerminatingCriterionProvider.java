package nsga.termination;

import static nsga.PreProcessLoadData.historyRecord;

public class TerminatingCriterionProvider {

	/*
	 * The most commonly used stopping criterion in Evolutionary Multi-objective Algorithms is an a priori fixed number of generations
	 * (or evaluations).
	 */
	public static TerminatingCriterion fixedTerminatingCriterion() {
		return (population, generationCount, maxGenerations) -> (generationCount <= maxGenerations);
	}

	/**
	 * 重写的是shouldRun方法。返回为true才继续遗传
	 * @return
	 */
	public static TerminatingCriterion refactorTerminatingCriterion(int maxPopulationSize, int maxRecordSize, double majority) {
		return ((population, generationCount, maxGenerations) -> {
			if (generationCount > maxGenerations) {
				return false;
			}
			if (historyRecord.size() > maxRecordSize) {
				return false;
			}
			return population.getPopulace().size() != maxPopulationSize
					|| !(population.getPopulace().stream().filter(p -> p.getRank() == 1).count() >= population.getPopulace().size() * majority);
		});
	}
}
