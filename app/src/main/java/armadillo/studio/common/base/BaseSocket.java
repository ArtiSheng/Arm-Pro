/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.base;

import java.net.Socket;

public class BaseSocket extends Socket {
    private final int s_port;

    public BaseSocket(int s_port) {
        this.s_port = s_port;
    }

    public int getS_port() {
        return s_port;
    }
}
