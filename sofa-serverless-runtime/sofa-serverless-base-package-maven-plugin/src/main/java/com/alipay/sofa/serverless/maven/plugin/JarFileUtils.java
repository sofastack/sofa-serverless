package com.alipay.sofa.serverless.maven.plugin;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

/**
 * jar file utils
 * @author CodeNoobKing
 */
public class JarFileUtils {

    /**
     * get content as file lines inside a bundled jar file.
     * @param file jarfile
     * @param entryName the target file.
     * @return the content as list of string.
     */
    @SneakyThrows
    public static List<String> getFileLines(File file, String entryName) {
        try (JarInputStream jin = new JarInputStream(new FileInputStream(file))) {

            JarEntry entry = null;
            while ((entry = jin.getNextJarEntry()) != null) {
                if (!entry.getName().equals(entryName)) {
                    continue;
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jin));
                return bufferedReader.lines().collect(Collectors.toList());
            }
        }
        return null;
    }

    /**
     * replace the content of given entry inside jarfile with content
     * @param file target jar file.
     * @param targetEntryName target entry name.
     * @param content content to replace.
     */
    @SneakyThrows
    public static void updateJarFileContent(File file,
                                            String targetEntryName,
                                            String content) {
        File tmpDir = null;
        // create a temp dir
        try {
            tmpDir = Files.createTempDirectory("jarfile").toFile();
            tmpDir.deleteOnExit();

            File curFile = tmpDir;
            String[] splittedPath = targetEntryName.split("/");
            for (int i = 0; i < splittedPath.length; i++) {
                curFile = Paths.get(curFile.getAbsolutePath(), splittedPath[i]).toFile();
                if (i == splittedPath.length - 1) {
                    curFile.createNewFile();
                    break;
                }
                Files.createDirectories(curFile.toPath());
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(curFile));
            outputStreamWriter.write(content);
            outputStreamWriter.flush();
            outputStreamWriter.close();

            String command = String.format("jar uf %s -C %s %s", file.getAbsolutePath(), tmpDir.getAbsolutePath(), targetEntryName);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } finally {
            if (tmpDir != null) {
                tmpDir.delete();
            }
        }
    }
}
