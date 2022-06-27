package nsga.termination;

import static nsga.PreProcessLoadData.historyRecord;
import static nsga.Service.getFourBitsDoubleString;

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
	public static TerminatingCriterion refactorTerminatingCriterion(int maxPopulationSize, int maxRecordSize) {
		return ((parentPopulation, generationCount, maxGenerations) -> {
			return generationCount <= maxGenerations && historyRecord.size() <= maxRecordSize;
//			long firstFrontSize = parentPopulation.getPopulace().stream().filter(p -> p.getRank() == 1).count();
//			System.out.println("parent size: " + parentPopulation.getPopulace().size() + " ; maxPopulation: " + maxPopulationSize);
//			System.out.println("first front: " + firstFrontSize + " ; percent: " + getFourBitsDoubleString(firstFrontSize*1.0 / maxPopulationSize));
//			return parentPopulation.getPopulace().size() != maxPopulationSize
//					|| !(firstFrontSize >= parentPopulation.getPopulace().size() * majority);
		});
	}
}
