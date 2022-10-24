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
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";
    private static final Properties property = new Properties();

    public static void main(String[] args) throws IOException {
        renameFiles();
    }

    public static void renameFiles() throws IOException {
        String filesSource = getFilesSource();
        if (filesSource == null) return;
        String[] files = filesSource.split(":");
        for (String originalFilePath : files) {
            File originalFile = new File(originalFilePath);
            if (!originalFile.exists()) {
                logger.log(Level.SEVERE, "No such file: {0}", originalFilePath);
            } else {
                String suffix = property.getProperty("suffix");
                if (suffix.equalsIgnoreCase("")) {
                    logger.log(Level.SEVERE, "No suffix is configured");
                    break;
                } else {
                    File renamedFiles = new File(originalFilePath.substring(0, originalFilePath.lastIndexOf(".")) + suffix + originalFilePath.substring(originalFilePath.lastIndexOf(".")));
                    copyOrMoveFile(originalFilePath, originalFile, renamedFiles);
                }
            }
        }
    }

    private static void copyOrMoveFile(String originalFilePath, File originalFile, File renamedFiles) throws IOException {
        String mode = property.getProperty("mode");
        if (mode.equalsIgnoreCase("copy")) {
            Files.copy(Path.of(originalFilePath), Path.of(String.valueOf(renamedFiles)));
            logger.log(Level.INFO, originalFile.getAbsolutePath() + "->" + renamedFiles.getAbsolutePath());
        } else if (mode.equalsIgnoreCase("move") && originalFile.renameTo(renamedFiles)) {
            logger.log(Level.INFO, originalFile.getAbsolutePath() + "=>" + renamedFiles.getAbsolutePath());
        } else {
            logger.log(Level.SEVERE, "Mode is not recognized: {0}", mode);
        }
    }

    private static String getFilesSource() throws IOException {
        FileInputStream propertiesInput = new FileInputStream(CONFIG_FILE_PATH);
        property.load(propertiesInput);
        String filesSource = property.getProperty("files");
        if (filesSource.equalsIgnoreCase("")) {
            logger.log(Level.WARNING, "No files are configured to be copied/moved");
            return null;
        }
        return filesSource;
    }
}
