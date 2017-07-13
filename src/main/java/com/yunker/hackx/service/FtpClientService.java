package com.yunker.hackx.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by 曹磊(Hackx) on 13/7/2017.
 * Email: caolei@mobike.com
 */

@Service
public class FtpClientService {

    public static Logger logger = LoggerFactory.getLogger(FtpClientService.class);


    @Value("${ftp.host.address}")
    private String host;

    @Value("${ftp.host.port}")
    private Integer port;

    @Value("${ftp.login.name}")
    private String user;

    @Value("${ftp.login.password}")
    private String password;

    private FTPClient ftpClient;
    private boolean isConnected;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void initFtpClient() {
        setConnected(false);
        ftpClient = new FTPClient();
        ftpClient.setCharset(Charset.forName("UTF-8"));
        ftpClient.setControlEncoding("UTF-8");
        ftpClient.setBufferSize(10240);
        System.out.println("ftpClient == null:" + (ftpClient == null));
//        try {
//            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//        } catch (Exception e) {
//            logger.error("initFtpClient error", e);
//        }
    }

    public void connect() {
        try {
            ftpClient.connect(host, port);
        } catch (IOException e) {
            logger.error("connect error,", e);
        }
        int replyCode = ftpClient.getReplyCode();
        logger.info("ReplyCode:" + replyCode);
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            disconnect();
            logger.error("Can't connect to server '" + host + "'");
        }
        try {
            if (!ftpClient.login(user, password)) {
                isConnected = false;
                disconnect();
                logger.error("Can't login to server '" + host + "'");
            } else {
                isConnected = true;
            }
        } catch (Exception e) {
            logger.error("ftpClient.login error", e);
        }
    }

    public void disconnect() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                isConnected = false;
            } catch (Exception e) {
                logger.error("disconnect error", e);
            }
        }
    }

    public void upload(String ftpFileName, File localFile) {
        if (!localFile.exists()) {
            logger.error("Can't upload '" + localFile.getAbsolutePath() + "'. This file doesn't exist.");
            return;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(localFile);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (!ftpClient.storeFile(ftpFileName, fis)) {
                logger.error("Can't upload file:" + ftpFileName + ",Check FTP permissions and path.");
            } else {
                logger.info("upload file:" + ftpFileName + " successfule.");
            }
        } catch (FileNotFoundException fnfe) {
            logger.error("upload error, FileNotFoundException", fnfe);
        } catch (IOException ioe) {
            logger.error("upload error, IOException", ioe);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (Exception e) {
                    logger.error("upload fis.close error", e);
                }
            }
        }
    }

    public boolean setWorkingDirectory(String dir) {
        if (!isConnected) {
            return false;
        }
        try {
            return ftpClient.changeWorkingDirectory(dir);
        } catch (IOException e) {
            logger.error("setWorkingDirectory exception", e);
        }
        return false;
    }

    public boolean makeDirectory(String pathname) {
        if (!isConnected) {
            return false;
        }
        try {
            return ftpClient.makeDirectory(pathname);
        } catch (IOException e) {
            logger.error("makeDirectory exception", e);
        }
        return false;
    }

}
