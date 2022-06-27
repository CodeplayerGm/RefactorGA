package nsga.objective;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.datastructure.Chromosome;
import nsga.datastructure.excel.ServiceData;
import nsga.datastructure.excel.ServiceListener;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static nsga.ParameterConfig.curFilePrefix;
import static nsga.ParameterConfig.originFilePrefix;
import static nsga.PreProcessLoadData.*;
import static nsga.Service.splitChromosomeToFAFileListMap;

public class MoJoFMFAObjective extends AbstractObjectiveFunction {

    public MoJoFMFAObjective() {
        this.objectiveFunctionTitle = "MoJoFM FA Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> moduleFAMap = splitChromosomeToFAFileListMap(chromosome);
        int maxModuleFileSize = moduleFAMap.values().stream()
                .mapToInt(List::size).max().getAsInt();

        return maxModuleFileSize * 1.0 / overloadServiceFileList.size();
    }

    public static int getFileTagInOriginSFMap(Map<String, List<String>> originSFMap, String file) throws Exception {
        int i = 0;
        for (Map.Entry<String, List<String>> entry : originSFMap.entrySet()) {
            List<String> files = entry.getValue();
            for (String originFile : files) {
                if (originFile.substring(originFilePrefix.length()).equals(file.substring(curFilePrefix.length()))) {
                    return i;
                } else {
                    // 针对某些服务被藏在不同层级的代码文件，从main开始去掉前缀，比较后半部分
                    int s1 = originFile.indexOf("main");
                    int s2 = file.indexOf("main");
                    if (originFile.substring(s1).equals(file.substring(s2))) {
                        return i;
                    }
                }
            }
            i ++;
        }
        throw new Exception("寻找原始服务对应文件的tag失败，检查一下对比文件名称的路径！");
    }

    public static boolean find(int leftIndex, int rightNodeNum, boolean[][] M, int[] rightAssignedTo, boolean[] ifRightAssigned) {
        // 遍历右边二分图的节点
//        System.out.println("  left index: " + leftIndex);
        for (int i = 0; i < rightNodeNum; i++) {
//            System.out.println("    right search: " + i);
            if (!M[leftIndex][i] || ifRightAssigned[i])
                continue;
            ifRightAssigned[i] = true;
            if (rightAssignedTo[i] == -1 || find(rightAssignedTo[i], rightNodeNum, M, rightAssignedTo, ifRightAssigned)) {
                rightAssignedTo[i] = leftIndex;
                return true;
            }
        }
        return false;
    }

    /**
     * 搜索极大二分匹配算法测试
     */
    private static void binaryMatchAlgoTest() {
        boolean[][] M = {
                {true, false, false, false},
                {true, true, false, false},
                {false, true, true, false},
                {false, true, true, false}
        };
        boolean[] ifRightAssigned = new boolean[4];
        int[] rightAssignedTo = new int[4];
        Arrays.fill(rightAssignedTo, -1);
        // 遍历左边二分图的节点
        for (int i = 0; i < 4; i++) {
            Arrays.fill(ifRightAssigned, false);
            System.out.println("left search: " + i);
            if (find(i, 4, M, rightAssignedTo, ifRightAssigned)) {
                System.out.println("left assigned - " + i);
            }
        }

        for (int i = 0; i < 4; i++) {
            System.out.println("right " + i + " assign to left: " + rightAssignedTo[i]);
        }
    }

    public static List<ServiceData> readOriginSrvToFileIdListMap(String originSrvExcelPath) throws FileNotFoundException {
        InputStream is = new FileInputStream(originSrvExcelPath);
        ServiceListener listener = new ServiceListener();
        EasyExcel.read(is, ServiceData.class, listener).sheet().doRead();
        return listener.serviceList;
    }

    public static double getMaximalDichotomyMatchingMoJoFM(HashMap<Integer, List<String>> SFMap, Map<String, List<String>> originSFMap, int maxMoJo, boolean ifPrint) {
        // 开始计算mojo
        // 1、给SFMap的每个文件打标签，并统计每个service下每个tag的数量
        HashMap<Integer, List<Integer>> STMap = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Integer>> STNMap = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : SFMap.entrySet()) {
            int serviceId = entry.getKey();
            STMap.put(serviceId, new ArrayList<>());
            STNMap.put(serviceId, new HashMap<>());
            entry.getValue().forEach(fs -> {
//                System.out.println("fs: " + fs);
                int tag = 0;
                try {
                    tag = getFileTagInOriginSFMap(originSFMap, fs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                System.out.println("tag: " +tag);
                STMap.get(serviceId).add(tag);
                HashMap<Integer, Integer> FNMap = STNMap.get(serviceId);
                FNMap.put(tag, FNMap.getOrDefault(tag, 0) + 1);
            });
        }

        // 2、构建二分依赖图
        int curSrvNum = STMap.size();
        int originSrcNum = originSFMap.size();
        HashMap<Integer, Integer> leftSrvIdMap = new HashMap<>(curSrvNum);
        HashMap<Integer, Integer> leftSrvIdReverseMap = new HashMap<>(curSrvNum);
        int srvStart = 0;
        boolean[][] connectedGraph = new boolean[curSrvNum][originSrcNum];
        for (Map.Entry<Integer, List<Integer>> entry : STMap.entrySet()) {
            Integer leftSrvId = entry.getKey();
            if (!leftSrvIdMap.containsKey(leftSrvId)) {
                leftSrvIdReverseMap.put(srvStart, leftSrvId);
                leftSrvIdMap.put(leftSrvId, srvStart ++);
            }
            // 对当前服务的tag - 数量按数量降序排列，找到数量最大的tag集合
            List<Map.Entry<Integer, Integer>> orderedTNList = STNMap.get(leftSrvId).entrySet().stream()
                    .sorted((e1, e2) -> (e2.getValue() - e1.getValue()))
                    .collect(Collectors.toList());
            int maxTagsNum = orderedTNList.get(0).getValue();
            // 对当前服务，找到数量最多的tag，在二分图添加依赖，可能有多条
            for (int j = 0; j < orderedTNList.size(); j++) {
                Map.Entry<Integer, Integer> tagEntry = orderedTNList.get(j);
                if (tagEntry.getValue() == maxTagsNum) {
                    int tag = tagEntry.getKey();
                    connectedGraph[leftSrvIdMap.get(leftSrvId)][tag] = true;
                } else {
                    break;
                }
            }
        }

//            System.out.println("二分图。。。");
//            for (Map.Entry<Integer, List<Integer>> entry : STMap.entrySet()) {
//                Integer leftSrvId = entry.getKey();
//                System.out.println("左服务: " + leftSrvId);
//                for (int k = 0; k < originSrcNum; k++) {
//                    System.out.print(connectedGraph[leftSrvIdMap.get(leftSrvId)][k] + " ");
//                }
//                System.out.println();
//            }

        // 3、搜索极大二分匹配
        boolean[] ifRightAssigned = new boolean[originSrcNum];
        int[] rightAssignedTo = new int[originSrcNum];
        Arrays.fill(rightAssignedTo, -1);
        // 遍历左边二分图的节点
        for (int j = 0; j < curSrvNum; j++) {
            Arrays.fill(ifRightAssigned, false);
            if (find(j, originSrcNum, connectedGraph, rightAssignedTo, ifRightAssigned)) {
//                    System.out.println("left assigned - " + j);
            }
        }

        // 4、根据极大二分分配方案，计算代码文件的move次数
        int[] leftAssignTo = new int[curSrvNum];
        Arrays.fill(leftAssignTo, -1);
        for (int j = 0; j < originSrcNum; j++) {
            if (rightAssignedTo[j] >= 0) {
                leftAssignTo[rightAssignedTo[j]] = j;
            }
//                System.out.println("originSrv " + j + " assign to curSrv: " + rightAssignedTo[j]);
        }
        int moveNum = 0;
        for (int j = 0; j < curSrvNum; j++) {
            Integer serviceId = leftSrvIdReverseMap.get(j);
            if (ifPrint)
                System.out.println("  serviceId: " + serviceId + " files: " + SFMap.get(serviceId).size());
            if (leftAssignTo[j] >= 0) {
                // 已匹配的curSrv，只move非tag的file
                int tag = leftAssignTo[j];
                List<Integer> tagList = STMap.get(serviceId);
                long count = tagList.stream().filter(t -> t != tag).count();

                int t = 0;
                int fileNum = 0;
                for (Map.Entry<String, List<String>> entry : originSFMap.entrySet()) {
                    fileNum = entry.getValue().size();
                    if (t == tag) break;
                    t ++;
                }

//                    System.out.println("  匹配origin: " + tag + " ; originFiles: " + fileNum + " ; add: " + count);
                moveNum += count;
            } else {
                // 未匹配的curSrv，全部move
                moveNum += SFMap.get(serviceId).size();
//                    System.out.println("  未匹配, add: " + SFMap.get(serviceId).size());
            }
        }

        if (ifPrint)
            System.out.println("move count : " + moveNum + " ; MoJoFM = " + (1 - moveNum * 1.0 / maxMoJo));
        return 1 - moveNum * 1.0 / maxMoJo;
    }

    public static HashMap<Integer, List<String>> getTargetSFMap(JSONObject program) {
        HashMap<Integer, List<String>> SFMap = new HashMap<>();
        JSONArray services = program.getJSONArray("children");
        for (int j = 0; j < services.size(); j++) {
            JSONObject service = services.getJSONObject(j);
            JSONArray fas = service.getJSONArray("children");
            SFMap.put(j + 1, new ArrayList<>());
            List<String> files = new ArrayList<>();
            for (int k = 0; k < fas.size(); k++) {
                JSONArray jsonFiles = fas.getJSONObject(k).getJSONArray("children");
                for (int l = 0; l < jsonFiles.size(); l++) {
                    files.add(jsonFiles.getJSONObject(l).getString("name"));
                }
            }
            SFMap.put(j + 1, files);
//            System.out.println("service: " + (j+1) + " ; files: " + files.size());
        }
        return SFMap;
    }

    // 计算分解准确率
    public static void main(String[] args) throws Exception {
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faPrograms-1.json");
//        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\faRandomPrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);

        // 先计算与原始未合并服务的系统的模块化划分最大的mojo距离
//        String originSrvExcelPath = "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mogu_blog_v2\\origin\\关注点识别\\mogu_blog_v2-service.xlsx";
//        String originSrvExcelPath = "D:\\School\\nju\\毕业设计\\通用\\实验数据\\lamp-cloud\\origin\\关注点识别\\lamp-cloud-service.xlsx";
//        String originSrvExcelPath = "D:\\School\\nju\\毕业设计\\通用\\实验数据\\simplemall\\origin\\关注点识别\\simplemall-service.xlsx";
        String originSrvExcelPath = "D:\\School\\nju\\毕业设计\\通用\\实验数据\\microservices-platform\\origin\\关注点识别\\microservices-platform-service.xlsx";
        List<ServiceData> serviceList = readOriginSrvToFileIdListMap(originSrvExcelPath);
        // 对每个服务，读取其目录下的文件
        Map<String, List<String>> originSFMap = new HashMap<>();
        int allFileNum = 0;
        HashSet<Integer> differentFilesSet = new HashSet<>();
        for (ServiceData sd : serviceList) {
            List<String> files = scan(new File(sd.getPath()), sourceFilter);
            originSFMap.put(sd.getName(), files);
            differentFilesSet.add(files.size());
            allFileNum += files.size();
        }
        // 不考虑join操作
        int maxMoJo = allFileNum - differentFilesSet.size();
        System.out.println("max mojo: " + maxMoJo);

        for (int i = 1; i < programs.size(); i++) {
            // 解析出每个服务下的文件列表
            JSONObject program = programs.getJSONObject(i);
            System.out.println(program.getString("name") + " -------------------------------------------------------------");
            HashMap<Integer, List<String>> SFMap = getTargetSFMap(program);
            getMaximalDichotomyMatchingMoJoFM(SFMap, originSFMap, maxMoJo, true);
        }
    }
}
