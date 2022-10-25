package com.epam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SuffixingApp {
    private static final Logger logger = Logger.getLogger(SuffixingApp.class.getName());

    public static void main(String[] args) throws IOException {
        String configFile = args[0];
        Properties property = new Properties();
        renameFiles(configFile, property);
    }
    public static void renameFiles(String configFile, Properties property) throws IOException {
        String filesSource = getFilesSource(configFile, property);
        if (filesSource == null) {
            return;
        }
        String[] files = filesSource.split(":");
        for (String originalFilePath : files) {
            File originalFile = new File(originalFilePath);
            if (!originalFile.exists()) {
                logger.log(Level.SEVERE, "No such file: {0}", originalFilePath);
            } else {
                String suffix = property.getProperty("suffix");
                if (suffix == null || suffix.isEmpty()) {
                    logger.log(Level.SEVERE, "No suffix is configured");
                    return;
                } else {
                    File renamedFiles = new File(originalFilePath.substring(0, originalFilePath.lastIndexOf(".")) + suffix + originalFilePath.substring(originalFilePath.lastIndexOf(".")));
                    copyOrMoveFile(originalFilePath, originalFile, renamedFiles, property);
                }
            }
        }
    }
    private static void copyOrMoveFile(String originalFilePath, File originalFile, File renamedFiles, Properties property) throws IOException {
        String mode = property.getProperty("mode");
        String originFilePath = originalFile.getPath().replace("\\", "/");
        String renamedFilePath = renamedFiles.getPath().replace("\\", "/");
        if (mode.equalsIgnoreCase("copy")) {
            Files.copy(Path.of(originalFilePath), Path.of(String.valueOf(renamedFiles)));
            logger.log(Level.INFO, originFilePath + " -> " + renamedFilePath);
        } else if (mode.equalsIgnoreCase("move") && originalFile.renameTo(renamedFiles)) {
            logger.log(Level.INFO, originFilePath + " => " + renamedFilePath);
        } else {
            logger.log(Level.SEVERE, "Mode is not recognized: {0}", mode);
        }
    }
    private static String getFilesSource(String configFile, Properties property) throws IOException {
        try (FileInputStream propertiesInput = new FileInputStream(configFile)) {
            property.load(propertiesInput);
        }
        String filesSource = property.getProperty("files");
        if (filesSource == null || filesSource.isEmpty()) {
            logger.log(Level.WARNING, "No files are configured to be copied/moved");
        }
        return filesSource;
    }
}