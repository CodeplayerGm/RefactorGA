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

import nsga.datastructure.*;
import nsga.objectivefunction.AbstractObjectiveFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static nsga.PreProcessLoadData.clusters;
import static nsga.PreProcessLoadData.faNums;

public final class Service {

	/**
	 * this class is never supposed to be instantiated
	 */
	private Service() {}

	public static Chromosome crowdedBinaryTournamentSelection(Population population) {

		Chromosome participant1 = population.getPopulace().get(ThreadLocalRandom.current().nextInt(0, population.size()));
		Chromosome participant2 = population.getPopulace().get(ThreadLocalRandom.current().nextInt(0, population.size()));

		if(participant1.getRank() < participant2.getRank())
			return participant1;
		else if(participant1.getRank() == participant2.getRank()) {
			if(participant1.getCrowdingDistance() > participant2.getCrowdingDistance())
				return participant1;
			else if(participant1.getCrowdingDistance() < participant2.getCrowdingDistance())
				return participant2;
			else return ThreadLocalRandom.current().nextBoolean() ? participant1 : participant2;
		} else return participant2;
	}

	/**
	 * 随机选择单亲个体
	 * @param population
	 * @return
	 */
	public static Chromosome singleRandomSelection(Population population) {
		return population.getPopulace().get(ThreadLocalRandom.current().nextInt(0, population.size()));
	}

	public static Population combinePopulation(Population parent, Population child) {
		List<Chromosome> populace = parent.getPopulace();
		populace.addAll(child.getPopulace());
		return new Population(populace);
	}

	public static void sortFrontWithCrowdingDistance(List<Chromosome> populace, int front) {

		int frontStartIndex = -1;
		int frontEndIndex = -1;
		List<Chromosome> frontToSort = new ArrayList<>();

		for(int i = 0; i < populace.size(); i++)
			if(populace.get(i).getRank() == front) {
				frontStartIndex = i;
				break;
			}

		if((frontStartIndex == -1) || (frontStartIndex == (populace.size() - 1)) || (populace.get(frontStartIndex + 1).getRank() != front))
			return;

		for(int i = frontStartIndex + 1; i < populace.size(); i++)
			if(populace.get(i).getRank() != front) {
				frontEndIndex = i - 1;
				break;
			} else if(i == (populace.size() - 1))
				frontEndIndex = i;

		for(int i = frontStartIndex; i <= frontEndIndex; i++)
			frontToSort.add(populace.get(i));

		frontToSort.sort(Collections.reverseOrder(Comparator.comparingDouble(Chromosome::getCrowdingDistance)));

		for(int i = frontStartIndex; i <= frontEndIndex; i++)
			populace.set(i, frontToSort.get(i - frontStartIndex));
	}

	public static void calculateObjectiveValues(Chromosome chromosome, List<AbstractObjectiveFunction> objectives) {
		for(int i = 0; i < objectives.size(); i++)
			chromosome.addObjectiveValue(i, objectives.get(i).getValue(chromosome));
	}

	public static void calculateObjectiveValues(Population population, List<AbstractObjectiveFunction> objectives) {
		for(Chromosome chromosome : population.getPopulace())
			Service.calculateObjectiveValues(chromosome, objectives);
	}

	public static void normalizeSortedObjectiveValues(Population population, int objectiveIndex) {

		double actualMin = population.get(0).getObjectiveValues().get(objectiveIndex);
		double actualMax = population.getLast().getObjectiveValues().get(objectiveIndex);

		for(Chromosome chromosome : population.getPopulace())
			chromosome.setNormalizedObjectiveValue(
					objectiveIndex,
					Service.minMaxNormalization(
							chromosome.getObjectiveValues().get(objectiveIndex),
							actualMin,
							actualMax
					)
			);
	}

	public static double getNormalizedGeneticCodeValue(List<BooleanAllele> geneticCode,
													   double actualMin,
													   double actualMax,
													   double normalizedMin,
													   double normalizedMax) {
		return Service.minMaxNormalization(
				Service.convertBinaryGeneticCodeToDecimal(geneticCode),
				actualMin,
				actualMax,
				normalizedMin,
				normalizedMax
		);
	}

	/**
	 * this method decodes the genetic code that is represented as a string of binary values, converted into
	 * decimal value.
	 *
	 * @param   geneticCode     the genetic code as an array of Allele. Refer Allele.java for more information
	 * @return                  the decimal value of the corresponding binary string.
	 */
	public static double convertBinaryGeneticCodeToDecimal(final List<BooleanAllele> geneticCode) {

		double value = 0;
		StringBuilder binaryString = new StringBuilder();

		for(BooleanAllele bit : geneticCode) binaryString.append(bit.toString());

		for(int i = 0; i < binaryString.length(); i++)
			if(binaryString.charAt(i) == '1')
				value += Math.pow(2, binaryString.length() - 1 - i);

		return value;
	}

	public static boolean isInGeneticCode(List<ValueAllele> geneticCode, double value) {

		for(ValueAllele allele : geneticCode)
			if(allele.getGene().equals(value))
				return true;

		return false;
	}

	public static boolean isInGeneticCode(List<IntegerAllele> geneticCode, int value) {

		for(IntegerAllele allele : geneticCode)
			if(allele.getGene().equals(value))
				return true;

		return false;
	}

	public static boolean populaceHasUnsetRank(List<Chromosome> populace) {
		for(Chromosome chromosome : populace)
			if(chromosome.getRank() == -1)
				return true;
		return false;
	}

	/**
	 * an implementation of min-max normalization
	 *
	 * @param   value   		the value that is to be normalized
	 * @param   actualMin   	the actual minimum value of the original scale
	 * @param   actualMax   	the actual maximum value of the original sclae
	 * @param   normalizedMin   the normalized minimum value of the target scale
	 * @param   normalizedMax   the normalized maximum value of the target scale
	 * @return          the normalized value
	 */
	public static double minMaxNormalization(double value,
											  double actualMin,
											  double actualMax,
											  double normalizedMin,
											  double normalizedMax) {
		return (((value - actualMin) / (actualMax - actualMin)) * (normalizedMax - normalizedMin)) + normalizedMin;
	}

	public static double minMaxNormalization(double value, double actualMin, double actualMax) {
		return Service.minMaxNormalization(value, actualMin, actualMax, 0, 1);
	}

	public static double roundOff(double value, double decimalPlace) {
		if(value == Double.MAX_VALUE || value == Double.MIN_VALUE) return value;
		decimalPlace = Math.pow(10, decimalPlace);
		return (Math.round(value * decimalPlace) / decimalPlace);
	}

	public static double percent(double x, double y) {
		return Math.floor((x / 100.0) * y);
	}

	public static List<Integer> generateUniqueRandomNumbers(int count) {
		return Service.generateUniqueRandomNumbers(count, 0, count);
	}

	public static List<Integer> generateUniqueRandomNumbers(int count, int bound) {
		return Service.generateUniqueRandomNumbers(count, 0, bound);
	}

	public static List<Integer> generateUniqueRandomNumbers(int count, int origin, int bound) {
		if(bound <= origin)
			throw new UnsupportedOperationException("Origin cannot be more than or equal to the Bound");
		if(count > (bound - origin))
			throw new UnsupportedOperationException("Count for randomly generated unique numbers cannot be more " +
													"than the total possible bounded range");
		List<Integer> range = IntStream.range(origin, bound).boxed().collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(range);
		return range.subList(0, count);
	}

	/**
	 * 输入一个已排序的种群，按不同的非支配前沿分隔成若干个列表，方便精英主义筛选
	 * @param sortedPopulation
	 * @return
	 */
	public static List<List<Chromosome>> splitPopulationByNonDominatedFront(Population sortedPopulation) {
		int rank = 1;
		List<List<Chromosome>> result = new ArrayList<>();
		List<Chromosome> list = new ArrayList<>();
		for (Chromosome chromosome : sortedPopulation.getPopulace()) {
			if (chromosome.getRank() == rank) {
				list.add(chromosome);
			} else {
				result.add(new ArrayList<>(list));
				list = new ArrayList<>();
				rank ++;
			}
		}

		if (!list.isEmpty()) {
			result.add(new ArrayList<>(list));
		}
		return result;
	}

	/**
	 * 新基因型 - 解析出每个子服务下的FA-id列表
	 * @param chromosome
	 * @return
	 */
	public static HashMap<Integer, List<Integer>> splitModularAlleleToServiceFAMap(Chromosome chromosome) {
		HashMap<Integer, List<Integer>> moduleFAMap = new HashMap<>();
		for (int i = 0; i < faNums; i++) {
			IntegerAllele allele = (IntegerAllele)chromosome.getGeneticCode().get(i);
			int moduleIndex = allele.getGene();
			if (!moduleFAMap.containsKey(moduleIndex)) {
				moduleFAMap.put(moduleIndex, new ArrayList<>());
			}
			moduleFAMap.get(moduleIndex).add(i);
		}
		return moduleFAMap;
	}

	/**
	 * 给定一个FA-id列表，返回它的文件列表
	 * @param faList
	 * @return
	 */
	public static List<String> getModuleFileList(List<Integer> faList) {
		List<String> moduleFileList = new ArrayList<>();
		for (Integer i : faList) {
			moduleFileList.addAll(clusters.get(i).fileList);
		}
		return moduleFileList;
	}

	/**
	 * 将json字符串写入文件
	 * @param jsonStr
	 * @param outputPath
	 */
	public static void writeObjString(String jsonStr, String outputPath) throws IOException {
		Path path = Paths.get(outputPath);
		if (Files.exists(path)) {
			Files.delete(path);
		}
		Files.createFile(path);
		System.out.println(outputPath + " 创建成功");
		try {
			Files.write(path, jsonStr.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(outputPath + " 写入成功");
	}
}
