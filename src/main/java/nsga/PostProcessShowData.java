package nsga;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static nsga.Reporter.outputDirectory;

public class PostProcessShowData {

//    public static final String outputProgramJson = "D:\\Development\\FrontBackProject\\refactorFront\\static\\refactorPrograms.json";
//    public static final String outputObjectiveJson = "D:\\Development\\FrontBackProject\\refactorFront\\static\\objectives.json";
    public static final String outputProgramJson = outputDirectory + File.separator + "refactorPrograms.json";
    public static final String outputObjectiveJson = outputDirectory + File.separator + "objectives.json";

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
            this.name = name.substring(81);
        }
    }

    public static class ObjectiveFile implements Serializable {
        public double concern;
        public double smq;
        public double mojo;

        public ObjectiveFile(double concern, double smq, double mojo) {
            this.concern = concern;
            this.smq = smq;
            this.mojo = mojo;
        }
    }

    public static List<Program> outputPrograms;
    public static List<ObjectiveFile> outputObjectives;
}
