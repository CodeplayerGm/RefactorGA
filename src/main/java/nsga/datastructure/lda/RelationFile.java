package nsga.datastructure.lda;

import java.util.List;

public class RelationFile {
    public List<String> fileSequence;
    public int[][] dependGraph;

    public RelationFile(List<String> fileSequence, int[][] dependGraph) {
        this.fileSequence = fileSequence;
        this.dependGraph = dependGraph;
    }
}
