package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.datastructure.Chromosome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nsga.PreProcessLoadData.*;
import static nsga.Service.*;

public class SMQFAObjective extends AbstractObjectiveFunction {

    public SMQFAObjective() {
        this.objectiveFunctionTitle = "SMQ FA Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFAMap = splitModularAlleleToServiceFAMap(chromosome);
        double smq = getAllServiceCohesion(srvFAMap) - getAllServiceCouplingDesc(srvFAMap);
//        System.out.println("SMQ: " + smq);
//        System.out.println("SMQ: " + smq + " ; improved: " + (smq - originSMQ)/originSMQ);
        return smq;
    }

    public static double getAllServiceCohesion(HashMap<Integer, List<Integer>> srvFAMap) {
        double totalCohesion = 0;
        int serviceNum = srvFAMap.size();

        // 当前服务的内聚计算 - 几个groupItem就是几个新服务
        for (Map.Entry<Integer, List<Integer>> entry : srvFAMap.entrySet()) {
            List<String> serviceFiles = getFileListFromFAList(entry.getValue());
            double cohesion = getSingleServiceCohesion(serviceFiles);
//            System.out.println("files-" + serviceFiles.size() + " ; cohesion-" + cohesion);
            totalCohesion += cohesion;
        }
        return totalCohesion / serviceNum;
    }

    public static double getSingleServiceCohesion(List<String> files) {
        List<Integer> fileIndexList = files.stream()
                .map(f -> overloadServiceFileList.indexOf(f))
                .collect(Collectors.toList());
        // 从call graph中找到edge数量
        int edgeNums = 0;
        for (int i = 0; i < fileIndexList.size(); i++) {
            for (int j = 0; j < fileIndexList.size(); j++) {
                if (j != i) {
                    int src = fileIndexList.get(i), dest = fileIndexList.get(j);
                    if (overloadServiceCallGraph[src][dest] > 0) {
                        edgeNums += overloadServiceCallGraph[src][dest];
                    }
                }
            }
        }
//        System.out.println("edges:" + edgeNums + "; files: " + files.size());
        return edgeNums == 0 ? 0 : edgeNums * 1.0 / files.size() / files.size() / maxFileCallNums;
    }

    public static double getAllServiceCouplingDesc(HashMap<Integer, List<Integer>> srvFAMap) {
        List<String> allServices = new ArrayList<>();
        // 服务名 - 文件列表
        Map<String, List<String>> curServiceMap = new HashMap<>();

        for (Map.Entry<Integer, List<Integer>> entry : srvFAMap.entrySet()) {
            String serviceName = "newService" + entry.getKey();
            allServices.add(serviceName);
            curServiceMap.put(serviceName, getFileListFromFAList(entry.getValue()));
        }

        int serviceNum = srvFAMap.size();
        int maxSrvEdge = 0;
        // 服务依赖矩阵
        int[][] serviceEdgeMatrix = new int[serviceNum][serviceNum];
        int len = overloadServiceFileList.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i != j && overloadServiceCallGraph[i][j] > 0) {
                    // 找到serviceIndex，填入矩阵
                    String srcService = getFileService(curServiceMap, overloadServiceFileList.get(i));
                    String destService = getFileService(curServiceMap, overloadServiceFileList.get(j));
                    int srcIndex = allServices.indexOf(srcService);
                    int destIndex = allServices.indexOf(destService);
                    if (srcIndex >= 0 && destIndex >= 0 && srcIndex != destIndex) {
                        serviceEdgeMatrix[srcIndex][destIndex] += overloadServiceCallGraph[i][j];
                        maxSrvEdge = Math.max(maxSrvEdge, overloadServiceCallGraph[i][j]);
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
                int faN = 2 * curServiceMap.get(s1).size() * curServiceMap.get(s2).size();
                double curCoup = edges * 1.0 / faN / maxSrvEdge;
                result += curCoup;
            }
        }

        return 2 * result / (serviceNum * (serviceNum - 1));
    }

    public static double getAllServiceCouplingAsc(HashMap<Integer, List<Integer>> srvFAMap) {
        return 1- getAllServiceCouplingDesc(srvFAMap);
    }

    public static String getFileService(Map<String, List<String>> curServiceMap, String file) {
        for(Map.Entry<String, List<String>> entry : curServiceMap.entrySet()) {
            if (entry.getValue().contains(file)) {
                return entry.getKey();
            }
        }
        return "file doesn't belong to any exist service";
    }

    /**
     * 从json中整理指定重构方案的srvFAMap
     * @param program
     * @return
     */
    public static HashMap<Integer, List<Integer>> getTargetProgramSrvFAMap(JSONObject program) {
        HashMap<Integer, List<Integer>> srvFAMap = new HashMap<>();
        JSONArray services = program.getJSONArray("children");
        for (int j = 0; j < services.size(); j++) {
            JSONObject service = services.getJSONObject(j);
            JSONArray fas = service.getJSONArray("children");
            srvFAMap.put(j + 1, new ArrayList<>());
            for (int k = 0; k < fas.size(); k++) {
                String faName = fas.getJSONObject(k).getString("name");
                srvFAMap.get(j + 1).add(Integer.parseInt(faName.substring("FA - ".length())));
            }
        }
        return srvFAMap;
    }

    /**
     * 计算每个方案的内聚改进百分比
     * @param programs
     * @param originCohesion
     */
    public static void getProgramCohesionImproved(JSONArray programs, double originCohesion) {
        for (int i = 1; i < programs.size(); i++) {
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<Integer>> srvFAMap = getTargetProgramSrvFAMap(program);
            double cohesion = getAllServiceCohesion(srvFAMap);
            System.out.println("cohesion: " + cohesion);
            System.out.println("提升：" + (cohesion - originCohesion) / originCohesion);
        }
    }

    /**
     * 计算每个方案的SMQ改进百分比
     * @param programs
     * @param originSMQ
     */
    public static void getProgramSMQImproved(JSONArray programs, double originSMQ) {
        for (int i = 1; i < programs.size(); i++) {
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<Integer>> srvFAMap = getTargetProgramSrvFAMap(program);
            double cohesion = getAllServiceCohesion(srvFAMap);
            double coupling = getAllServiceCouplingDesc(srvFAMap);
//            System.out.println("cohesion: " + getFourBitsDoubleString(cohesion) + "; coupling: " + getFourBitsDoubleString(coupling));
//            System.out.println("smq: " + getFourBitsDoubleString(cohesion-coupling));
            System.out.println("提升：" + getFourBitsDoubleString((cohesion - coupling - originSMQ) / originSMQ));
        }
    }

    public static void main(String[] args) throws IOException {
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faPrograms-1.json");
//        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\randomRefactorPrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);
        double originSMQ = getAllServiceCohesion(getTargetProgramSrvFAMap(programs.getJSONObject(0)));
        System.out.println("origin smq: " + getFourBitsDoubleString(originSMQ));

        getProgramSMQImproved(programs, originSMQ);
    }

}
