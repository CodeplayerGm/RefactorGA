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

package nsga.datastructure;

import nsga.Service;

import java.util.*;
import java.util.stream.Collectors;

public class Chromosome {

	private final List<Double> objectiveValues;
	private final List<Double> normalizedObjectiveValues;
	private final List<AbstractAllele> geneticCode;
	private  List<Chromosome> dominatedChromosomes;
	private double crowdingDistance = 0;
	/**
	 * 被多少个个体支配
	 */
	private int dominatedCount = 0;
	private double fitness = Double.MIN_VALUE;
	/**
	 * 非支配前沿序号，从1开始
	 */
	private int rank = -1;

	public Chromosome(List<? extends AbstractAllele> geneticCode) {

		this.geneticCode = new ArrayList<>();
		this.objectiveValues = new ArrayList<>();
		this.normalizedObjectiveValues = new ArrayList<>();
		this.dominatedChromosomes = new ArrayList<>();

		for(AbstractAllele allele : geneticCode)
			this.geneticCode.add(allele.getCopy());
	}

	public Chromosome(Chromosome chromosome) {

		this(chromosome.geneticCode);

		for(int i = 0; i < chromosome.objectiveValues.size(); i++)
			this.objectiveValues.add(i, chromosome.objectiveValues.get(i));

		this.crowdingDistance = chromosome.crowdingDistance;
		this.dominatedCount = chromosome.dominatedCount;
		this.fitness = chromosome.fitness;
		this.rank = chromosome.rank;
	}

	public void addDominatedChromosome(Chromosome chromosome) {
		this.dominatedChromosomes.add(chromosome);
	}

	public void incrementDominatedCount(int incrementValue) {
		this.dominatedCount += incrementValue;
	}

	public List<Chromosome> getDominatedChromosomes() {
		return dominatedChromosomes;
	}

	public void setDominatedChromosomes(List<Chromosome> dominatedChromosomes) {
		this.dominatedChromosomes = dominatedChromosomes;
	}

	public List<Double> getObjectiveValues() {
		return objectiveValues;
	}

	public double getObjectiveValue(int index) {
		if(index > (this.objectiveValues.size() - 1))
			throw new UnsupportedOperationException("Chromosome does not have " + (index + 1) + " objectives!");
		return this.objectiveValues.get(index);
	}

	public double getAvgObjectiveValue() {
		return this.objectiveValues.stream().mapToDouble(Double::doubleValue).summaryStatistics().getAverage();
	}

	public void addObjectiveValue(int index, double value) {

		double roundedValue = Service.roundOff(value, 4);

		if(this.getObjectiveValues().size() <= index) this.objectiveValues.add(index, roundedValue);
		else this.objectiveValues.set(index, roundedValue);
	}

	public List<Double> getNormalizedObjectiveValues() {
		return this.normalizedObjectiveValues;
	}

	public void setNormalizedObjectiveValue(int index, double value) {

		if(this.getNormalizedObjectiveValues().size() <= index) this.normalizedObjectiveValues.add(index, value);
		else this.normalizedObjectiveValues.set(index, value);
	}

	public List<AbstractAllele>  getGeneticCode() {
		return geneticCode;
	}

	public double getCrowdingDistance() {
		return crowdingDistance;
	}

	public void setCrowdingDistance(double crowdingDistance) {
		this.crowdingDistance = crowdingDistance;
	}

	public int getDominatedCount() {
		return dominatedCount;
	}

	public void setDominatedCount(int dominationCount) {
		this.dominatedCount = dominationCount;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getLength() {
		return this.geneticCode.size();
	}

	public AbstractAllele getAllele(int index) {
		return this.geneticCode.get(index);
	}

	public void setAllele(int index, AbstractAllele allele) {
		this.geneticCode.set(index, allele.getCopy());
	}

	public Chromosome getCopy() {
		return new Chromosome(this);
	}

	public void reset() {
		this.dominatedCount = 0;
		this.rank = -1;
		this.dominatedChromosomes = new ArrayList<>();
	}

	public boolean identicalGeneticCode(Chromosome chromosome) {
		if(this.geneticCode.size() != chromosome.getLength())
			return false;
		if(!this.geneticCode.get(0).getClass().equals(chromosome.getAllele(0).getClass()))
			return false;
		for(int i = 0; i < this.geneticCode.size(); i++)
			if(!this.geneticCode.get(i).getGene().equals(chromosome.getAllele(i).getGene()))
				return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		// 旧的基因型设计
//		if (obj instanceof Chromosome) {
//			Chromosome contrast = (Chromosome) obj;
//			if (this.getGeneticCode().size() != contrast.getGeneticCode().size()) {
//				return false;
//			}
//
//			List<GroupItemAllele> giList = new ArrayList<>();
//			this.getGeneticCode().forEach(gc -> giList.add((GroupItemAllele) gc));
//			List<GroupItemAllele> contrastList = new ArrayList<>();
//			contrast.getGeneticCode().forEach(gc -> contrastList.add((GroupItemAllele) gc));
//
////			System.out.println("equal length: " + giList.size() + " - " + contrastList.size());
//			for (GroupItemAllele gi : giList) {
//				if (!contrastList.contains(gi)) {
////					System.out.println("duplicated chromosome !!!!!!!!!!");
//					return false;
//				}
//			}
//			return true;
//		}
//		return false;

		// 新的基因型设计
		if (obj instanceof Chromosome) {
			Chromosome contrast = (Chromosome) obj;
			if (this.getGeneticCode().size() != contrast.getGeneticCode().size()) {
				return false;
			}

			// 陷阱 如 ：0011和1122实际上是相同的
			// 分组排序
			List<List<Integer>> valueSet = getSortedGroupMapValueSet();
			List<List<Integer>> contrastSet = contrast.getSortedGroupMapValueSet();

			// 拼接id字符串
			StringBuilder s1 = new StringBuilder();
			for (List<Integer> group : valueSet) {
				for (Integer faId : group) {
					s1.append(faId);
				}
				// group的分隔符
				s1.append("#");
			}

			StringBuilder s2 = new StringBuilder();
			for (List<Integer> group : contrastSet) {
				for (Integer faId : group) {
					s2.append(faId);
				}
				// group的分隔符
				s2.append("#");
			}
			return s1.toString().equals(s2.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		// 旧的基因型设计
		// 返回一个固定值，强行执行equals
//		return 0;

		// 新基因型设计
		List<List<Integer>> valueSet = getSortedGroupMapValueSet();
		StringBuilder s1 = new StringBuilder();
		for (List<Integer> group : valueSet) {
			for (Integer faId : group) {
				s1.append(faId);
			}
			// group的分隔符
			s1.append("#");
		}
		return s1.toString().hashCode();
	}

	/**
	 * 功能原子/代码文件粒度：将基因编码转换成每个服务下的fa列表，服务按照功能原子的数量进行降序排序
	 * @return
	 */
	public List<List<Integer>> getSortedGroupMapValueSet() {
		// key - moduleId，value：fa/file id list
		HashMap<Integer, List<Integer>> groupMap = new HashMap<>();
		for (int i = 0; i < geneticCode.size(); i++) {
			Integer moduleId = ((IntegerAllele) geneticCode.get(i)).getGene();
			List<Integer> idList = groupMap.get(moduleId);
			if (idList == null) {
				idList = new ArrayList<>();
				idList.add(i);
				groupMap.put(moduleId, idList);
			} else {
				idList.add(i);
			}
		}

		// 按fa id list的长度排序的keyset
		return groupMap.entrySet().stream()
				.sorted((e1, e2) -> {
					if (e2.getValue().size() > e1.getValue().size()) {
						return 1;
					} else if (e2.getValue().size() < e1.getValue().size()) {
						return -1;
					} else {
						// group长度相同时，按内部元素排序
						e1.getValue().sort(Comparator.comparingInt(n -> n));
						e2.getValue().sort(Comparator.comparingInt(n -> n));
						return e1.getValue().get(0) - e2.getValue().get(0);
					}
				})
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		StringBuilder response = new StringBuilder("Objective values: ");
		for(double value : this.objectiveValues)
			response.append("[").append(value).append("]")
					.append(" | Rank: ").append(this.rank).append(" ;;; ");
//					.append(" | Crowding Distance: ").append(this.crowdingDistance);

		return response.toString();
	}
}
