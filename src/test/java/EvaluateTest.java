import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.datastructure.excel.ServiceData;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static nsga.ParameterConfig.experimentTimes;
import static nsga.PreProcessLoadData.*;
import static nsga.Service.getFourBitsDoubleString;
import static nsga.Service.getTwoBitsDoubleString;
import static nsga.objective.MoJoFMFAObjective.*;
import static nsga.objective.SMQFAObjective.*;
import static nsga.objective.SemanticFAObjective.getAllSrvSemanticCohesion;
import static nsga.objective.SemanticFAObjective.getAllSrvSemanticCoupling;

public class EvaluateTest {

    static String[] experimentPaths = new String[]{
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mall-swarm\\merge1\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mall-swarm\\merge2\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mogu_blog_v2\\merge2\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mogu_blog_v2\\merge3\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\simplemall\\merge3\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\simplemall\\merge4\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\lamp-cloud\\merge1\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\lamp-cloud\\merge2\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\microservices-platform\\merge2\\搜索结果",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\microservices-platform\\merge3\\搜索结果"
    };

    static String[] originServicePaths = new String[] {
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mall-swarm\\origin\\关注点识别\\mall-swam-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mall-swarm\\origin\\关注点识别\\mall-swam-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mogu_blog_v2\\origin\\关注点识别\\mogu_blog_v2-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mogu_blog_v2\\origin\\关注点识别\\mogu_blog_v2-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\simplemall\\origin\\关注点识别\\simplemall-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\simplemall\\origin\\关注点识别\\simplemall-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\lamp-cloud\\origin\\关注点识别\\lamp-cloud-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\lamp-cloud\\origin\\关注点识别\\lamp-cloud-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\microservices-platform\\origin\\关注点识别\\microservices-platform-service.xlsx",
            "D:\\School\\nju\\毕业设计\\通用\\实验数据\\microservices-platform\\origin\\关注点识别\\microservices-platform-service.xlsx"
    };

    static double[] originStMQs = new double[] {
            0.0014, 0.0003, 0.0007, 0.0008, 0.0056, 0.0054, 0.0003, 0.0003, 0.0014, 0.0017
    };

    static double[] originSeMQs = new double[] {
            0.2649, 0.2629, 0.2779, 0.2740, 0.2708, 0.2723, 0.2728, 0.2756, 0.2682, 0.2723
    };

    static int[] faPopulationParams = new int[] {300, 300, 300, 300, 100, 100, 300, 300, 100, 100};

    static int[] filePopulationParams = new int[] {500, 500, 500, 500, 300, 300, 300, 300, 300, 300};

    static int runtimes = 30;

    public static double calculateAvgObjectiveDistance(String experimentPath, int population) throws IOException {
        double minDis = 9999, avgDis = 0;
        double avgSMQ = 0, avgSemantic = 0, avgMoJoFM = 0;
        double bestSmq = 0, bestSemantic = 0, bestMoJoFm = 0;
        double count = 0;
        for (int i = 1; i <= runtimes; i++) {
            String data = readFile(experimentPath + i + ".txt");
            String[] programs = data.split("\n");
            count += programs.length - 1;
            for (int j = 1; j < programs.length; j++) {
                String es = programs[j];
                List<Double> objList = Arrays.stream(es.split(" ")).map(Double::parseDouble).collect(Collectors.toList());
                double result = 0;
                for (double od : objList) {
                    result += (1 - od) * (1 - od);
                }
                result = Math.sqrt(result);
                avgSMQ += objList.get(0);
                avgSemantic += objList.get(1);
                avgMoJoFM += objList.get(2);
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

        System.out.println("平均有效方案数量：" + getFourBitsDoubleString(count / experimentTimes) + " ; 平均占比：" + getFourBitsDoubleString(count * 100 / experimentTimes / population));
//        System.out.println("  最优解距离：" + getFourBitsDoubleString(minDis) + " bestSMQ = " + getFourBitsDoubleString(bestSmq) +
//                " bestSemantic = " + getFourBitsDoubleString(bestSemantic) + " bestMoJo = " + getFourBitsDoubleString(bestMoJoFm));
//        System.out.println("  平均SMQ：" + getFourBitsDoubleString(avgSMQ / count) +
//                " ; 平均semantic：" + getFourBitsDoubleString(avgSemantic / count) +
//                " ; 平均mojo：" + getFourBitsDoubleString(avgMoJoFM / count));
        System.out.println("  平均距离：" + getFourBitsDoubleString(avgDis / count));
        return avgDis / count;
    }

    public static void getSearchSpace(String prefixPath) throws IOException {
        double overloadSum = 0, notOverloadSum = 0;
        for (int i = 1; i <= runtimes; i++) {
            String data = readFile(prefixPath + i + ".txt");
            String[] datas = data.split("\n");
            notOverloadSum +=Integer.parseInt(datas[1].substring(12));
            overloadSum += Integer.parseInt(datas[2].substring(9));
        }
        System.out.println("  平均不过载：" + getFourBitsDoubleString(notOverloadSum / runtimes));
        System.out.println("  平均过载：" + getFourBitsDoubleString(overloadSum / runtimes));
        System.out.println("  平均总大小：" + getFourBitsDoubleString((overloadSum + notOverloadSum) / runtimes));
    }

    public static void rq1() throws IOException {
        double faGADis = 0, faRandomDis = 0, fileGADis = 0, fileRandomDis = 0;

        for (int i = 0; i < experimentPaths.length; i++) {
            String curPath = experimentPaths[i];
            System.out.println("==========================================================================================");
            System.out.println(curPath.substring("D:\\School\\nju\\毕业设计\\通用\\实验数据\\".length(), curPath.length() - 5));

            System.out.println("fa-ga -------------------------------------");
            faGADis += calculateAvgObjectiveDistance(curPath + "\\faObjectives-", faPopulationParams[i]);
            getSearchSpace(curPath + "\\faCost-");
            System.out.println("fa-random -------------------------------------");
            faRandomDis += calculateAvgObjectiveDistance(curPath + "\\faRandomObjectives-", faPopulationParams[i]);
            getSearchSpace(curPath + "\\faRandomCost-");
            System.out.println("file-ga -------------------------------------");
            fileGADis += calculateAvgObjectiveDistance(curPath + "\\fileObjectives-", filePopulationParams[i]);
            getSearchSpace(curPath + "\\fileCost-");
            System.out.println("file-random -------------------------------------");
            fileRandomDis += calculateAvgObjectiveDistance(curPath + "\\fileRandomObjectives-", filePopulationParams[i]);
            getSearchSpace(curPath + "\\fileRandomCost-");
        }

        System.out.println("功能原子GA平均距离：" + getFourBitsDoubleString(faGADis / experimentPaths.length));
        System.out.println("功能原子随机平均距离：" + getFourBitsDoubleString(faRandomDis / experimentPaths.length));
        System.out.println("代码文件GA平均距离：" + getFourBitsDoubleString(fileGADis / experimentPaths.length));
        System.out.println("代码文件随机平均距离：" + getFourBitsDoubleString(fileRandomDis / experimentPaths.length));
    }

    public static void calculateMaxQualityPercentage(double distance) {
        System.out.println(1 - Math.sqrt(distance * distance / 3));
    }

    public static void rq2() throws IOException {
        double totalFAGA = 0, totalFARandom = 0, totalFileGA = 0, totalFileRandom = 0;
        for (int i = 0; i < experimentPaths.length; i++) {
            String curPath = experimentPaths[i];
            System.out.println("======================================================================================");
            System.out.println(curPath.substring("D:\\School\\nju\\毕业设计\\通用\\实验数据\\".length(), curPath.length() - 5));

            // 计算原始方案的maxMoJo
            String originSrv = originServicePaths[i];
            List<ServiceData> serviceList = readOriginSrvToFileIdListMap(originSrv);
            Map<String, List<String>> originSFMap = new HashMap<>();
            int allFileNum = 0;
            HashSet<Integer> differentFilesSet = new HashSet<>();
            for (ServiceData sd : serviceList) {
                List<String> files = scan(new File(sd.getPath()), sourceFilter);
                originSFMap.put(sd.getName(), files);
                differentFilesSet.add(files.size());
                allFileNum += files.size();
            }
            int maxMoJo = allFileNum - differentFilesSet.size();

            double totalAccuracy;
            int count;

            // FA-NSGA
            totalAccuracy = 0;
            count = 0;
            for (int j = 1; j <= runtimes; j++) {
                String faJsonString = readFile(curPath + "\\faPrograms-" + j + ".json");
                JSONArray faPrograms = JSONArray.parseArray(faJsonString);
                for (int k = 1; k < faPrograms.size(); k++) {
                    JSONObject program = faPrograms.getJSONObject(k);
                    HashMap<Integer, List<String>> SFMap = getTargetSFMap(program);
                    totalAccuracy += getMaximalDichotomyMatchingMoJoFM(SFMap, originSFMap, maxMoJo, false);
                    count ++;
                }
            }
            totalFAGA += totalAccuracy * 100 / count;
            System.out.println("FA-NSGA-平均分解准确率：" + getFourBitsDoubleString(totalAccuracy * 100 / count));

            // FA-Random
            totalAccuracy = 0;
            count = 0;
            for (int j = 1; j <= runtimes; j++) {
                String faJsonString = readFile(curPath + "\\faRandomPrograms-" + j + ".json");
                JSONArray faPrograms = JSONArray.parseArray(faJsonString);
                for (int k = 1; k < faPrograms.size(); k++) {
                    JSONObject program = faPrograms.getJSONObject(k);
                    HashMap<Integer, List<String>> SFMap = getTargetSFMap(program);
                    totalAccuracy += getMaximalDichotomyMatchingMoJoFM(SFMap, originSFMap, maxMoJo, false);
                    count ++;
                }
            }
            totalFARandom += totalAccuracy * 100 / count;
            System.out.println("FA-Random-平均分解准确率：" + getFourBitsDoubleString(totalAccuracy * 100 / count));

            // File-NSGA
            totalAccuracy = 0;
            count = 0;
            for (int j = 1; j <= runtimes; j++) {
                String fileJsonString = readFile(curPath + "\\filePrograms-" + j + ".json");
                JSONArray filePrograms = JSONArray.parseArray(fileJsonString);
                for (int k = 1; k < filePrograms.size(); k++) {
                    JSONObject program = filePrograms.getJSONObject(k);
                    HashMap<Integer, List<String>> SFMap = getTargetSFMap(program);
                    totalAccuracy += getMaximalDichotomyMatchingMoJoFM(SFMap, originSFMap, maxMoJo, false);
                    count ++;
                }
            }
            totalFileGA += totalAccuracy * 100 / count;
            System.out.println("File-NSGA-平均分解准确率：" + getFourBitsDoubleString(totalAccuracy * 100 / count));

            // File-Random
            totalAccuracy = 0;
            count = 0;
            for (int j = 1; j <= runtimes; j++) {
                String fileJsonString = readFile(curPath + "\\fileRandomPrograms-" + j + ".json");
                JSONArray filePrograms = JSONArray.parseArray(fileJsonString);
                for (int k = 1; k < filePrograms.size(); k++) {
                    JSONObject program = filePrograms.getJSONObject(k);
                    HashMap<Integer, List<String>> SFMap = getTargetSFMap(program);
                    totalAccuracy += getMaximalDichotomyMatchingMoJoFM(SFMap, originSFMap, maxMoJo, false);
                    count ++;
                }
            }
            totalFileRandom += totalAccuracy * 100 / count;
            System.out.println("File-Random-平均分解准确率：" + getFourBitsDoubleString(totalAccuracy * 100 / count));
        }

        System.out.println("整体FA-GA准确率：" + getFourBitsDoubleString(totalFAGA));
        System.out.println("整体FA-Random准确率：" + getFourBitsDoubleString(totalFARandom));
        System.out.println("整体File-GA准确率：" + getFourBitsDoubleString(totalFileGA));
        System.out.println("整体File-Random准确率：" + getFourBitsDoubleString(totalFileRandom));
    }

    public static void rq3() throws IOException {

        double allStMQ = 0, allSeMQ = 0, allRC = 0;
        double StMQImproved = 0, SeMQImproved = 0;

        for (int i = 0; i < experimentPaths.length; i++) {
            String curPath = experimentPaths[i];
            System.out.println("======================================================================================");
            System.out.println(curPath.substring("D:\\School\\nju\\毕业设计\\通用\\实验数据\\".length(), curPath.length() - 5));

            double bestStMQ = 0, bestSeMQ = 0, bestCost = 0, bestDis = 999, avgStMQ = 0, avgSeMQ = 0, avgCost = 0;
            int count = 0;
            double originStMQ = originStMQs[i];
            double originSeMQ = originSeMQs[i];
            for (int j = 1; j <= runtimes; j++) {
                String GAObjs = readFile(curPath + "\\faObjectives-" + j + ".txt");
                String[] GAObjStrs = GAObjs.split("\n");
                for (int k = 1; k < GAObjStrs.length; k++) {
                    List<Double> objList = Arrays.stream(GAObjStrs[k].split(" ")).map(Double::parseDouble).collect(Collectors.toList());
                    double result = 0;
                    for (double od : objList) {
                        result += (1 - od) * (1 - od);
                    }
                    result = Math.sqrt(result);
                    if (result < bestDis) {
                        bestStMQ = objList.get(0);
                        bestSeMQ = objList.get(1);
                        bestCost = objList.get(2);
                    }
                    avgStMQ += objList.get(0);
                    avgSeMQ += objList.get(1);
                    avgCost += objList.get(2);
                    count ++;
                }
            }
            avgStMQ /= count;
            avgSeMQ /= count;
            avgCost /= count;
//            System.out.println("  FA ga avg stmq = 0" + getFourBitsDoubleString(avgStMQ) + "(" + getTwoBitsDoubleString((avgStMQ - originStMQ) / originStMQ * 100) +
//                    "\\%); semq = 0" + getFourBitsDoubleString(avgSeMQ) + "(" + getTwoBitsDoubleString((avgSeMQ - originSeMQ) / originSeMQ * 100) +
//                    "\\%); mojo = 0" + getFourBitsDoubleString(avgCost));
            System.out.println("  FA ga best stmq = 0" + getFourBitsDoubleString(bestStMQ) + "(" + getTwoBitsDoubleString((bestStMQ - originStMQ) / originStMQ * 100) +
                    "\\%); semq = 0" + getFourBitsDoubleString(bestSeMQ) + "(" + getTwoBitsDoubleString((bestSeMQ - originSeMQ) / originSeMQ * 100) +
                    "\\%); mojo = 0" + getFourBitsDoubleString(bestCost));

            bestStMQ = 0; bestSeMQ = 0; bestCost = 0; bestDis = 999; avgStMQ = 0; avgSeMQ = 0; avgCost = 0;
            count = 0; originStMQ = originStMQs[i]; originSeMQ = originSeMQs[i];
            for (int j = 1; j <= runtimes; j++) {
                String RandomObjs = readFile(curPath + "\\faRandomObjectives-" + j + ".txt");
                String[] RandomObjStrs = RandomObjs.split("\n");
                for (int k = 1; k < RandomObjStrs.length; k++) {
                    List<Double> objList = Arrays.stream(RandomObjStrs[k].split(" ")).map(Double::parseDouble).collect(Collectors.toList());
                    double result = 0;
                    for (double od : objList) {
                        result += (1 - od) * (1 - od);
                    }
                    result = Math.sqrt(result);
                    if (result < bestDis) {
                        bestStMQ = objList.get(0);
                        bestSeMQ = objList.get(1);
                        bestCost = objList.get(2);
                    }
                    avgStMQ += objList.get(0);
                    avgSeMQ += objList.get(1);
                    avgCost += objList.get(2);
                    count ++;
                }
            }
            avgStMQ /= count;
            avgSeMQ /= count;
            avgCost /= count;
//            System.out.println("  FA random avg stmq = 0" + getFourBitsDoubleString(avgStMQ) + "(" + getTwoBitsDoubleString((avgStMQ - originStMQ) / originStMQ * 100) +
//                    "\\%); semq = 0" + getFourBitsDoubleString(avgSeMQ) + "(" + getTwoBitsDoubleString((avgSeMQ - originSeMQ) / originSeMQ * 100) +
//                    "\\%); mojo = 0" + getFourBitsDoubleString(avgCost));
            System.out.println("  FA random best stmq = 0" + getFourBitsDoubleString(bestStMQ) + "(" + getTwoBitsDoubleString((bestStMQ - originStMQ) / originStMQ * 100) +
                    "\\%); semq = 0" + getFourBitsDoubleString(bestSeMQ) + "(" + getTwoBitsDoubleString((bestSeMQ - originSeMQ) / originSeMQ * 100) +
                    "\\%); mojo = 0" + getFourBitsDoubleString(bestCost));


            bestStMQ = 0; bestSeMQ = 0; bestCost = 0; bestDis = 999; avgStMQ = 0; avgSeMQ = 0; avgCost = 0;
            count = 0; originStMQ = originStMQs[i]; originSeMQ = originSeMQs[i];
            for (int j = 1; j <= runtimes; j++) {
                String RandomObjs = readFile(curPath + "\\fileObjectives-" + j + ".txt");
                String[] RandomObjStrs = RandomObjs.split("\n");
                for (int k = 1; k < RandomObjStrs.length; k++) {
                    List<Double> objList = Arrays.stream(RandomObjStrs[k].split(" ")).map(Double::parseDouble).collect(Collectors.toList());
                    double result = 0;
                    for (double od : objList) {
                        result += (1 - od) * (1 - od);
                    }
                    result = Math.sqrt(result);
                    if (result < bestDis) {
                        bestStMQ = objList.get(0);
                        bestSeMQ = objList.get(1);
                        bestCost = objList.get(2);
                    }
                    avgStMQ += objList.get(0);
                    avgSeMQ += objList.get(1);
                    avgCost += objList.get(2);
                    count ++;
                }
            }
            avgStMQ /= count;
            avgSeMQ /= count;
            avgCost /= count;
//            System.out.println("  File ga avg stmq = 0" + getFourBitsDoubleString(avgStMQ) + "(" + getTwoBitsDoubleString((avgStMQ - originStMQ) / originStMQ * 100) +
//                    "\\%); semq = 0" + getFourBitsDoubleString(avgSeMQ) + "(" + getTwoBitsDoubleString((avgSeMQ - originSeMQ) / originSeMQ * 100) +
//                    "\\%); mojo = 0" + getFourBitsDoubleString(avgCost));
            System.out.println("  File ga best stmq = 0" + getFourBitsDoubleString(bestStMQ) + "(" + getTwoBitsDoubleString((bestStMQ - originStMQ) / originStMQ * 100) +
                    "\\%); semq = 0" + getFourBitsDoubleString(bestSeMQ) + "(" + getTwoBitsDoubleString((bestSeMQ - originSeMQ) / originSeMQ * 100) +
                    "\\%); mojo = 0" + getFourBitsDoubleString(bestCost));


            bestStMQ = 0; bestSeMQ = 0; bestCost = 0; bestDis = 999; avgStMQ = 0; avgSeMQ = 0; avgCost = 0;
            count = 0; originStMQ = originStMQs[i]; originSeMQ = originSeMQs[i];
            for (int j = 1; j <= runtimes; j++) {
                String RandomObjs = readFile(curPath + "\\fileRandomObjectives-" + j + ".txt");
                String[] RandomObjStrs = RandomObjs.split("\n");
                for (int k = 1; k < RandomObjStrs.length; k++) {
                    List<Double> objList = Arrays.stream(RandomObjStrs[k].split(" ")).map(Double::parseDouble).collect(Collectors.toList());
                    double result = 0;
                    for (double od : objList) {
                        result += (1 - od) * (1 - od);
                    }
                    result = Math.sqrt(result);
                    if (result < bestDis) {
                        bestStMQ = objList.get(0);
                        bestSeMQ = objList.get(1);
                        bestCost = objList.get(2);
                    }
                    avgStMQ += objList.get(0);
                    avgSeMQ += objList.get(1);
                    avgCost += objList.get(2);
                    count ++;
                }
            }
            avgStMQ /= count;
            avgSeMQ /= count;
            avgCost /= count;
//            System.out.println("  File random avg stmq = 0" + getFourBitsDoubleString(avgStMQ) + "(" + getTwoBitsDoubleString((avgStMQ - originStMQ) / originStMQ * 100) +
//                    "\\%); semq = 0" + getFourBitsDoubleString(avgSeMQ) + "(" + getTwoBitsDoubleString((avgSeMQ - originSeMQ) / originSeMQ * 100) +
//                    "\\%); mojo = 0" + getFourBitsDoubleString(avgCost));
            System.out.println("  File random best stmq = 0" + getFourBitsDoubleString(bestStMQ) + "(" + getTwoBitsDoubleString((bestStMQ - originStMQ) / originStMQ * 100) +
                    "\\%); semq = 0" + getFourBitsDoubleString(bestSeMQ) + "(" + getTwoBitsDoubleString((bestSeMQ - originSeMQ) / originSeMQ * 100) +
                    "\\%); mojo = 0" + getFourBitsDoubleString(bestCost));
            allStMQ += avgStMQ; allSeMQ += avgSeMQ; allRC += avgCost;
            StMQImproved += ((avgStMQ - originStMQ) / originStMQ * 100);
            SeMQImproved += ((avgSeMQ - originSeMQ) / originSeMQ * 100);
        }

        System.out.println("平均StMQ: " + getFourBitsDoubleString(allStMQ / experimentPaths.length));
        System.out.println("平均StMQ改进: " + getTwoBitsDoubleString(StMQImproved / experimentPaths.length));
        System.out.println("平均SeMQ: " + getFourBitsDoubleString(allSeMQ / experimentPaths.length));
        System.out.println("平均SeMQ改进: " + getTwoBitsDoubleString(SeMQImproved / experimentPaths.length));
        System.out.println("平均RC: " + getFourBitsDoubleString(allRC / experimentPaths.length));
    }

    public static void rq4() throws IOException {

        double faNSGATime = 0, faRandomTime = 0;
        double fileNSGATime = 0, fileRandomTime = 0;

        for (int i = 0; i < experimentPaths.length; i++) {
            String curPath = experimentPaths[i];
            System.out.println("======================================================================================");
            System.out.println(curPath.substring("D:\\School\\nju\\毕业设计\\通用\\实验数据\\".length(), curPath.length() - 5));

            for (int j = 1; j <= runtimes; j++) {
                String timeStr = readFile(curPath + "\\faCost-" + j + ".txt").split("\n")[3].substring(5);
                faNSGATime += Double.parseDouble(timeStr);
                timeStr = readFile(curPath + "\\faRandomCost-" + j + ".txt").split("\n")[3].substring(5);
                faRandomTime += Double.parseDouble(timeStr);
                timeStr = readFile(curPath + "\\fileCost-" + j + ".txt").split("\n")[3].substring(5);
                fileNSGATime += Double.parseDouble(timeStr);
                timeStr = readFile(curPath + "\\fileRandomCost-" + j + ".txt").split("\n")[3].substring(5);
                fileRandomTime += Double.parseDouble(timeStr);
            }
        }

        System.out.println("faCost = " + getTwoBitsDoubleString(faNSGATime / runtimes / experimentPaths.length));
        System.out.println("faRandomCost = " + getTwoBitsDoubleString(faRandomTime / runtimes / experimentPaths.length));
        System.out.println("fileCost = " + getTwoBitsDoubleString(fileNSGATime / runtimes / experimentPaths.length));
        System.out.println("fileRandomCost = " + getTwoBitsDoubleString(fileRandomTime / runtimes / experimentPaths.length));
    }

    public static void main(String[] args) throws IOException {
//        rq1();
//        rq2();
//        rq3();
        rq4();
    }

}
