package com.yunker.hackx.service;

import com.yunker.hackx.common.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.*;
import java.util.List;

/**
 * Created by 曹磊(Hackx) on 13/7/2017.
 * Email: caolei@mobike.com
 */
@Service
public class DirectoryWatchService implements CommandLineRunner {

    public static Logger logger = LoggerFactory.getLogger(DirectoryWatchService.class);

    @Resource
    FtpClientService ftpClientService;

    private static WatchService watchService;
    private String listenerRootPath;

    @Value("${watching.directory}")
    private String watchingDriectory;


    @Override
    public void run(String... args) throws Exception {
        initWatchService();
        startWatch();
    }

    public static void initWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (Exception e) {
            logger.error("Exception occur when watch", e);
        }
    }

    public static void addListener(String path) {
        try {
            Path p = Paths.get(path);
            p.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            logger.info("addListener successful, path is " + path);
        } catch (Exception e) {
            logger.error("addListener exception, path is " + path, e);
        }
    }

    public void startWatch() {
        listenerRootPath = watchingDriectory + DateUtil.getCurrentDayStr();
        addListener(listenerRootPath);
        ftpClientService.initFtpClient();
        ftpClientService.connect();
        logger.info("ftpClientService.connect result:" + ftpClientService.isConnected());
        try {
            while (true) {
                WatchKey watchKey = watchService.take();
                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                for (WatchEvent<?> event : watchEvents) {
                    String directoryPath = (watchKey.watchable()).toString();
                    logger.info("PATH:" + directoryPath + ",FILE:" + event.context() + ",KIND:" + event.kind());
                    if ("ENTRY_CREATE".equals(event.kind().name())) {
                        String targetPath = "/" + directoryPath.split("\\\\")[2];
                        logger.info("targetPath:" + targetPath);

                        logger.info("makeDirectory:" + ftpClientService.getFtpClient().makeDirectory(targetPath));
                        ftpClientService.setWorkingDirectory(targetPath);
                        logger.info("workDirectory:" + ftpClientService.getFtpClient().printWorkingDirectory());

                        File file = new File(directoryPath + "\\" + event.context());
                        logger.info("localFilePath:" + file.getAbsolutePath());

                        ftpClientService.upload(targetPath + "/" + event.context(), file);
                        logger.info("upload file " + file.getAbsolutePath() + " to " + targetPath + "/" + event.context() + " finished");
                    } else {
                        logger.warn("Other event kind: " + event.kind().name());
                    }
                }
                watchKey.reset();
            }
        } catch (Exception e) {
            logger.warn("startWatch exception", e);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void discoverDirectory() {
        String dir = watchingDriectory + DateUtil.getCurrentDayStr();
        File file = new File(dir);
        if (file.exists()) {
            if (file.isDirectory()) {
                if (file.list().length == 0) {
                    addListener(dir);
                    logger.info("add new watched path:" + dir);
                }
            }
        }
    }
}
