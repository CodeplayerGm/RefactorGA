package nsga.objective;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nsga.ParameterConfig.experimentTimes;
import static nsga.PostProcessShowData.outputFARandomObjectiveTxt;
import static nsga.PreProcessLoadData.readFile;
import static nsga.Service.getFourBitsDoubleString;

public class EuclidianDistance {

    public static void calculateAvgObjectiveDistance(String experimentGroupCostPath) throws IOException {
        double minDis = 9999, avgDis = 0;
        double avgSMQ = 0, avgSemantic = 0, avgMoJoFM = 0;
        double bestSmq = 0, bestSemantic = 0, bestMoJoFm = 0;
        int count = 0;
        for (int i = 1; i <= experimentTimes; i++) {
            String data = readFile(experimentGroupCostPath + i + ".txt");
//            System.out.println("实验 - " + i + " -------------------------------------------------------------");
            String[] programs = data.split("\n");
            count += programs.length - 1;
            for (int j = 1; j < programs.length; j++) {
//                System.out.println("  方案 - " + j + "  ..........................................");
                String es = programs[j];
                List<Double> objList = Arrays.stream(es.split(" ")).map(Double::parseDouble).collect(Collectors.toList());
                double result = 0;
                for (double od : objList) {
                    result += (1 - od) * (1 - od);
                }
                avgSMQ += objList.get(0);
                avgSemantic += objList.get(1);
                avgMoJoFM += objList.get(2);
//                System.out.println("  euclidian distance: " + Math.sqrt(result));
                if (result < minDis) {
                    minDis = result;
                    bestSmq = objList.get(0);
                    bestSemantic = objList.get(1);
                    bestMoJoFm = objList.get(2);
                }
                minDis = Math.min(minDis, result);
                avgDis += result;
            }
        }
        System.out.println("==========================================================================================");
        System.out.println("最优解距离：" + getFourBitsDoubleString(minDis) + " bestSMQ = " + getFourBitsDoubleString(bestSmq) +
                " bestSemantic = " + getFourBitsDoubleString(bestSemantic) + " bestMoJo = " + getFourBitsDoubleString(bestMoJoFm));
        System.out.println("平均SMQ：" + getFourBitsDoubleString(avgSMQ / count) +
                " ; 平均semantic：" + getFourBitsDoubleString(avgSemantic / count) +
                " ; 平均mojo：" + getFourBitsDoubleString(avgMoJoFM / count));
        System.out.println("平均距离：" + getFourBitsDoubleString(avgDis / count));
        System.out.println("平均前沿数量：" + count / experimentTimes);
    }

    public static void main(String[] args) throws IOException {
//        calculateAvgObjectiveDistance(outOutputFAObjectiveTxt);
        calculateAvgObjectiveDistance(outputFARandomObjectiveTxt);
    }
}
