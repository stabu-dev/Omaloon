package mmc;

import org.apache.commons.io.*;
import org.gradle.api.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class JarMindustryTask extends DefaultTask{
    static final Logger logger = LoggerFactory.getLogger(JarMindustryTask.class);

    public static String defaultMindustryPath(){
        String mindustryDataDir = System.getenv("MINDUSTRY_DATA_DIR");
        if(mindustryDataDir != null) return mindustryDataDir;
        return MockOS.getAppDataDirectoryString("Mindustry");
    }


    private static String[] parseOutputs(@Nullable File outputFile) throws IOException{
        if(outputFile == null || !outputFile.exists()) return new String[]{defaultMindustryPath()};
        String[] lines = FileUtils.readFileToString(outputFile, Charset.defaultCharset())
            .replace(System.lineSeparator(), "\n")
            .replaceAll("((#|//)[^\n]*\n|\n?(#|//)[^\n]*$)", "")//removing comments
            .replaceAll("\n\n+", "")//removing blank lines
            .split("\n");
        for(int i = 0; i < lines.length; i++){
            if(lines[i].equals("classic")){
                lines[i] = defaultMindustryPath() + File.pathSeparator + "mods";
            }
        }
        return lines;
    }

    @NotNull
    private static File resultJar(Project project, AbstractArchiveTask jar){
        if(true) return jar.getArchiveFile().get().getAsFile();
        String archiveFileName = jar.getArchiveFileName().get();

        return new File(project.getBuildDir(), "libs/" + archiveFileName);
    }

    @TaskAction
    public void copyResultJar() throws IOException{
        Project project = getProject();
        AbstractArchiveTask jar = (AbstractArchiveTask)project.getTasks().getByName("jar");
        File source = resultJar(project, jar);
        String[] strings = parseOutputs(findOutputFile(project));
        for(String path : strings){
            File destination = new File(project.file(path), source.getName());
            if(destination.exists()){
                //noinspection ResultOfMethodCallIgnored
                destination.delete();
            }
            FileUtils.copyFile(source, destination);
            System.out.println("[I] Jar copied to `" + destination.getAbsolutePath() + "`");
        }
    }

    @Nullable
    private File findOutputFile(@Nullable Project project){
        if(project == null) return null;
        File file = project.file("outputDirectories.txt");
        if(file.exists()) return file;
        file = project.file("modsDirectories.txt");
        if(file.exists()){
            logger.warn("`modsDirectories.txt` is deprecated, use `outputDirectories.txt`");
            return file;
        }
        Project parent = project.getParent();
        if(parent == project) return null;//VERY, VERY strange, but I think can happen.
        return findOutputFile(parent);
    }
}
