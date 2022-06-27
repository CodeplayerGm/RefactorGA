package nsga;

public class ParameterConfig {
    /**
     * LDA部分的参数
     */
    /**
     * 主题与文件的概率阈值
     */
    public static double file_threshold = 0.012;
    /**
     * 主题与服务的概率阈值
     */
    public static double service_threshold = 0.048;
    /**
     * 过载的关注点分数阈值
     */
    public static double overload_threshold = 5;

    /**
     * 遗传算法参数
     */
    /**
     * 控制过载个体在种群中的最大占比
     */
    public static float overloadRemainThreshold = 0.2f;
    public static float crossoverProb = 0.8f;
    public static float mutationProb = 0.4f;
    public static float breakProb = 0.3f;
    /**
     * FA搜索参数
     */
    public static int faMaxGeneration = 200;
    public static int faMaxPopulationSize = 100;
    public static int faMaxRecord = 50000;
//    public static double faMaxFrontMajority = 0.8;
    /**
     * File搜索参数
     */
    public static int fileMaxGeneration = 500;
    public static int fileMaxPopulationSize = 300;
    public static int fileMaxRecord = 250000;
//    public static double fileMaxFrontMajority = 0.99;
    /**
     * 实验重复次数
     */
    public static int experimentTimes = 30;

    /**
     * 计算MoJoFM分解准确率使用到的文件前缀路径
     */
//    public static String originFilePrefix = "D:\\Development\\idea_projects\\mogu_blog_v2\\";
//    public static String curFilePrefix = "D:\\Development\\idea_projects\\mogu_blog_v2-merge2\\merge2\\";
//    public static String originFilePrefix = "D:\\Development\\idea_projects\\mogu_blog_v2\\";
//    public static String curFilePrefix = "D:\\Development\\idea_projects\\mogu_blog_v2-merge3\\merge3\\";
    public static String originFilePrefix = "D:\\Development\\idea_projects\\microservices-platform\\";
    public static String curFilePrefix = "D:\\Development\\idea_projects\\microservices-platform-merge2\\merge2\\";


}
