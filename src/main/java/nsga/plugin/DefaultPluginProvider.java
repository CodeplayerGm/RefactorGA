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

package nsga.plugin;

import nsga.Service;
import nsga.datastructure.Chromosome;
import nsga.datastructure.GroupItemAllele;
import nsga.datastructure.IntegerAllele;
import nsga.datastructure.Population;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static nsga.PostProcessShowData.outputFrontData;
import static nsga.PreProcessLoadData.*;
import static nsga.objective.ConcernGranularityObjective.ifChromosomeOverload;

public class DefaultPluginProvider {

	public static PopulationProducer defaultPopulationProducer() {
		return (populationSize, chromosomeLength, geneticCodeProducer, fitnessCalculator) -> {
			List<Chromosome> populace = new ArrayList<>();
			for(int i = 0; i < populationSize; i++)
				populace.add(
					new Chromosome(
						geneticCodeProducer.produce(chromosomeLength)
					)
				);
			return new Population(populace);
		};
	}

	public static ChildPopulationProducer defaultChildPopulationProducer() {
		return (parentPopulation, crossover, mutation, populationSize) -> {
			List<Chromosome> populace = new ArrayList<>();
			while(populace.size() < populationSize)
				if((populationSize - populace.size()) == 1)
					populace.add(
						mutation.perform(
							Service.crowdedBinaryTournamentSelection(parentPopulation)
						)
					);
				else
					for(Chromosome chromosome : crossover.perform(parentPopulation))
						populace.add(mutation.perform(chromosome));

			return new Population(populace);
		};
	}

	/**
	 * 初始化种群
	 * @return
	 */
	public static PopulationProducer refactorInitPopulationProducer() {
		return (populationSize, chromosomeLength, geneticCodeProducer, fitnessCalculator) -> {
			List<Chromosome> populace = new ArrayList<>();
			GroupItemAllele initialGroup = new GroupItemAllele(new ArrayList<>());
			clusters.forEach(fa -> initialGroup.getGene().add(fa));
			// 初始种群仅有这一个个体
			Chromosome initialIndividual = new Chromosome(new ArrayList<GroupItemAllele>() {{ add(initialGroup); }});
			populace.add(initialIndividual);
			return new Population(populace);
		};
	}

	/**
	 * 交叉变异，执行单亲交叉，获得children种群。以当前的population作为父种群，按照单亲交叉生成孩子种群
	 * @return
	 */
	public static ChildPopulationProducer refactorGenerateChildrenProducer() {
		return (parentPopulation, crossover, mutation, populationSize) -> {
			// 本算法没有变异操作，所以mutation是用不到的
			// 单亲交叉的逻辑基本已经在SingleCrossover中实现了
			return new Population(crossover.perform(parentPopulation));
		};
	}

	/**
	 * 功能原子粒度 - 初始化种群
	 * @return
	 */
	public static PopulationProducer faInitPopulationProducer() {
		// 接口里的参数是没用到的
		return (populationSize, chromosomeLength, geneticCodeProducer, fitnessCalculator) -> {
			// 初始个体：每个FA都在一个模块内
			List<IntegerAllele> modularAlleles = new ArrayList<>(faNums);
			for (int i = 0; i < faNums; i++) {
				modularAlleles.add(new IntegerAllele(i));
			}
			Chromosome initIndividual = new Chromosome(new Chromosome(modularAlleles));
			outputFrontData.add(modularAlleles);
			historyRecord.put(initIndividual, ifChromosomeOverload(initIndividual));
			List<Chromosome> populace = new ArrayList<Chromosome>() {{ add(initIndividual); }};
			return new Population(populace);
		};
	}

	/**
	 * 功能原子粒度、代码文件粒度
	 * 交叉-变异-过载比例控制，种群更新逻辑
	 * @return
	 */
	public static ChildPopulationProducer childrenProducer(float mutateProbability) {
		return (parentPopulation, crossover, mutation, populationSize) -> {
			List<Chromosome> populace;
			// 交叉产生的新后代
			populace = crossover.perform(parentPopulation);

			// 变异产生的新后代
			int mutateNum = (int) (parentPopulation.getPopulace().size() * mutateProbability);
			mutateNum = Math.max(mutateNum, 1);
			HashSet<Integer> mutateParents = new HashSet<>();
			for (int i = 0; i < mutateNum; i++) {
				int id;
				do {
					id = ThreadLocalRandom.current().nextInt(0, parentPopulation.getPopulace().size());
				}  while (mutateParents.contains(id));
				mutateParents.add(id);
			}

			for (int id : mutateParents) {
				Chromosome child = mutation.perform(parentPopulation.get(id));
				if (!historyRecord.containsKey(child)) {
					historyRecord.put(child, ifChromosomeOverload(child));
					populace.add(child);
				}
			}

			return new Population(populace);
		};
	}

	/**
	 * 功能原子、代码文件粒度
	 * 随机搜索种群更新逻辑
	 * @param mutateProbability
	 * @return
	 */
	public static ChildPopulationProducer randomChildrenProducer(float mutateProbability) {
		return (parentPopulation, crossover, mutation, populationSize) -> {
			List<Chromosome> populace;
			// 交叉产生的新后代
			populace = crossover.perform(parentPopulation);

			// 变异产生的新后代
			int mutateNum = (int) (parentPopulation.getPopulace().size() * mutateProbability);
			mutateNum = Math.max(mutateNum, 1);
			Chromosome emptyChromosome = new Chromosome(parentPopulation.getPopulace().get(0));
			for (int i = 0; i < mutateNum; i++) {
				// 随机搜索的变异随便输入一个个体即可，返回的是随机生成的个体
				Chromosome child = mutation.perform(emptyChromosome);
				if (!historyRecord.containsKey(child)) {
					historyRecord.put(child, ifChromosomeOverload(child));
					populace.add(child);
				}
			}

			return new Population(populace);
		};
	}

	/**
	 * 代码文件粒度 - 初始化种群
	 * @return
	 */
	public static PopulationProducer fileInitPopulationProducer() {
		// 接口里的参数是没用到的
		return (populationSize, chromosomeLength, geneticCodeProducer, fitnessCalculator) -> {
			// 初始个体：每个代码文件都在一个模块内
			int overloadSrvFiles = overloadServiceFileList.size();
			List<IntegerAllele> modularAlleles = new ArrayList<>(overloadSrvFiles);
			for (int i = 0; i < overloadSrvFiles; i++) {
				modularAlleles.add(new IntegerAllele(i));
			}
			Chromosome initIndividual = new Chromosome(new Chromosome(modularAlleles));
			historyRecord.put(initIndividual, ifChromosomeOverload(initIndividual));
			List<Chromosome> populace = new ArrayList<Chromosome>() {{ add(initIndividual); }};
			return new Population(populace);
		};
	}

//	/**
//	 * 代码文件粒度：交叉变异，种群更新逻辑
//	 * @param mutateProbability
//	 * @return
//	 */
//	public static ChildPopulationProducer fileGenerateChildrenProducer(float mutateProbability) {
//		return (parentPopulation, crossover, mutation, populationSize) -> {
//			List<Chromosome> populace;
//			// 交叉产生的新后代
//			populace = crossover.perform(parentPopulation);
//
//			// 变异产生的新后代
//			int mutateNum = (int) (parentPopulation.getPopulace().size() * mutateProbability);
//			mutateNum = Math.max(mutateNum, 1);
//			HashSet<Integer> mutateParents = new HashSet<>();
//			for (int i = 0; i < mutateNum; i++) {
//				int id;
//				do {
//					id = ThreadLocalRandom.current().nextInt(0, parentPopulation.getPopulace().size());
//				}  while (mutateParents.contains(id));
//				mutateParents.add(id);
//			}
//
//			for (int id : mutateParents) {
//				Chromosome child = mutation.perform(parentPopulation.get(id));
//				if (!historyRecord.containsKey(child)) {
//					historyRecord.put(child, ifChromosomeOverload(child));
//					populace.add(child);
//				}
//			}
//
//			// 在给出混合种群前，控制过载个体在总种群中的占比
//			// 为了保证种群的增长过程，当种群个体达到最大时才开始淘汰
//			if (populace.size() >= populationSize * 2) {
//				List<Chromosome> overloadList = populace.stream()
//						.filter(c -> historyRecord.get(c)).collect(Collectors.toList());
//				// 过载个体过多需要淘汰一部分
//				System.out.println("当前过载个体占比：" + overloadList.size() * 1.0 / populace.size());
//				if (overloadList.size() * 1.0 / populace.size() > overloadRemainThreshold) {
//					System.out.print("总种群：" + populace.size() + " ; 过载个体：" + overloadList.size() + " ; 淘汰：");
//					int cutNum = (int)((overloadList.size() - populace.size() * overloadRemainThreshold)
//							/ (1 - overloadRemainThreshold));
//					System.out.println(cutNum);
//					// 淘汰时-考虑效率上的话，通过FIFO进行淘汰
//					for (int i = 0; i < cutNum; i++) {
//						populace.remove(overloadList.get(i));
//					}
//				}
//			}
//
//			return new Population(populace);
//		};
//	}
}
