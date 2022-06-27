import java.io.IOException;

import static nsga.ParameterConfig.experimentTimes;
import static nsga.PostProcessShowData.*;
import static nsga.PreProcessLoadData.readFile;
import static nsga.Service.getFourBitsDoubleString;
import static nsga.objective.EuclidianDistance.calculateAvgObjectiveDistance;

public class ParameterSettingTest {

    public static void calculateAvgExperimentCost(String experimentGroupCostPath) throws IOException {
        double avgIterations = 0, avgNotOverload = 0, avgOverload = 0, avgTime = 0;
        for (int i = 1; i <= experimentTimes; i++) {
            String data = readFile( experimentGroupCostPath + i + ".txt");
//            System.out.println("实验 - " + i + " -------------------------------------------------------------");
            String[] costs = data.split("\n");

            avgIterations += Double.parseDouble(costs[0].substring(costs[0].indexOf(":") + 1));
            avgNotOverload += Double.parseDouble(costs[1].substring(costs[1].indexOf(":") + 1));
            avgOverload += Double.parseDouble(costs[2].substring(costs[2].indexOf(":") + 1));
            avgTime += Double.parseDouble(costs[3].substring(costs[3].indexOf(":") + 1));
        }
        System.out.println("平均迭代次数：" + (int)(avgIterations / experimentTimes));
        System.out.println("平均过载数量：" + (int)(avgOverload / experimentTimes));
        System.out.println("平均不过载数量：" + (int)(avgNotOverload / experimentTimes));
        System.out.println("平均执行时间：" + getFourBitsDoubleString(avgTime / experimentTimes));
    }

    public static void statisticsForExperimentParameterSetting(String objectivePath, String costPath) throws IOException {
        // 1、计算最优解距离
        calculateAvgObjectiveDistance(objectivePath);
        // 2、计算平均开销信息
        calculateAvgExperimentCost(costPath);
    }

    public static void main(String[] args) throws IOException {
//        statisticsForExperimentParameterSetting(outputFAObjectiveTxt, outputFACostTxt);
//        statisticsForExperimentParameterSetting(outputFARandomObjectiveTxt, outputFARandomCostTxt);
//        statisticsForExperimentParameterSetting(outputFileObjectiveTxt, outputFileCostTxt);
        statisticsForExperimentParameterSetting(outputFileRandomObjectiveTxt, outputFileRandomCostTxt);
    }
}
