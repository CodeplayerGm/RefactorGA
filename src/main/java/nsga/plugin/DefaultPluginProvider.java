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

import nsga.PreProcessLoadData;
import nsga.Service;
import nsga.datastructure.Chromosome;
import nsga.datastructure.GroupItemAllele;
import nsga.datastructure.IntegerAllele;
import nsga.datastructure.Population;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
			PreProcessLoadData.clusters.forEach(fa -> initialGroup.getGene().add(fa));
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
	 * 新基因型 - 初始化种群
	 * @return
	 */
	public static PopulationProducer refactorInitPopulationProducerEncode() {
		// 参数用不到，从文件读取
		return (populationSize, chromosomeLength, geneticCodeProducer, fitnessCalculator) -> {
			List<Chromosome> populace = new ArrayList<>();
//			List<IntegerAllele> modularAlleles = new ArrayList<>(faNums);
//			// 初始个体的所有FA都在一个模块里
//			for (int i = 0; i < faNums; i++) {
//				modularAlleles.add(new IntegerAllele(0));
//			}
//			populace.add(new Chromosome(modularAlleles));


			// 生成最大种群的随机分配
//			for (int i = 0; i < populationSize; i++) {
//				List<IntegerAllele> modularAlleles = new ArrayList<>(faNums);
//
//
//				for (int j = 0; j < faNums; j++) {
//					int index = ThreadLocalRandom.current().nextInt(0, faNums);
//					modularAlleles.add(new IntegerAllele(0));
//				}
//			}

			// 初始一个每个都在一个模块内
			List<IntegerAllele> modularAlleles = new ArrayList<>(PreProcessLoadData.faNums);
			// 初始个体的所有FA都在一个模块里
			for (int i = 0; i < PreProcessLoadData.faNums; i++) {
				modularAlleles.add(new IntegerAllele(i));
			}
			populace.add(new Chromosome(modularAlleles));

			return new Population(populace);
		};
	}

	/**
	 * 新基因型种群交叉变异，种群更新逻辑
	 * @return
	 */
	public static ChildPopulationProducer refactorGenerateChildrenProducerEncode(float mutateProbability) {
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
				if (!PreProcessLoadData.historyRecord.contains(child)) {
					PreProcessLoadData.historyRecord.add(child);
					populace.add(child);
				}
			}

			return new Population(populace);
		};
	}
}
