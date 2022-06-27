package nsga.objective;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsga.datastructure.Chromosome;
import nsga.datastructure.excel.ServiceData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static nsga.PreProcessLoadData.*;
import static nsga.Service.splitChromosomeToServiceFileIdMap;
import static nsga.objective.MoJoFMFAObjective.*;

public class MoJoFMFileObjective extends AbstractObjectiveFunction {

    public MoJoFMFileObjective() {
        this.objectiveFunctionTitle = "MoJoFM File Index";
    }

    @Override
    public double getValue(Chromosome chromosome) {
        HashMap<Integer, List<Integer>> srvFilesMap = splitChromosomeToServiceFileIdMap(chromosome);
        int maxSrvFileSize = srvFilesMap.values().stream()
                .mapToInt(List::size).max().getAsInt();
        return maxSrvFileSize * 1.0 / overloadServiceFileList.size();
    }

    public static void main(String[] args) throws Exception {
//        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\filePrograms.json");
        String jsonString = readFile("D:\\Development\\idea_projects\\NSGA-II\\output\\fileRandomPrograms.json");
        JSONArray programs = JSONArray.parseArray(jsonString);

        // 先计算与原始未合并服务的系统的模块化划分最大的mojo距离
        String originSrvExcelPath = "D:\\School\\nju\\毕业设计\\通用\\实验数据\\mogu_blog_v2\\origin\\关注点识别\\mogu_blog_v2-service.xlsx";
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
