import nsga.crossover.FARandomCrossover;
import nsga.crossover.FaSingleCrossover;
import nsga.crossover.FileRandomCrossover;
import nsga.crossover.FileSingleCrossover;
import nsga.mutation.FaModularMutation;
import nsga.mutation.FARandomMutation;
import nsga.mutation.FileModularMutation;
import nsga.mutation.FileRandomMutation;
import nsga.objective.ObjectiveProvider;
import nsga.plugin.DefaultPluginProvider;
import nsga.runbody.*;
import nsga.termination.TerminatingCriterionProvider;

import static nsga.ParameterConfig.*;
import static nsga.PreProcessLoadData.*;
import static nsga.PostProcessShowData.*;
import static nsga.Service.appendStringToFile;
import static nsga.Service.getFourBitsDoubleString;

public class RefactorTest {

    private static void  faRefactorTest(Configuration configuration, int startIter) {
        // 功能原子粒度重构方法，初始化种群
        configuration.setPopulationProducer(DefaultPluginProvider.faInitPopulationProducer());
        configuration.objectives = ObjectiveProvider.provideFAStructuralAndSemanticAndMoJoFM();
        // 交叉操作
        configuration.setChromosomeLength(faNums);
        configuration.setCrossover(new FaSingleCrossover(null, crossoverProb));
        // 变异操作 - mutationProbability是在refactorGenerateChildrenProducerEncode方法中用的
        configuration.setMutation(new FaModularMutation(mutationProb, breakProb));
        // 子代更新方法
        configuration.setChildPopulationProducer(DefaultPluginProvider.childrenProducer(mutationProb));
        // 最大代数，与终止条件有关
        configuration.setGenerations(faMaxGeneration);
        // 原意是种群的固定数量，但在本研究中，种群数量是从1开始的，次值应该是种群上限
        configuration.setPopulationSize(faMaxPopulationSize);
        // 遗传终止方法
        configuration.setTerminatingCriterion(TerminatingCriterionProvider.refactorTerminatingCriterion(
                faMaxPopulationSize, faMaxRecord));

        FANsga2 faNsga2 = new FANsga2(configuration);
        for (int i = startIter; i <= experimentTimes; i++) {
            experimentInitDataStructure("fa");

            long start = System.currentTimeMillis();
            faNsga2.run(i, outputFAProgramJson, outputFAObjectiveTxt, outputFAFrontTxt, outputFACostTxt, true);
            long end = System.currentTimeMillis();

            appendStringToFile("\ntime:" + (end - start) / 1000.0, outputFACostTxt + i + ".txt", false);
            System.out.println("运行时间：" + getFourBitsDoubleString((end - start) / 1000.0) + " s");
        }
    }

    private static void faRandomSearchTest(Configuration configuration, int startIter) {
        // 随机搜索，初始化种群
        configuration.setPopulationProducer(DefaultPluginProvider.faInitPopulationProducer());
        configuration.objectives = ObjectiveProvider.provideFAStructuralAndSemanticAndMoJoFM();
        // 交叉操作
        configuration.setChromosomeLength(faNums);
        configuration.setCrossover(new FARandomCrossover(null, crossoverProb));
        // 变异操作 - mutationProbability是在refactorGenerateChildrenProducerEncode方法中用的
        configuration.setMutation(new FARandomMutation(mutationProb));
        // 子代更新方法，参数暂时没用到
        configuration.setChildPopulationProducer(DefaultPluginProvider.randomChildrenProducer(mutationProb));
        // 最大代数，与终止条件有关
        configuration.setGenerations(faMaxGeneration);
        // 原意是种群的固定数量，但在本研究中，种群数量是从1开始的，次值应该是种群上限
        configuration.setPopulationSize(faMaxPopulationSize);
        // 遗传终止方法
        configuration.setTerminatingCriterion(TerminatingCriterionProvider.refactorTerminatingCriterion(
                faMaxPopulationSize, faMaxRecord));

        FANsga2 rs = new FANsga2(configuration);
        for (int i = startIter; i <= experimentTimes; i++) {
            experimentInitDataStructure("fa");
            // 实验计时
            long start = System.currentTimeMillis();
            rs.run(i, outputFARandomProgramJson, outputFARandomObjectiveTxt, outputFARandomFrontTxt, outputFARandomCostTxt, false);
            long end = System.currentTimeMillis();
            appendStringToFile("\ntime:" + (end - start) / 1000.0, outputFARandomCostTxt + i + ".txt", false);
            System.out.println("运行时间：" + getFourBitsDoubleString((end - start) / 1000.0) + " s");
        }
    }

    private static void fileRefactorTest(Configuration configuration, int startIter) {
        // 代码文件粒度重构，初始化种群
        configuration.setPopulationProducer(DefaultPluginProvider.fileInitPopulationProducer());
        configuration.objectives = ObjectiveProvider.provideFileStructuralAndSemanticAndMoJoFM();
        // 交叉操作
        configuration.setChromosomeLength(overloadServiceFileList.size());
        configuration.setCrossover(new FileSingleCrossover(null, crossoverProb));
        // 变异操作 - mutationProbability是在refactorGenerateChildrenProducerEncode方法中用的
        configuration.setMutation(new FileModularMutation(mutationProb, breakProb));
        // 子代更新方法
        configuration.setChildPopulationProducer(DefaultPluginProvider.childrenProducer(mutationProb));
        // 最大代数，与终止条件有关
        configuration.setGenerations(fileMaxGeneration);
        // 原意是种群的固定数量，但在本研究中，种群数量是从1开始的，次值应该是种群上限
        configuration.setPopulationSize(fileMaxPopulationSize);
        // 遗传终止方法
        configuration.setTerminatingCriterion(TerminatingCriterionProvider.refactorTerminatingCriterion(
                fileMaxPopulationSize, fileMaxRecord));

        FileNsga2 fileNsga2 = new FileNsga2(configuration);
        for (int i = startIter; i <= experimentTimes; i++) {
            System.out.println("experiment - " + i + " -------------------------------------------------------------------");
            experimentInitDataStructure("file");

            long start = System.currentTimeMillis();
            fileNsga2.run(i, outputFileProgramJson, outputFileObjectiveTxt, outputFileFrontTxt, outputFileCostTxt, true);
            long end = System.currentTimeMillis();

            appendStringToFile("\ntime:" + (end - start) / 1000.0, outputFileCostTxt + i + ".txt", false);
            System.out.println("运行时间：" + getFourBitsDoubleString((end - start) / 1000.0) + " s");
        }
    }

    private static void fileRandomSearchTest(Configuration configuration, int startIter) {
        // 代码文件粒度的随机搜索，初始化种群
        configuration.setPopulationProducer(DefaultPluginProvider.fileInitPopulationProducer());
        configuration.objectives = ObjectiveProvider.provideFileStructuralAndSemanticAndMoJoFM();
        // 交叉操作
        configuration.setChromosomeLength(overloadServiceFileList.size());
        configuration.setCrossover(new FileRandomCrossover(null, crossoverProb));
        // 变异操作 - mutationProbability是在refactorGenerateChildrenProducerEncode方法中用的
        configuration.setMutation(new FileRandomMutation(mutationProb));
        // 子代更新方法，参数暂时没用到
        configuration.setChildPopulationProducer(DefaultPluginProvider.randomChildrenProducer(mutationProb));
        // 最大代数，与终止条件有关
        configuration.setGenerations(fileMaxGeneration);
        // 原意是种群的固定数量，但在本研究中，种群数量是从1开始的，次值应该是种群上限
        configuration.setPopulationSize(fileMaxPopulationSize);
        // 遗传终止方法
        configuration.setTerminatingCriterion(TerminatingCriterionProvider.refactorTerminatingCriterion(
                fileMaxPopulationSize, fileMaxRecord));

        FileNsga2 rs = new FileNsga2(configuration);
        for (int i = 1; i <= experimentTimes; i++) {
            experimentInitDataStructure("file");

            long start = System.currentTimeMillis();
            rs.run(i, outputFileRandomProgramJson, outputFileRandomObjectiveTxt, outputFileRandomFrontTxt, outputFileRandomCostTxt, false);
            long end = System.currentTimeMillis();

            appendStringToFile("\ntime:" + (end - start) / 1000.0, outputFileRandomCostTxt + i + ".txt", false);
            System.out.println("运行时间：" + getFourBitsDoubleString((end - start) / 1000.0) + " s");
        }

    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();

        long start = System.currentTimeMillis();
//        faRefactorTest(configuration, 1);
//        faRandomSearchTest(configuration, 1);
        fileRefactorTest(configuration, 1);
        fileRandomSearchTest(configuration, 1);

        long end = System.currentTimeMillis();
        System.out.println("任务总耗时：" + getFourBitsDoubleString((end - start) / 1000.0));
    }
}
