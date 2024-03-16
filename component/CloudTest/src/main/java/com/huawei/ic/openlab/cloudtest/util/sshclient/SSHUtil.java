/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util.sshclient;

import com.huawei.ic.openlab.cloudtest.common.exception.SshErrorException;

import lombok.extern.slf4j.Slf4j;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.output.NoCloseOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SSH 远程连接服务
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
@Slf4j
public class SSHUtil {
    private static final Integer DEFAULT_TIMEOUT = 1000 * 30;
    private static final String DEFAULT_SHELL_CHARSET_NAME = "UTF-8";

    /**
     * 测试ssh连接
     *
     * @param ip ip
     * @param port port
     * @param userName username
     * @param passWord password
     * @return boolean
     * @throws SshErrorException exception
     */
    public static boolean sshConnectTest(String ip, int port, String userName, String passWord) {
        try (SshClient client = SshClient.setUpDefaultClient()) {
            client.start();
            try (ClientSession session = client.connect(userName, ip, port).verify(DEFAULT_TIMEOUT).getSession()) {
                session.addPasswordIdentity(passWord);
                return session.auth().verify(DEFAULT_TIMEOUT).isSuccess();
            } finally {
                client.stop();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    /**
     * sshExecCmd
     *
     * @param ip ip
     * @param port port
     * @param userName username
     * @param passWord password
     * @param commandList command list
     * @return string
     * @throws SshErrorException exception
     */
    public static String sshExecCmd(String ip, int port, String userName, String passWord, List<String> commandList)
            throws SshErrorException {
        try (SshClient client = SshClient.setUpDefaultClient()) {
            client.start();
            try (ClientSession session = client.connect(userName, ip, port).verify(DEFAULT_TIMEOUT).getSession()) {
                return execCmd(passWord, session, commandList, DEFAULT_SHELL_CHARSET_NAME);
            } finally {
                client.stop();
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new SshErrorException("SSH出现IO异常");
        }
    }

    private static String execCmd(String passWord, ClientSession session, List<String> commandList, String charsetName)
            throws IOException, SshErrorException {
        session.addPasswordIdentity(passWord);
        if (session.auth().verify(DEFAULT_TIMEOUT).isFailure()) {
            throw new SshErrorException("SSH 连接超时");
        }
        try {
            String response = "";
            for (String command : commandList) {
                response = exec(session, command, charsetName);
            }
            return response;
        } finally {
            session.close(false);
        }
    }

    private static String exec(ClientSession session, String command, String charsetName)
            throws IOException, SshErrorException {
        try (ChannelExec channelExec = session.createExecChannel(command);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            channelExec.setOut(new NoCloseOutputStream(out));
            channelExec.setErr(new NoCloseOutputStream(out));
            if (!channelExec.open().verify(DEFAULT_TIMEOUT).isOpened()) {
                throw new SshErrorException("SSH 连接异常");
            }
            List<ClientChannelEvent> list = new ArrayList<>();
            list.add(ClientChannelEvent.CLOSED);
            channelExec.waitFor(list, DEFAULT_TIMEOUT);
            channelExec.close();

            String response = out.toString(charsetName);
            String[] lines = response.split(System.lineSeparator());
            for (String line : lines) {
                log.debug(line);
            }
            return response;
        }
    }
}
