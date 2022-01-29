package nsga;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.datastructure.Chromosome;
import nsga.datastructure.FunctionalAtom;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PreProcessLoadData {

    /**
     * 代码文件 - 主题概率分布
     */
    public static List<List<Double>> fileTopic;
    /**
     * 关注点 - TC值
     */
    public static Map<Integer, Double> concernTCMap;
    /**
     * 已识别的关注点列表
     */
    public static Set<Integer> concerns;
    /**
     * 全局代码文件列表
     */
    public static List<String> globalFileList;
    /**
     * 主题与文件的概率阈值
     */
    public static double file_threshold = 0.006;
    /**
     * 过载的关注点分数阈值
     */
    public static double overload_threshold = 10;
    /**
     * 适应度模块数量阈值
     */
    public static double moduleNum_threshold = 1;

    /**
     * 计算SMQ的文件列表，调用矩阵
     */
//    static String currentServicePath = "D:\\Development\\idea_projects\\microservices-platform-master\\zlt-uaa";
    static String projectPath = "D:\\Development\\idea_projects\\microservices-platform-master";
    static String callGraphPath = "D:\\Desktop\\allCallGraph.json";
    public static List<String> allFileList;
    public static int[][] allCallMatrix;
//    public static double originSMQ = 0;

    /**
     * 初始化种群
     */
    static String clusterFile = "D:\\Desktop\\cluster.json";
    public static List<FunctionalAtom> clusters;
    public static int faNums;
    /**
     * 已产生的基因型记录
     */
    public static HashSet<Chromosome> historyRecord;

    static {
        try {
            fileTopic = readFileTopicMap();
            concernTCMap = readConcernTCMap();
            globalFileList = readGlobalFileList();
            concerns = concernTCMap.keySet();

            generateCallMatrix();
//            List<String> curSrv = scan(new File(currentServicePath), filter);
//            FunctionalAtom fa = new FunctionalAtom(curSrv);
//            GroupItem gi = new GroupItem(new ArrayList<>() {{add(fa);}});
//            Individual individual = new Individual(new ArrayList<>() {{add(gi);}});
//            originSMQ = SMQMetric(individual);

            readCluster(clusterFile);

            historyRecord = new HashSet<>();

            // 初始方案可视化数据
            PostProcessShowData.outputPrograms = new ArrayList<>();
            PostProcessShowData.Program origin = new PostProcessShowData.Program("originProgram");
            PostProcessShowData.SubService originService = new PostProcessShowData.SubService("originService", new ArrayList<>());
            for (int i = 0; i < faNums; i++) {
                originService.children.add(new PostProcessShowData.FAFile(
                        "FA - " + i, clusters.get(i).fileList
                ));
            }
            origin.children = new ArrayList<>();
            origin.children.add(originService);
            PostProcessShowData.outputPrograms.add(origin);

            PostProcessShowData.ObjectiveFile objectiveFile = new PostProcessShowData.ObjectiveFile(
                    -1, 1.13, 1.0
            );
            PostProcessShowData.outputObjectives = new ArrayList<>();
            PostProcessShowData.outputObjectives.add(objectiveFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取model-final.theta：文件与主题的概率分布
     * @return
     */
    public static List<List<Double>> readFileTopicMap() throws IOException {
        String theta = "D:\\Development\\idea_projects\\codeTopics\\src\\test\\example\\model-final.theta";
        List<List<Double>> result = new ArrayList<>();
        FileInputStream is = new FileInputStream(theta);
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            List<Double> dataList = Arrays.stream(line.split(" "))
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
            result.add(dataList);
        }
        is.close();
        isr.close();
        br.close();
        System.out.println("file - topic加载成功");
        return result;
    }

    /**
     * 读取关注点 - tc值信息
     */
    public static Map<Integer, Double> readConcernTCMap() throws IOException {
        String concernDir = "D:\\Development\\idea_projects\\codeTopics\\concern.txt";
        Map<Integer, Double> concernTCMap = new HashMap<>();

        FileInputStream is = new FileInputStream(concernDir);
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            String[] datas = line.split(" ");
            concernTCMap.put(Integer.parseInt(datas[0]), Double.parseDouble(datas[1]));
        }
        is.close();
        isr.close();
        br.close();
        System.out.println("concern - tc加载成功");
        return concernTCMap;
    }

    /**
     * 读取files.flist
     * @return
     */
    public static List<String> readGlobalFileList() throws IOException {
        String filenameDir = "D:\\Development\\idea_projects\\codeTopics\\src\\test\\example\\files.flist";
        System.out.println("global file list加载成功");
        return readFileList(filenameDir);
    }

    /**
     * 读取指定目录下的文件，返回一个list
     * @return
     */
    public static List<String> readFileList(String path) throws IOException {
        List<String> fileList = new ArrayList<>();
        FileInputStream is = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            fileList.add(line);
        }
        is.close();
        isr.close();
        br.close();
        return fileList;
    }

    /**
     * 基于路径和过滤规则扫描指定目标下的全部代码文件，返回一个list
     * @param file
     * @param filter
     * @return
     */
    public static List<String> scan(File file, FileFilter filter) {
        File[] files = file.listFiles(filter);
        List<String> fileList = new ArrayList<>();
        for (File f : files) {
            if (f.isFile()) {
                fileList.add(f.getAbsolutePath());
            } else {
                fileList.addAll(scan(f, filter));
            }
        }
        return fileList;
    }

    /**
     * 读取指定路径的文件到字符串中
     * @param path
     * @return
     */
    public static String readFile(String path) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream is = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        is.close();
        isr.close();
        br.close();
        return stringBuilder.toString();
    }

    /**
     * 生成SMQ的call矩阵
     * @throws IOException
     */
    public static void generateCallMatrix() throws IOException {
        FileFilter filter = pathname -> (pathname.getName().endsWith(".java") || pathname.isDirectory())
                && !pathname.getName().toLowerCase(Locale.ROOT).endsWith("test");
        allFileList = scan(new File(projectPath), filter);

        int len = allFileList.size();
        allCallMatrix = new int[len][len];
        JSONArray jsonArray = JSONArray.parseArray(readFile(callGraphPath));
        for (int i = 0; i < jsonArray.size(); i ++) {
            String objStr = jsonArray.get(i).toString();
            objStr = objStr.substring(1, objStr.length() - 1);
            int[] arr = Arrays.stream(objStr.split(",")).mapToInt(Integer::parseInt).toArray();
            allCallMatrix[i] = arr;
        }
    }

    /**
     * 读取功能原子聚类结果
     * @param clusterFile
     * @return
     */
    public static void readCluster(String clusterFile) throws IOException {
        String clusterStr = readFile(clusterFile);
        JSONObject jsonObject = JSONObject.parseObject(clusterStr);
        JSONArray fileArray = jsonObject.getJSONArray("clusters");
        clusters = new ArrayList<>();
        for (int i = 0; i < fileArray.size(); i++) {
            JSONArray cluster = fileArray.getJSONArray(i);
            List<String> clusterFileList = cluster.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            clusters.add(new FunctionalAtom(clusterFileList));
        }
        faNums = clusters.size();
        System.out.println("读取功能原子聚类结果成功！");
//		for (int i = 0; i < clusters.size(); i++) {
//			System.out.println("功能原子 "  + (i+1) + "th --------------------------------------------------------------");
//			for (String file : clusters.get(i)) {
//				System.out.println(file);
//			}
//		}
    }

}
