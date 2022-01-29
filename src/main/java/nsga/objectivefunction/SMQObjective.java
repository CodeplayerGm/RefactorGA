package nsga.objectivefunction;

import nsga.datastructure.AbstractAllele;
import nsga.datastructure.Chromosome;
import nsga.datastructure.GroupItemAllele;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nsga.PreProcessLoadData.allCallMatrix;
import static nsga.PreProcessLoadData.allFileList;

public class SMQObjective extends AbstractObjectiveFunction {

    public SMQObjective() {
        this.objectiveFunctionTitle = "SMQ Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        double smq = getAllServiceCohesion(chromosome) - getAllServiceCoupling(chromosome);
//        System.out.println("SMQ: " + smq);
//        System.out.println("SMQ: " + smq + " ; improved: " + (smq - originSMQ)/originSMQ);
        return smq;
    }

    public static double getAllServiceCoupling(Chromosome chromosome) {
        List<String> allServices = new ArrayList<>();
        Map<String, List<String>> curServiceMap = new HashMap<>();
        Map<String, Integer> curFAMap = new HashMap<>();
        int index = 0;
        for (AbstractAllele allele : chromosome.getGeneticCode()) {
            GroupItemAllele gi = (GroupItemAllele) allele;
            String ns = "newService" + index++;
            allServices.add(ns);
            curServiceMap.put(ns, gi.getAllFiles());
            curFAMap.put(ns, gi.getGene().size());
        }
        int serviceNum = chromosome.getGeneticCode().size();

        int[][] serviceEdgeMatrix = new int[serviceNum][serviceNum];
        int len = allFileList.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (allCallMatrix[i][j] > 0) {
                    // 找到serviceIndex，填入矩阵
                    String srcService = getFileService(curServiceMap, allFileList.get(i));
                    String destService = getFileService(curServiceMap, allFileList.get(j));
                    int srcIndex = allServices.indexOf(srcService);
                    int destIndex = allServices.indexOf(destService);
                    if (srcIndex >= 0 && destIndex >= 0 && srcIndex != destIndex) {
                        serviceEdgeMatrix[srcIndex][destIndex] += allCallMatrix[i][j];
                    }
                }
            }
        }

        // 计算两两之间的coup
        double result = 0;
        for (int i = 0; i < serviceNum; i++) {
            for (int j = i + 1; j < serviceNum; j++) {
                String s1 = allServices.get(i), s2 = allServices.get(j);
                int edges = serviceEdgeMatrix[i][j] + serviceEdgeMatrix[j][i];
                int faN = curFAMap.get(s1) * curFAMap.get(s2);
                double curCoup = edges * 1.0 / faN;
//                System.out.println("service: " + s1 + " to service: " + s2);
//                System.out.println("edges: " + edges + " ; faN: " + faN + " ; coup: " + curCoup);
                result += curCoup;
            }
        }

//        System.out.println("total: coup: " + result + " ; serviceNum: " + serviceNum + " ; ret: " + result);
        return result;
    }

    public static double getAllServiceCohesion(Chromosome chromosome) {
//        double totalCohesion = Optional.of(otherServiceCohesionMap.values().stream()
//                .reduce(Double::sum).get()).orElse(0.0);
        double totalCohesion = 0;
//        int serviceNum = otherServiceFAMap.size() + individual.groupItemList.size();
        int serviceNum = chromosome.getGeneticCode().size();

        // 当前服务的内聚计算 - 几个groupItem就是几个新服务
        int index = 0;
        for (AbstractAllele allele : chromosome.getGeneticCode()) {
            GroupItemAllele gi = (GroupItemAllele) allele;
            List<String> serviceFiles = gi.getAllFiles();
            int faNums = gi.getGene().size();
            double cohesion = getSingleServiceCohesion(serviceFiles, faNums);
//            double cohesion = getSingleServiceCohesion1(gi.faList, serviceFiles);
            totalCohesion += cohesion;
//            System.out.println("new service " + index++ + ": files-" + serviceFiles.size() + "; faNums: " + faNums + "; cohesion: " + cohesion);
        }

//        System.out.println("total cohesion: " + totalCohesion / serviceNum);
        return totalCohesion / serviceNum;
    }

    public static String getFileService(Map<String, List<String>> curServiceMap, String file) {
        for(Map.Entry<String, List<String>> entry : curServiceMap.entrySet()) {
            if (entry.getValue().contains(file)) {
                return entry.getKey();
            }
        }
//        for(Map.Entry<String, List<String>> entry : otherServiceFileMap.entrySet()) {
//            if (entry.getValue().contains(file)) {
//                return entry.getKey();
//            }
//        }
        return "file doesn't belong to any exist service";
    }

    public static double getSingleServiceCohesion(List<String> files, int faNums) {
        List<Integer> fileIndexList = files.stream()
                .map(f -> allFileList.indexOf(f))
                .collect(Collectors.toList());
        // 从call graph中找到edge数量
        int edgeNums = 0;
        for (int i = 0; i < fileIndexList.size(); i++) {
            for (int j = 0; j < fileIndexList.size(); j++) {
                if (j != i) {
                    if (allCallMatrix[i][j] > 0) {
                        edgeNums += allCallMatrix[i][j];
                    }
                }
            }
        }
//        System.out.println("edges:" + edgeNums + "; faNums:" + faNums + "; files: " + files.size());
        return edgeNums * 1.0 / files.size();
    }
}
