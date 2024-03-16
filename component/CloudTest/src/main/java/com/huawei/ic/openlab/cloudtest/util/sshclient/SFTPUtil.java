/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util.sshclient;

import com.huawei.ic.openlab.cloudtest.entity.CloudLabTestTask;
import com.huawei.ic.openlab.cloudtest.entity.LabTestReq;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

/**
 * FastDfsClient
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
@Slf4j
public class SFTPUtil {
    private static final Integer DEFAULT_TIMEOUT = 1000 * 60;

    /**
     * upload file
     *
     * @param testReq testReq
     * @param targetPath target path
     * @param in input stream
     * @throws JSchException JSchException
     * @throws SftpException SftpException
     */
    public static void uploadFile(LabTestReq testReq,
                                  String targetPath,
                                  InputStream in) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session sshSession = jsch.getSession(testReq.getServerUser(), testReq.getServerIp(), testReq.getServerPort());
        sshSession.setPassword(testReq.getServerPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        sshSession.setConfig(config);
        sshSession.connect(DEFAULT_TIMEOUT);

        Channel channel = sshSession.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = null;
        if (!(channel instanceof ChannelSftp)) {
            return;
        }
        try {
            sftp = (ChannelSftp) channel;
            sftp.put(in, targetPath);
        } finally {
            if (sftp != null) {
                sftp.exit();
            }
        }
    }

    /**
     * upload file
     *
     * @param task testReq
     * @param targetPath target path
     * @param in input stream
     * @throws JSchException JSchException
     * @throws SftpException SftpException
     */
    public static void uploadFile(CloudLabTestTask task,
                                  String targetPath,
                                  InputStream in) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session sshSession = jsch.getSession(task.getServerUser(), task.getServerIp(), task.getServerPort());
        sshSession.setPassword(task.getServerPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        sshSession.setConfig(config);
        sshSession.connect(DEFAULT_TIMEOUT);

        Channel channel = sshSession.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = null;
        if (!(channel instanceof ChannelSftp)) {
            return;
        }
        try {
            sftp = (ChannelSftp) channel;
            sftp.put(in, targetPath);
        } finally {
            if (sftp != null) {
                sftp.exit();
            }
        }
    }
}
