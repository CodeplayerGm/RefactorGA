package nsga;

import nsga.datastructure.IntegerAllele;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static nsga.Reporter.outputDirectory;

public class PostProcessShowData {

    /**
     * FA粒度重构方法的输出路径
     */
    public static final String outputFAProgramJson = outputDirectory + File.separator + "faPrograms-";
    public static final String outputFAObjectiveTxt = outputDirectory + File.separator + "faObjectives-";
    public static final String outputFAFrontTxt = outputDirectory + File.separator + "faFront-";
    public static final String outputFACostTxt = outputDirectory + File.separator + "faCost-";
    /**
     * FA随机搜索的输出路径
     */
    public static final String outputFARandomProgramJson = outputDirectory + File.separator + "faRandomPrograms-";
    public static final String outputFARandomObjectiveTxt = outputDirectory + File.separator + "faRandomObjectives-";
    public static final String outputFARandomFrontTxt = outputDirectory + File.separator + "faRandomFront-";
    public static final String outputFARandomCostTxt = outputDirectory + File.separator + "faRandomCost-";
    /**
     * 代码文件粒度重构方法的输出路径
     */
    public static final String outputFileProgramJson = outputDirectory + File.separator + "filePrograms-";
    public static final String outputFileObjectiveTxt = outputDirectory + File.separator + "fileObjectives-";
    public static final String outputFileFrontTxt = outputDirectory + File.separator + "fileFront-";
    public static final String outputFileCostTxt = outputDirectory + File.separator + "fileCost-";
    /**
     * 代码文件随机搜索的输出路径
     */
    public static final String outputFileRandomProgramJson = outputDirectory + File.separator + "fileRandomPrograms-";
    public static final String outputFileRandomObjectiveTxt = outputDirectory + File.separator + "fileRandomObjectives-";
    public static final String outputFileRandomFrontTxt = outputDirectory + File.separator + "fileRandomFront-";
    public static final String outputFileRandomCostTxt = outputDirectory + File.separator + "fileRandomCost-";


    public static class Program implements Serializable {
        public String name;
        public List<SubService> children;

        public Program(String name) {
            this.name = name;
        }
    }

    public static class SubService implements Serializable {
        public String name;
        public List<FAFile> children;

        public SubService(String name, List<FAFile> children) {
            this.name = name;
            this.children = children;
        }
    }

    public static class FAFile implements Serializable {
        public String name;
        public List<LeafFile> children;

        public FAFile(String name, List<String> children) {
            this.name = name;
            this.children = children.stream().map(LeafFile::new).collect(Collectors.toList());
        }
    }

    public static class LeafFile implements Serializable {
        public String name;

        public LeafFile(String name) {
            this.name = name;
        }
    }

    public static List<List<Double>> outputObjectiveData;
    public static List<List<IntegerAllele>> outputFrontData;
    public static List<Program> outputPrograms;
}
