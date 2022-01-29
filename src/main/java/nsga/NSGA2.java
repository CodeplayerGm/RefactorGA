/*
 * MIT License
 *
 * Copyright (c) 2019 Debabrata Acharya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nsga;

import com.alibaba.fastjson.JSONObject;
import com.debacharya.nsgaii.datastructure.*;
import nsga.datastructure.Chromosome;
import nsga.datastructure.IntegerAllele;
import nsga.datastructure.Population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static nsga.PreProcessLoadData.clusters;
import static nsga.PreProcessLoadData.historyRecord;

/**
 * An instance of this class is used to run the NSGA-II algorithm. This class defines the core algorithm of NSGA-II.
 * Calling the `run()` method on an instance of `NSGA2` gets the algorithm running. It requires an instance of `Configuration`
 * class to run, which describes all the configuration required for that run. The `Configuration` class is described later in
 * the documentation.
 */
public class NSGA2 {

	public static final int DOMINANT = 1;
	public static final int INFERIOR = 2;
	public static final int NON_DOMINATED = 3;

	private final Configuration configuration;

	/**
	 * creates an instance of `NSGA2` with a default configuration object that provides a default implementation of every plugin
	 * needed by the algorithm to run. While this constructor is not of much use to the user, but this helps run a proof-of-concept
	 * or the algorithm itself with all the default plugins filled in.
	 */
	public NSGA2() {
		this.configuration = new Configuration();
	}

	/**
	 * creates an instance of `NSGA2` by taking a configuration object as parameter.
	 * This will be usually the most useful constructor for `NSGA2` for the user.
	 * The user can configure his / her plugins to the liking and then pass it to the `NSGA2`
	 * constructor for the algorithm to be setup according to the users needs.
	 *
	 * @param	configuration	the configuration object setup for running the algorithm
	 */
	public NSGA2(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Runs the actual NSGA-II core algorithm. It returns the Pareto Front or the last child as a `Population` object.
	 * This needs to be called on an instance of `NSGA2` to run the actual algorithm.
	 *
	 * @return	the final population as the Pareto Front
	 */
	public Population run() {

		if(!this.configuration.isSetup())
			throw new UnsupportedOperationException(Configuration.CONFIGURATION_NOT_SETUP + "\n" + this.configuration.toString());

		Reporter.init(this.configuration);

		Population parent = this.preparePopulation(
			this.configuration.getPopulationProducer().produce(
				this.configuration.getPopulationSize(),
				this.configuration.getChromosomeLength(),
				this.configuration.getGeneticCodeProducer(),
				null
			)
		);

		Population child = this.preparePopulation(
			this.configuration.getChildPopulationProducer().produce(
				parent,
				this.configuration.getCrossover(),
				this.configuration.getMutation(),
				this.configuration.getPopulationSize()
			)
		);

		int generation = 0;
		Reporter.reportGeneration(parent, child, generation, this.configuration.objectives, historyRecord.size());
		while(configuration.getTerminatingCriterion().shouldRun(child, ++generation, this.configuration.getGenerations())) {
			// getChildFromCombinedPopulation方法里完成对混合种群的精英主义筛选
			parent = this.getChildFromCombinedPopulation(
				this.preparePopulation(
					Service.combinePopulation(
						parent,
						child
					)
				)
			);

			child = this.preparePopulation(
				this.configuration.getChildPopulationProducer().produce(
					parent,
					this.configuration.getCrossover(),
					this.configuration.getMutation(),
					this.configuration.getPopulationSize()
				)
			);

			Reporter.reportGeneration(parent, child, generation, this.configuration.objectives, historyRecord.size());
		}

		// 最后输出最后一代的混合种群的第一前沿
		parent = this.getChildFromCombinedPopulation(
				this.preparePopulation(
						Service.combinePopulation(parent, child)));
		List<Chromosome> firstFront = Service.splitPopulationByNonDominatedFront(parent).get(0);

		System.out.println("最后一代全部前沿：" + parent.getPopulace().size());
		System.out.println("第一前沿：" + firstFront.size());
		firstFront = firstFront.stream().filter(ch -> ch.getObjectiveValue(0) > 0).collect(Collectors.toList());
		System.out.println("过滤掉不过载的服务后：" + firstFront.size());
		int sc = 1;
		for (Chromosome chromosome : firstFront) {
			System.out.println("方案" + sc);
			System.out.println("适应度：" + chromosome.getObjectiveValues());
			PostProcessShowData.ObjectiveFile curObjectives = new PostProcessShowData.ObjectiveFile(
					chromosome.getObjectiveValue(0),
					chromosome.getObjectiveValue(1),
					chromosome.getObjectiveValue(2)
			);
			PostProcessShowData.outputObjectives.add(curObjectives);

			List<IntegerAllele> geneticCode = new ArrayList<>();
			chromosome.getGeneticCode().forEach(gc -> geneticCode.add((IntegerAllele) gc));
			System.out.println(geneticCode);

			PostProcessShowData.Program curProgram = new PostProcessShowData.Program("重构方案" + sc);
			curProgram.children = new ArrayList<>();
			List<List<Integer>> groupSet = chromosome.getSortedFAGroupMapValueSet();
			for (int i = 0; i < groupSet.size(); i++) {
				System.out.println("  子服务" + i);
				PostProcessShowData.SubService subService = new PostProcessShowData.SubService("服务" + (i+1), new ArrayList<>());

				for (int j = 0; j < groupSet.get(i).size(); j++) {
					int faId = groupSet.get(i).get(j);
					System.out.println("    FA - " + faId + " fileNum=" + clusters.get(groupSet.get(i).get(j)).fileList.size());
					subService.children.add(new PostProcessShowData.FAFile(
							"FA - " + faId, clusters.get(faId).fileList
					));
				}

				curProgram.children.add(subService);
			}
			PostProcessShowData.outputPrograms.add(curProgram);

			sc ++;
//			System.out.println("方案" + sc++ + " GI数量：" + chromosome.getGeneticCode().size() + " ****************************************************");
//			System.out.println("适应度：" + chromosome.getObjectiveValues());
//			List<GroupItemAllele> solution = new ArrayList<>();
//			chromosome.getGeneticCode().forEach(gc -> solution.add((GroupItemAllele) gc));
//			for (GroupItemAllele gi : solution) {
//				System.out.println("  [ FA数量：" + gi.getGene().size());
//				List<FunctionalAtom> faList = gi.getGene();
//				for (FunctionalAtom fa : faList) {
//					System.out.println("    [");
//					fa.fileList.forEach(fname -> System.out.println("      " + fname));
//					System.out.println("    ]");
//				}
//				System.out.println("  ]");
//			}
		}

		// 把重构方案写入json文件
		try {
			System.out.println(JSONObject.toJSONString(PostProcessShowData.outputPrograms, true));
			System.out.println(JSONObject.toJSONString(PostProcessShowData.outputObjectives, true));
			Service.writeObjString(JSONObject.toJSONString(PostProcessShowData.outputPrograms), PostProcessShowData.outputProgramJson);
			Service.writeObjString(JSONObject.toJSONString(PostProcessShowData.outputObjectives), PostProcessShowData.outputObjectiveJson);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Reporter.terminate(new Population(firstFront), this.configuration.objectives);
		if(Reporter.autoTerminate)
			Reporter.commitToDisk();
		return child;
	}

	/**
	 * This method takes a `Population` object and basically performs all the operations needed to be performed on the parent
	 * population in each generation. It executes the following operations on the population instance in order.
	 *
	 * - It calculates the objective values of all the chromosomes in the population based on the objective functions set
	 *	in the `Configuration` instance.
	 * - It then runs fast non-dominated sort on the population as defined in `NSGA-II paper [DOI: 10.1109/4235.996017] Section III Part A.`
	 * - It then assigns crowding distance to each chromosome.
	 * - Finally, it sorts the chromosomes in the population based on its assigned rank.
	 *
	 * @param	population	the population instance to undergo the above steps
	 * @return				the same population instance that was passed as an argument
	 */
	public Population preparePopulation(Population population) {
		if (population.size() == 0) {
			return population;
		}

		// 计算个体在所有目标上的值，保存在Chromosome的objectiveValues中
		Service.calculateObjectiveValues(population, this.configuration.objectives);
		// 实现非支配排序，统计每个个体的支配列表、被支配数量、前沿序列号
		this.fastNonDominatedSort(population);
		// 计算每个个体的总拥挤距离：每个目标上的拥挤距离之和
		this.crowdingDistanceAssignment(population);
		// 按照非支配前沿来排序
		population.getPopulace().sort(Comparator.comparingInt(Chromosome::getRank));
		return population;
	}

	/**
	 * This method takes a `Population` of size `2N` (_a combination of parent and child, both of size `N`,
	 * according to the originally proposed algorithm_) and returns a new `Population` instance of size `N` by
	 * selecting the first `N` chromosomes from the combined population, based on their rank. If it has to choose `M` chromosomes
	 * of rank `N` such that `M &gt; N`, it then sorts the `M` chromosomes based on their crowding distance.
	 *
	 * @param	combinedPopulation	the combined population of parent and child of size 2N
	 * @return						the new population of size N chosen from the combined population passed as parameter
	 */
	public Population getChildFromCombinedPopulation(Population combinedPopulation) {
		// 该方法需要改写，因为这里的种群数量不是固定的，而是慢慢增长到populationSize的
		// 接下来要做的是保证种群数量不超过populationSize，并基于精英主义 - (非支配前沿 + 拥挤距离)进行筛选
		// 输入的combinedPopulation是已经计算好拥挤距离crowdingDistance，并且按非支配前沿排序好的了rank
		List<List<Chromosome>> frontLists = Service.splitPopulationByNonDominatedFront(combinedPopulation);
		List<Chromosome> offSpring = new ArrayList<>();
		int index = 0;
		for (; index < frontLists.size(); index++) {
			if (offSpring.size() + frontLists.get(index).size() <= this.configuration.getPopulationSize()) {
				offSpring.addAll(frontLists.get(index));
			} else {
				break;
			}
		}

		// 判断是否要对最后一个前沿个体进行拥挤距离排序
		if (offSpring.size() < this.configuration.getPopulationSize() && index < frontLists.size()) {
			int restNum = this.configuration.getPopulationSize() - offSpring.size();
			// 按拥挤距离从大到小排序
			List<Chromosome> chromosomes = frontLists.get(index);
			chromosomes.sort(Comparator.comparingDouble(Chromosome::getCrowdingDistance).reversed());
			// 截取前restNum个
			offSpring.addAll(chromosomes.subList(0, restNum));
		}

		return new Population(offSpring);
	}

	/**
	 * This is an implementation of the fast non-dominated sorting algorithm as defined in the
	 * NSGA-II paper [DOI: 10.1109/4235.996017] Section III Part A.
	 *
	 * @param   population  the population object that needs to undergo fast non-dominated sorting algorithm
	 */
	public void fastNonDominatedSort(Population population) {
		if (population.getPopulace().size() == 0) {
			return;
		}

		List<Chromosome> populace = population.getPopulace();
		for(Chromosome chromosome : populace) {
			chromosome.reset();
		}

		// 两两判断支配关系，支配关系保存在chromosome的dominatedChromosomes、
		for(int i = 0; i < populace.size() - 1; i++) {
			for (int j = i + 1; j < populace.size(); j++)
				switch (this.dominates(populace.get(i), populace.get(j))) {
					case NSGA2.DOMINANT:
						populace.get(i).addDominatedChromosome(populace.get(j));
						populace.get(j).incrementDominatedCount(1);
						break;

					case NSGA2.INFERIOR:
						populace.get(i).incrementDominatedCount(1);
						populace.get(j).addDominatedChromosome(populace.get(i));
						break;

					case NSGA2.NON_DOMINATED:
						break;
				}
			// 第一前沿不被任何个体支配
			if(populace.get(i).getDominatedCount() == 0)
				populace.get(i).setRank(1);
		}

		// 最后一个别忘记判断
		if(population.getLast().getDominatedCount() == 0)
			population.getLast().setRank(1);

		while(Service.populaceHasUnsetRank(populace)) {
			populace.forEach(chromosome -> {
				if(chromosome.getRank() != -1) {
					chromosome.getDominatedChromosomes().forEach(dominatedChromosome -> {
						if(dominatedChromosome.getDominatedCount() > 0) {
							dominatedChromosome.incrementDominatedCount(-1);
							if(dominatedChromosome.getDominatedCount() == 0)
								dominatedChromosome.setRank(chromosome.getRank() + 1);
						}
					});
				}
			});
		}
	}

	/**
	 * This is the implementation of the crowding distance assignment algorithm as defined in the
	 * NSGA-II paper [DOI: 10.1109/4235.996017] Section III Part B.
	 * this ensures diversity preservation.
	 *
	 * @param   population   the population whose crowding distances are to be calculated.
	 */
	public void crowdingDistanceAssignment(Population population) {
		int size = population.size();

		for(int i = 0; i < this.configuration.objectives.size(); i++) {
			int iFinal = i;
			population.getPopulace().sort(Collections.reverseOrder(Comparator.comparingDouble(c -> c.getObjectiveValue(iFinal))));
			Service.normalizeSortedObjectiveValues(population, i);
			population.get(0).setCrowdingDistance(Double.MAX_VALUE);
			population.getLast().setCrowdingDistance(Double.MAX_VALUE);

			double maxNormalizedObjectiveValue = population.selectMaximumNormalizedObjectiveValue(i);
			double minNormalizedObjectiveValue = population.selectMinimumNormalizedObjectiveValue(i);

			for(int j = 1; j < size; j++)
				if(population.get(j).getCrowdingDistance() < Double.MAX_VALUE) {
					double previousChromosomeObjectiveValue = population.get(j - 1).getNormalizedObjectiveValues().get(i);
					double nextChromosomeObjectiveValue = population.get(j + 1).getNormalizedObjectiveValues().get(i);
					double objectiveDifference = nextChromosomeObjectiveValue - previousChromosomeObjectiveValue;
					double minMaxDifference = maxNormalizedObjectiveValue - minNormalizedObjectiveValue;

					population.get(j).setCrowdingDistance(
						Service.roundOff(
							population.get(j).getCrowdingDistance() +
							(objectiveDifference / minMaxDifference),
							4
						)
					);
				}
		}
	}

	/**
	 * This method checks whether one chromosome dominates the other chromosome or not. While the actual domination
	 * logic has been described in the `isDominant(Chromosome, Chromosome)` method, the `dominates(Chromosome, Chromosome)
	 * method returns one among the three values based on whether chromosome1 is dominant over chromosome2,
	 * or is inferior to chromosome2 or whether both of them are non-dominating, by returning
	 * `com.debacharya.nsgaii.NSGA2.DOMINANT`, `com.debacharya.nsgaii.NSGA2.INFERIOR` or
	 * `com.debacharya.nsgaii.NSGA2.NON_DOMINATED` respectively.
	 *
	 * @param	chromosome1	the chromosome to check whether it is dominating, inferior or non-dominated
	 * @param	chromosome2	the chromosome against which chromosome1 is checked
	 * @return				either NSGA2.DOMINANT, NSGA2.INFERIOR or NSGA2.NON_DOMINATED
	 */
	public int dominates(Chromosome chromosome1, Chromosome chromosome2) {
		if(this.isDominant(chromosome1, chromosome2)) return NSGA2.DOMINANT;
		else if(this.isDominant(chromosome2, chromosome1)) return NSGA2.INFERIOR;
		else return NSGA2.NON_DOMINATED;
	}

	/**
	 * This method checks whether chromosome1 dominates chromosome2.
	 * Requires that none of the values of the objective function values of chromosome1 is smaller
	 * than the values of the objective function values of chromosome2 and
	 * at least one of the values of the objective function of chromosome1 is greater than
	 * the corresponding value of the objective function of chromosome2.
	 *
	 * @param   chromosome1     the chromosome that may dominate
	 * @param   chromosome2     the chromosome that may be dominated
	 * @return                  boolean logic whether chromosome1 dominates chromosome2.
	 */
	public boolean isDominant(Chromosome chromosome1, Chromosome chromosome2) {

		boolean atLeastOneIsBetter = false;

		for(int i = 0; i < this.configuration.objectives.size(); i++)
			if(chromosome1.getObjectiveValues().get(i) < chromosome2.getObjectiveValues().get(i))
				return false;
			else if(chromosome1.getObjectiveValues().get(i) > chromosome2.getObjectiveValues().get(i))
				atLeastOneIsBetter = true;

		return atLeastOneIsBetter;
	}
}
