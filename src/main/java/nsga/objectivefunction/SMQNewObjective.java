package nsga.objectivefunction;

import nsga.Service;
import nsga.datastructure.Chromosome;
import nsga.PreProcessLoadData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SMQNewObjective extends AbstractObjectiveFunction {

    public SMQNewObjective() {
        this.objectiveFunctionTitle = "SMQ Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> moduleFAMap = Service.splitModularAlleleToServiceFAMap(chromosome);
        double smq = getAllServiceCohesion(moduleFAMap) - getAllServiceCoupling(moduleFAMap);
//        System.out.println("SMQ: " + smq);
//        System.out.println("SMQ: " + smq + " ; improved: " + (smq - originSMQ)/originSMQ);
        return smq;
    }

    public static double getAllServiceCohesion(HashMap<Integer, List<Integer>> moduleFAMap) {
        double totalCohesion = 0;
        int serviceNum = moduleFAMap.size();

        // 当前服务的内聚计算 - 几个groupItem就是几个新服务
        for (Map.Entry<Integer, List<Integer>> entry : moduleFAMap.entrySet()) {
            List<String> serviceFiles = Service.getModuleFileList(entry.getValue());
            double cohesion = getSingleServiceCohesion(serviceFiles);
            totalCohesion += cohesion;
        }

        return totalCohesion / serviceNum;
    }

    public static double getSingleServiceCohesion(List<String> files) {
        List<Integer> fileIndexList = files.stream()
                .map(f -> PreProcessLoadData.allFileList.indexOf(f))
                .collect(Collectors.toList());
        // 从call graph中找到edge数量
        int edgeNums = 0;
        for (int i = 0; i < fileIndexList.size(); i++) {
            for (int j = 0; j < fileIndexList.size(); j++) {
                if (j != i) {
                    if (PreProcessLoadData.allCallMatrix[i][j] > 0) {
                        edgeNums += PreProcessLoadData.allCallMatrix[i][j];
                    }
                }
            }
        }
//        System.out.println("edges:" + edgeNums + "; faNums:" + faNums + "; files: " + files.size());
        return edgeNums * 1.0 / files.size();
    }

    public static double getAllServiceCoupling(HashMap<Integer, List<Integer>> moduleFAMap) {
        List<String> allServices = new ArrayList<>();
        // 服务名 - 文件列表
        Map<String, List<String>> curServiceMap = new HashMap<>();
        // 服务名 - 功能原子数量
        Map<String, Integer> curFAMap = new HashMap<>();

        for (Map.Entry<Integer, List<Integer>> entry : moduleFAMap.entrySet()) {
            String serviceName = "newService" + entry.getKey();
            allServices.add(serviceName);
            curServiceMap.put(serviceName, Service.getModuleFileList(entry.getValue()));
            curFAMap.put(serviceName, entry.getValue().size());
        }

        int serviceNum = moduleFAMap.size();
        // 服务依赖矩阵
        int[][] serviceEdgeMatrix = new int[serviceNum][serviceNum];
        int len = PreProcessLoadData.allFileList.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (PreProcessLoadData.allCallMatrix[i][j] > 0) {
                    // 找到serviceIndex，填入矩阵
                    String srcService = getFileService(curServiceMap, PreProcessLoadData.allFileList.get(i));
                    String destService = getFileService(curServiceMap, PreProcessLoadData.allFileList.get(j));
                    int srcIndex = allServices.indexOf(srcService);
                    int destIndex = allServices.indexOf(destService);
                    if (srcIndex >= 0 && destIndex >= 0 && srcIndex != destIndex) {
                        serviceEdgeMatrix[srcIndex][destIndex] += PreProcessLoadData.allCallMatrix[i][j];
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
                result += curCoup;
            }
        }

        return result;
    }

    public static String getFileService(Map<String, List<String>> curServiceMap, String file) {
        for(Map.Entry<String, List<String>> entry : curServiceMap.entrySet()) {
            if (entry.getValue().contains(file)) {
                return entry.getKey();
            }
        }
        return "file doesn't belong to any exist service";
    }

}
