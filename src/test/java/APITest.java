import org.junit.Test;

import java.util.*;

import static nsga.PreProcessLoadData.calculateCosineSim;

public class APITest {


    @Test
    public void cosineSimTest() {
        HashMap<String, Integer> wordFrequency1 = new HashMap<String, Integer>() {{
            put("2010", 1);
            put("世博会", 3);
            put("中国", 1);
            put("举行", 1);
        }};
        HashMap<String, Integer> wordFrequency2 = new HashMap<String, Integer>() {{
            put("2005", 1);
            put("世博会", 2);
            put("1970", 1);
            put("日本", 1);
            put("举行", 1);
        }};
        HashMap<String, Integer> wordFrequency3 = new HashMap<String, Integer>() {{
            put("2010", 1);
            put("世博会", 2);
        }};
        // 合并词汇表，得到词汇向量set
        List<String> setList1 = new ArrayList<>(wordFrequency1.keySet());
//        List<String> setList2 = new ArrayList<>(wordFrequency2.keySet());
        List<String> setList3 = new ArrayList<>(wordFrequency3.keySet());
        HashSet<String> mergeSet = new HashSet<>(setList1);
//        mergeSet.addAll(setList2);
        mergeSet.addAll(setList3);
        List<String> wordSetList = new ArrayList<>(mergeSet);
        int wordSetLen = wordSetList.size();
        // 计算两个文件的词频-tf向量
        double[] tfIdfVec1 = new double[wordSetLen];
        double[] tfIdfVec3 = new double[wordSetLen];
        for (int i = 0; i < wordSetLen; i++) {
            String curWord = wordSetList.get(i);
            if (wordFrequency1.containsKey(curWord)) {
                tfIdfVec1[i] = wordFrequency1.get(curWord) * 1.0 / 6;
            }
            if (wordFrequency3.containsKey(curWord)) {
                tfIdfVec3[i] = wordFrequency3.get(curWord) * 1.0 / 3;
            }
        }
        System.out.println("tf 计算");
        System.out.println(wordSetList);
        System.out.println(Arrays.toString(tfIdfVec1));
        System.out.println(Arrays.toString(tfIdfVec3));
        // 计算每个word在代码文件中的出现数量（这里不是1，就是2），得到idf向量
        int curDocNum = 2;
        for (int i = 0; i < wordSetLen; i++) {
            String curWord = wordSetList.get(i);
            int count = 0;
            if (wordFrequency1.containsKey(curWord)) {
                count ++;
            }
            if (wordFrequency3.containsKey(curWord)) {
                count ++;
            }
            // 分母加1，保证idf不为0
            double idf = Math.log(curDocNum * 1.0 / (count + 1));
            if (wordFrequency1.containsKey(curWord)) {
                // tf和idf向量相乘得到tf-idf向量
                tfIdfVec1[i] *= idf;
            }
            if (wordFrequency3.containsKey(curWord)) {
                // tf和idf向量相乘得到tf-idf向量
                tfIdfVec3[i] = idf;
            }
        }

        System.out.println("idf 计算 ---------------------------------------");
        System.out.println(Arrays.toString(tfIdfVec1));
        System.out.println(Arrays.toString(tfIdfVec3));
        // 计算两个tf-idf向量的余弦相似度
        double sim = calculateCosineSim(tfIdfVec1, tfIdfVec3);
        System.out.println(sim);
    }


}
