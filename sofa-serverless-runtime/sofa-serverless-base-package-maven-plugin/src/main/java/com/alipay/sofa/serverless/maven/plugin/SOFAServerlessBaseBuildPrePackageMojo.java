package com.alipay.sofa.serverless.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase process-sources
 */
@Mojo(name = "after-package", defaultPhase = LifecyclePhase.PACKAGE)
public class SOFAServerlessBaseBuildPrePackageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File outputDirectory;

    @Parameter(property = "promoteSofaServerlessAdapterPriority", defaultValue = "true")
    private boolean promoteSofaServerlessAdapterPriority;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @SneakyThrows
    private void promoteSofaServerlessAdapterPriorityInClassPathIdx(Path targetJarFilePath) {
        getLog().info("start to promote sofa serverless adapter priority for jar " + targetJarFilePath.toAbsolutePath().toString());
        String targetJarFileAbsPath = targetJarFilePath.toAbsolutePath().toString();
        File targetJarFile = new File(targetJarFileAbsPath);
        List<String> classpathidx = JarFileUtils.getFileLines(targetJarFilePath.toFile(), "BOOT-INF/classpath.idx");
        if (classpathidx == null) {
            getLog().warn("classpathidx not found.");
            return;
        }

        for (int i = 0; i < classpathidx.size(); i++) {
            String line = classpathidx.get(i);
            if (line.contains("sofa-serverless-adapter")) {
                classpathidx.remove(i);
                classpathidx.add(0, line);
                break;
            }
        }
        JarFileUtils.updateJarFileContent(targetJarFile, "BOOT-INF/classpath.idx", String.join(System.lineSeparator(), classpathidx));
    }

    /**
     * move the priority of sofa serverless adapter priority so that it can be loaded before other dependencies.
     * this would ensure the logic of compatibility fix file.
     */
    private void promoteThePriorityOfSofaServerlessAdapterDependency() throws Throwable {
        if (!promoteSofaServerlessAdapterPriority) {
            getLog().warn("if this is set to false, you should manually ensure the sofa-serverless-adapter is loaded before other dependencies.");
            return;
        }

        Files.walk(outputDirectory.toPath()).
                filter(path -> path.toString().contains(".jar")).
                forEach((this::promoteSofaServerlessAdapterPriorityInClassPathIdx));
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            File f = outputDirectory;
            Preconditions.checkState(f.exists(), "Directory does not exist " + f);
            getLog().info("start sofa-serverless prepare-build for base package.");
            Files.walk(f.toPath())
                    .forEach(elm -> getLog().debug(elm.getFileName().toString()));
            promoteThePriorityOfSofaServerlessAdapterDependency();
        } catch (Throwable t) {
            getLog().error(t);
        }
    }
}
