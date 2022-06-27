package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.Service;
import nsga.datastructure.Chromosome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static nsga.PreProcessLoadData.*;
import static nsga.objective.SMQFAObjective.getSingleServiceCohesion;

public class SMQFileObjective extends AbstractObjectiveFunction {

    public SMQFileObjective() {
        this.objectiveFunctionTitle = "SMQ Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFilesMap = Service.splitChromosomeToServiceFileIdMap(chromosome);
        double smq = getAllServiceCohesion(srvFilesMap) - getAllServiceCouplingDesc(srvFilesMap);
//        System.out.println("SMQ: " + smq);
//        System.out.println("SMQ: " + smq + " ; improved: " + (smq - originSMQ)/originSMQ);
        return smq;
    }

    public static double getAllServiceCohesion(HashMap<Integer, List<Integer>> srvFilesMap) {
        double totalCohesion = 0;
        int serviceNum = srvFilesMap.size();

        // 当前服务的内聚计算
        for (Map.Entry<Integer, List<Integer>> entry : srvFilesMap.entrySet()) {
            List<String> serviceFiles = new ArrayList<>();
            entry.getValue().forEach(fi -> serviceFiles.add(overloadServiceFileList.get(fi)));
            double cohesion = getSingleServiceCohesion(serviceFiles);
//            System.out.println("service-" + entry.getKey() + " ; cohesion-" + cohesion + " ; files-" + serviceFiles.size());
            totalCohesion += cohesion;
        }
        return totalCohesion / serviceNum;
    }

    public static double getAllServiceCouplingDesc(HashMap<Integer, List<Integer>> srvFilesMap) {
        List<String> allServices = new ArrayList<>();
        // 服务名 - 文件列表
        Map<String, List<Integer>> srvNameToFileIdListMap = new HashMap<>();

        for (Map.Entry<Integer, List<Integer>> entry : srvFilesMap.entrySet()) {
            String serviceName = "newService" + entry.getKey();
            allServices.add(serviceName);
            srvNameToFileIdListMap.put(serviceName, entry.getValue());
        }

        int serviceNum = srvFilesMap.size();
        int[][] serviceEdgeMatrix = new int[serviceNum][serviceNum];
        int len = overloadServiceFileList.size();
        int maxEdge = 0;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (overloadServiceCallGraph[i][j] > 0) {
                    String srcService = getFileService(srvNameToFileIdListMap, i);
                    String destService = getFileService(srvNameToFileIdListMap, j);
                    int srcIndex = allServices.indexOf(srcService);
                    int destIndex = allServices.indexOf(destService);
                    if (srcIndex >= 0 && destIndex >= 0 && srcIndex != destIndex) {
                        serviceEdgeMatrix[srcIndex][destIndex] += overloadServiceCallGraph[i][j];
                        maxEdge = Math.max(maxEdge, overloadServiceCallGraph[i][j]);
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
                if (edges > 0) {
                    int faN = 2 * srvNameToFileIdListMap.get(s1).size() * srvNameToFileIdListMap.get(s2).size();
                    double curCoup = edges * 1.0 / faN / maxEdge;
                    result += curCoup;
                }
            }
        }

        return 2 * result / (serviceNum * (serviceNum - 1));
    }

    public static double getAllServiceCouplingAsc(HashMap<Integer, List<Integer>> srvFilesMap) {
        return 1 - getAllServiceCouplingDesc(srvFilesMap);
    }

    public static String getFileService(Map<String, List<Integer>> curServiceMap, Integer fileId) {
        for(Map.Entry<String, List<Integer>> entry : curServiceMap.entrySet()) {
            if (entry.getValue().contains(fileId)) {
                return entry.getKey();
            }
        }
        return "file doesn't belong to any exist service";
    }

    public static HashMap<Integer, List<Integer>> getTargetSrvFilesMap(JSONObject program) {
        HashMap<Integer, List<Integer>> srvFilesMap = new HashMap<>();
        JSONArray originServices = program.getJSONArray("children");
        for (int j = 0; j < originServices.size(); j++) {
            JSONObject originSrv = originServices.getJSONObject(j);
            JSONArray faArr = originSrv.getJSONArray("children");
            List<Integer> fileIdList = new ArrayList<>();
            for (int k = 0; k < faArr.size(); k++) {
                JSONObject faObj = faArr.getJSONObject(k);
                JSONArray fileArr = faObj.getJSONArray("children");
                for (int i = 0; i < fileArr.size(); i++) {
                    fileIdList.add(overloadServiceFileList.indexOf(fileArr.getJSONObject(i).getString("name")));
                }
            }
            srvFilesMap.put(j + 1, fileIdList);
//            System.out.println("origin map: " + (j+1) + " ; files: " + fileIdList);
        }
        return srvFilesMap;
    }

    public static void getProgramCohesionImproved(JSONArray programs, double originCohesion) {
        for (int i = 1; i < programs.size(); i++) {
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<Integer>> srvToFileIdListMap = getTargetSrvFilesMap(program);
            double cohesion = getAllServiceCohesion(srvToFileIdListMap);
            System.out.println("cohesion: " + cohesion);
            System.out.println("提升：" + (cohesion - originCohesion) / originCohesion);
        }
    }

    public static void getProgramSMQImproved(JSONArray programs, double originSMQ) {
        for (int i = 1; i < programs.size(); i++) {
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<Integer>> srvFilesMap = getTargetSrvFilesMap(program);
            double cohesion = getAllServiceCohesion(srvFilesMap);
            double coupling = getAllServiceCouplingAsc(srvFilesMap);
            System.out.println("cohesion: " + cohesion + "; coupling: " + coupling);
            System.out.println("smq: " + (cohesion-coupling));
            System.out.println("提升：" + (cohesion - coupling - originSMQ) / originSMQ);
        }
    }

    public static void main(String[] args) throws IOException {
//        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faPrograms.json");
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\filePrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);
        HashMap<Integer, List<Integer>> originMap = getTargetSrvFilesMap(programs.getJSONObject(0));
        double originSMQ = getAllServiceCohesion(originMap);
        System.out.println("origin smq: " + originSMQ);

        getProgramSMQImproved(programs, originSMQ);
    }

}
