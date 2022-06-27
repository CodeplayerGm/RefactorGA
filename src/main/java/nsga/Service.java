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
import nsga.objective.AbstractObjectiveFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static nsga.ParameterConfig.overloadRemainThreshold;
import static nsga.PostProcessShowData.outputFrontData;
import static nsga.PostProcessShowData.outputObjectiveData;
import static nsga.PreProcessLoadData.*;

public final class Service {

	/**
	 * this class is never supposed to be instantiated
	 */
	private Service() {}

	static DecimalFormat fourDF = new DecimalFormat("#.0000");
	static DecimalFormat twoDF = new DecimalFormat("#.0");

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

	public static Population combinePopulation(Population parent, Population child, int maxPopulationSize, boolean ifFilterOverload) {
		List<Chromosome> populace = parent.getPopulace();
		populace.addAll(child.getPopulace());

		if (!ifFilterOverload) {
			return new Population(populace);
		}

		// 在给出混合种群前，控制过载个体在总种群中的占比
		// 为了保证种群的增长过程，当种群个体达到最大时才开始淘汰
		int combinedPopulationSize = populace.size();
//		System.out.println("混合种群数量：" + combinedPopulationSize);
		if (combinedPopulationSize > maxPopulationSize * 1.5) {
			List<Chromosome> overloadList = populace.stream()
					.filter(c -> historyRecord.get(c)).collect(Collectors.toList());
			long overloadCount = overloadList.size();
//			System.out.println("当前过载个体占比：" + overloadCount * 1.0 / combinedPopulationSize);
			// 过载个体过多需要淘汰一部分
			if (overloadCount * 1.0 / combinedPopulationSize > overloadRemainThreshold) {
//				System.out.print("总种群：" + populace.size() + " ; 过载个体：" + overloadCount + " ; 淘汰：");
				int cutNum = (int)((overloadCount - populace.size() * overloadRemainThreshold)
						/ (1 - overloadRemainThreshold));
//				System.out.println(cutNum);
				// 淘汰时-考虑效率上的话，通过FIFO进行淘汰
				for (int i = 0; i < cutNum; i++) {
					populace.remove(overloadList.get(i));
				}
			}
		}

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
	 * 输入一个个体chromosome，返回其模块 - 文件id列表 映射map
	 * @param chromosome
	 * @return
	 */
	public static HashMap<Integer, List<Integer>> splitChromosomeToFAFileListMap(Chromosome chromosome) {
		HashMap<Integer, List<Integer>> moduleFilesMap = new HashMap<>();
		for (int i = 0; i < faNums; i++) {
			IntegerAllele allele = (IntegerAllele)chromosome.getGeneticCode().get(i);
			int moduleIndex = allele.getGene();
			if (!moduleFilesMap.containsKey(moduleIndex)) {
				moduleFilesMap.put(moduleIndex, new ArrayList<>());
			}
			List<Integer> fileIdList = clusters.get(i).fileList.stream()
					.map(fs -> allServiceFileList.indexOf(fs)).collect(Collectors.toList());
			moduleFilesMap.get(moduleIndex).addAll(fileIdList);
		}
		return moduleFilesMap;
	}

	/**
	 * 将chromosome拆解为：srv - LIST<file>
	 * 使用的是过载服务的文件列表
	 * @param chromosome
	 * @return
	 */
	public static HashMap<Integer, List<Integer>> splitChromosomeToServiceFileIdMap(Chromosome chromosome) {
		HashMap<Integer, List<Integer>> srvFilesMap = new HashMap<>();
		for (int i = 0; i < overloadServiceFileList.size(); i++) {
			IntegerAllele allele = (IntegerAllele)chromosome.getGeneticCode().get(i);
			int srvIndex = allele.getGene();
			if (!srvFilesMap.containsKey(srvIndex)) {
				srvFilesMap.put(srvIndex, new ArrayList<>());
			}
			srvFilesMap.get(srvIndex).add(i);
		}
		return srvFilesMap;
	}

	/**
	 * 将chromosome拆解为：srv - LIST<file>
	 * 使用的是所有业务服务的文件列表
	 * @param chromosome
	 * @return
	 */
	public static HashMap<Integer, List<Integer>> splitChromosomeToAllFileIdMap(Chromosome chromosome) {
		HashMap<Integer, List<Integer>> srvFilesMap = new HashMap<>();
		for (int i = 0; i < overloadServiceFileList.size(); i++) {
			IntegerAllele allele = (IntegerAllele)chromosome.getGeneticCode().get(i);
			int srvIndex = allele.getGene();
			if (!srvFilesMap.containsKey(srvIndex)) {
				srvFilesMap.put(srvIndex, new ArrayList<>());
			}
			String file = overloadServiceFileList.get(i);
			srvFilesMap.get(srvIndex).add(allServiceFileList.indexOf(file));
		}
		return srvFilesMap;
	}

	/**
	 * 给定一个FA-id列表，返回它的文件列表
	 * @param faList
	 * @return
	 */
	public static List<String> getFileListFromFAList(List<Integer> faList) {
		List<String> moduleFileList = new ArrayList<>();
		for (Integer i : faList) {
			moduleFileList.addAll(clusters.get(i).fileList);
		}
		return moduleFileList;
	}

	/**
	 * 给定一个FA-id列表，返回它的文件id列表
	 * @param faList
	 * @return
	 */
	public static List<Integer> getFileIdListFromFAList(List<Integer> faList) {
		List<Integer> fileIdList = new ArrayList<>();
		for (Integer i : faList) {
			List<Integer> faFileIdList = clusters.get(i).fileList.stream()
					.map(overloadServiceFileList::indexOf).collect(Collectors.toList());
			fileIdList.addAll(faFileIdList);
		}
		return fileIdList;
	}

	/**
	 * 将json字符串写入文件
	 * @param data
	 * @param outputPath
	 */
	public static void writeStringToFile(String data, String outputPath) throws IOException {
		Path path = Paths.get(outputPath);
		if (Files.exists(path)) {
			Files.delete(path);
		}
		Files.createFile(path);
		try {
			Files.write(path, data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(outputPath + " 写入成功");
	}


	/**
	 * 给指定文件追加内容
	 * @param data
	 * @param outputPath
	 */
	public static void appendStringToFile(String data, String outputPath, boolean ifDeleteOrigin) {
		try {
			Path path = Paths.get(outputPath);
			if (Files.exists(path) && ifDeleteOrigin) {
				Files.delete(path);
			}
			if (!Files.exists(path)) {
				Files.createFile(path);
			}
			Files.write(path, data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 把适应度目标值写入文件
	 * @param outputPath
	 * @throws IOException
	 */
	public static void writeObjectivesToFile(String outputPath) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (List<Double> objective : outputObjectiveData) {
			for (Double od : objective) {
				sb.append(od).append(" ");
			}
			sb.append("\n");
		}
		writeStringToFile(sb.toString(), outputPath);
	}

	/**
	 * 把前沿解编码写入文件
	 * @param outputPath
	 * @throws IOException
	 */
	public static void writeFrontToFile(String outputPath) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (List<IntegerAllele> code : outputFrontData) {
			sb.append(code).append("\n");
		}
		writeStringToFile(sb.toString(), outputPath);
	}

	/**
	 * 读取文件中的内容到字符串
	 * @param path
	 * @return
	 */
	public static String readFileAsString(String path) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			FileInputStream is = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stringBuilder.toString().replaceAll("\\/\\*[\\w\\W]*?\\*\\/|\\/\\/.*","");
	}

	/**
	 * 随机生成一个功能原子粒度的个体
	 */
	public static Chromosome randomGenerateFAChromosome() {
		List<IntegerAllele> modularAlleles = new ArrayList<>(faNums);
		for (int i = 0; i < faNums; i++) {
			int randomSrvId = ThreadLocalRandom.current().nextInt(faNums + 1);
			modularAlleles.add(new IntegerAllele(randomSrvId));
		}
		return new Chromosome(new Chromosome(modularAlleles));
	}

	/**
	 * 随机生成一个代码文件粒度的个体
	 */
	public static Chromosome randomGenerateFileChromosome() {
		List<IntegerAllele> modularAlleles = new ArrayList<>(overloadServiceFileList.size());
		for (int i = 0; i < overloadServiceFileList.size(); i++) {
			int randomSrvId = ThreadLocalRandom.current().nextInt(overloadServiceFileList.size() + 1);
			modularAlleles.add(new IntegerAllele(randomSrvId));
		}
		return new Chromosome(new Chromosome(modularAlleles));
	}

	/**
	 * 保留4位小数，直接截断
	 * @param data
	 * @return
	 */
	public static String getFourBitsDoubleString(double data) {
		return fourDF.format(data);
	}

	/**
	 * 保留2位小数，直接截断
	 * @param data
	 * @return
	 */
	public static String getTwoBitsDoubleString(double data) {
		return twoDF.format(data);
	}
}
