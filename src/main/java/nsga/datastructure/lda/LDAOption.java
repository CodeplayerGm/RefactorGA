package nsga.datastructure.lda;


public class LDAOption {
    /*
    "Specify whether we want to estimate model from scratch"
     */
    public boolean est = false;

    /*"Specify whether we want to continue the last estimation"
     */
    public boolean estc = false;

    /*Specify whether we want to do inference
     */
    public boolean inf = true;

    /*
    Specify directory
     */
    public String dir = "";

    /*
    Specify data file
     */
    public String dfile = "";

    /*
    Specify the model name
     */
    public String modelName = "";

    /*
    Specify alpha
     */
    public double alpha = -1.0;

    /*
    Specify beta
     */
    public double beta = -1.0;

    /*
    Specify the number of topics
     */
    public int K = 100;

    /*
    Specify the number of iterations
     */
    public int niters = 1000;

    /*
    Specify the number of steps to save the model since the last save
     */
    public int savestep = 100;

    /*
    Specify the number of most likely words to be printed for each topic
     */
    public int twords = 100;

    /*
    Specify whether we include raw data in the input
     */
    public boolean withrawdata = false;

    /*
    Specify the wordmap file
     */
    public String wordMapFileName = "wordmap.txt";
}
