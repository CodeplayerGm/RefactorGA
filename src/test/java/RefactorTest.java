import nsga.Configuration;
import nsga.NSGA2;
import nsga.PreProcessLoadData;
import nsga.crossover.SingleCrossoverEncode;
import nsga.mutation.ModularMutation;
import nsga.objectivefunction.ObjectiveProvider;
import nsga.plugin.DefaultPluginProvider;
import nsga.termination.TerminatingCriterionProvider;

public class RefactorTest {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();

//        // 旧的基因型设计，目标：过载分数、SMQ/内聚+耦合 -----------------------------------------------------------------
//        // 初始化种群方法
//        configuration.setPopulationProducer(DefaultPluginProvider.refactorInitPopulationProducer());
//        // 适应度函数组合
//        configuration.objectives = ObjectiveProvider.provideRefactorObjectives();
////        configuration.objectives = ObjectiveProvider.provideRefactorObjectivesSplited();
//        // 交叉选择器
//        configuration.setCrossover(new SingleCrossover(null, 0.75f));
//        // 更新种群方法
//        configuration.setChildPopulationProducer(DefaultPluginProvider.refactorGenerateChildrenProducer());
//        // 最大代数，与终止条件有关
//        configuration.setGenerations(200);
//        // 原意是种群的固定数量，但在本研究中，种群数量是从1开始的，次值应该是种群上限
//        configuration.setPopulationSize(100);
//        // 遗传终止方法
//        configuration.setTerminatingCriterion(100, 5000, 0.8));


        // 新的基因型设计，初始化种群，目标：过载分数、SMQ、MoJo
        configuration.setPopulationProducer(DefaultPluginProvider.refactorInitPopulationProducerEncode());
        configuration.objectives = ObjectiveProvider.provideRefactorObjectivesEncode();
        // 交叉操作
        configuration.setChromosomeLength(PreProcessLoadData.faNums);
        configuration.setCrossover(new SingleCrossoverEncode(null, 0.6f));
        // 变异操作 - mutationProbability是在refactorGenerateChildrenProducerEncode方法中用的
        configuration.setMutation(new ModularMutation(0.3f, 0.3f));
        // 子代更新方法
        configuration.setChildPopulationProducer(DefaultPluginProvider.refactorGenerateChildrenProducerEncode(0.3f));
        // 最大代数，与终止条件有关
        configuration.setGenerations(100);
        // 原意是种群的固定数量，但在本研究中，种群数量是从1开始的，次值应该是种群上限
        configuration.setPopulationSize(100);
        // 遗传终止方法
        configuration.setTerminatingCriterion(TerminatingCriterionProvider.refactorTerminatingCriterion(
                100, 5000, 0.8));

        NSGA2 nsga2 = new NSGA2(configuration);
        nsga2.run();

    }
}
