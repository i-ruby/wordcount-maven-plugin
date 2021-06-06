package work.iruby.wordcount;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mojo(name = "wordcount", defaultPhase = LifecyclePhase.TEST)
public class WordCountMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}")
    private File basedir;

    @Parameter(property = "outputFile", defaultValue = "wordcount.txt")
    private File outputFile;

    private final static String BLANK_REGEX = "['.,;<>(){}\\[\\]\"]+";
    private final static Pattern BLANK_PATTERN = Pattern.compile(BLANK_REGEX);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("basedir: " + basedir);
        List<Path> pathList = getPathList(basedir.toPath());
        if (pathList == null) return;
        ForkJoinPool pool = new ForkJoinPool();
        Map<String, Integer> map = pool.submit(new WordCount(pathList)).join();
        LinkedHashMap<String, Integer> collect = sortByValToLinkedMap(map);
        getLog().info("outputFile: " + outputFile);
        creatOutputFile(collect, outputFile);
    }

    private void creatOutputFile(LinkedHashMap<String, Integer> collect, File outputFile) throws MojoExecutionException {
        System.out.println();
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.write(collect.toString());
        } catch (IOException e) {
            throw new MojoExecutionException("输出位置有误", e);
        }
    }

    private LinkedHashMap<String, Integer> sortByValToLinkedMap(Map<String, Integer> map) {
        return map.entrySet().stream().sorted((e1, e2) -> {
            if (e1.getValue() > e2.getValue()) {
                return -1;
            } else if (e1.getValue() < e2.getValue()) {
                return 1;
            } else {
                return e1.getKey().compareTo(e2.getKey());
            }
        }).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private List<Path> getPathList(Path basedir) throws MojoFailureException {
        List<Path> pathList = new ArrayList<>();
        try {
            Files.walkFileTree(basedir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Objects.requireNonNull(file);
                    Objects.requireNonNull(attrs);
                    if (file.toString().endsWith(".java")) {
                        pathList.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new MojoFailureException("指定路径不正确", e);
        }
        if (pathList.size() == 0) {
            return null;
        }
        return pathList;
    }

    private Map<String, Integer> reduce(Map<String, Integer> map1, Map<String, Integer> map2) {
        if (map1.size() < map2.size()) {
            for (String s : map1.keySet()) {
                map2.merge(s, map1.get(s), Integer::sum);
            }
            return map2;
        } else {
            for (String s : map2.keySet()) {
                map1.merge(s, map2.get(s), Integer::sum);
            }
            return map1;
        }
    }

    private Map<String, Integer> countWord(Path path) {
        Map<String, Integer> map = new HashMap<>();
        try {
            Files.readAllLines(path)
                    .stream()
                    .flatMap(line -> Arrays.stream(
                            BLANK_PATTERN.matcher(line)
                                    .replaceAll(" ")
                                    .split("\\s+")))
                    .filter(w -> !w.isEmpty())
                    .forEach(w -> map.merge(w, 1, Integer::sum));
        } catch (IOException e) {
            throw new RuntimeException("文件解析出错", e);
        }
        return map;
    }

    class WordCount extends RecursiveTask<Map<String, Integer>> {
        List<Path> pathList;

        public WordCount(List<Path> pathList) {
            this.pathList = pathList;
        }

        @Override
        protected Map<String, Integer> compute() {
            if (pathList.size() == 0) {
                return new HashMap<>();
            } else if (pathList.size() == 1) {
                return countWord(pathList.get(0));
            } else {
                ForkJoinTask<Map<String, Integer>> leftTask = new WordCount(pathList.subList(0, pathList.size() / 2)).fork();
                ForkJoinTask<Map<String, Integer>> rightTask = new WordCount(pathList.subList(pathList.size() / 2, pathList.size())).fork();
                return reduce(leftTask.join(), rightTask.join());
            }
        }
    }

}
