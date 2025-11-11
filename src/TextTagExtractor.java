import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class TextTagExtractor {
    private final Set<String> stopWords = new HashSet<>();
    private static final Pattern TOKEN = Pattern.compile("[a-zA-Z]+");

    public void loadStopWords(Path stopFile) throws IOException {
        stopWords.clear();
        try (BufferedReader br = Files.newBufferedReader(stopFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                String w = line.trim().toLowerCase();
                if (!w.isEmpty()) stopWords.add(w);
            }
        }
    }

    public Map<String, Integer> extractTags(Path textFile) throws IOException {
        Map<String, Integer> freq = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(textFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = TOKEN.matcher(line.toLowerCase());
                while (m.find()) {
                    String word = m.group();
                    if (!stopWords.contains(word)) {
                        freq.merge(word, 1, Integer::sum);
                    }
                }
            }
        }
        return freq;
    }

    public List<Map.Entry<String, Integer>> sortByFrequency(Map<String, Integer> freq) {
        return freq.entrySet()
                .stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    return (cmp != 0) ? cmp : a.getKey().compareTo(b.getKey());
                })
                .collect(Collectors.toList());
    }

    public String formatResults(List<Map.Entry<String, Integer>> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("Keyword\tFrequency\n");
        for (Map.Entry<String, Integer> e : entries) {
            sb.append(e.getKey()).append("\t").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    public void saveOutput(Path outputFile, String content) throws IOException {
        Files.writeString(outputFile, content);
    }
}
