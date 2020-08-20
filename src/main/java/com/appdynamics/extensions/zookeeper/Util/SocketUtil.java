/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper.Util;

import java.io.IOException;
import java.net.Socket;

public class SocketUtil {

    public static Socket createSocket(String host, int port, int socketTimeout) throws IOException {
        Socket socket = new Socket(host,port);
        socket.setSoTimeout(socketTimeout);  //timeout on the socket
        return socket;
    }
}
