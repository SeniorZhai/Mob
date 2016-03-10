package com.google.android.exoplayer.upstream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastDataSource implements UriDataSource {
    public static final int DEFAULT_MAX_PACKET_SIZE = 2000;
    public static final int TRANSFER_LISTENER_PACKET_INTERVAL = 1000;
    private DataSpec dataSpec;
    private boolean opened;
    private final DatagramPacket packet;
    private byte[] packetBuffer;
    private int packetRemaining;
    private int packetsReceived;
    private MulticastSocket socket;
    private final TransferListener transferListener;

    public static final class MulticastDataSourceException extends IOException {
        public MulticastDataSourceException(String message) {
            super(message);
        }

        public MulticastDataSourceException(IOException cause) {
            super(cause);
        }
    }

    public MulticastDataSource(TransferListener transferListener) {
        this(transferListener, DEFAULT_MAX_PACKET_SIZE);
    }

    public MulticastDataSource(TransferListener transferListener, int maxPacketSize) {
        this.transferListener = transferListener;
        this.packetBuffer = new byte[maxPacketSize];
        this.packet = new DatagramPacket(this.packetBuffer, 0, maxPacketSize);
    }

    public long open(DataSpec dataSpec) throws MulticastDataSourceException {
        this.dataSpec = dataSpec;
        String uri = dataSpec.uri.toString();
        String host = uri.substring(0, uri.indexOf(58));
        try {
            this.socket = new MulticastSocket(Integer.parseInt(uri.substring(uri.indexOf(58) + 1)));
            this.socket.joinGroup(InetAddress.getByName(host));
            this.opened = true;
            this.transferListener.onTransferStart();
            return -1;
        } catch (IOException e) {
            throw new MulticastDataSourceException(e);
        }
    }

    public void close() {
        if (this.opened) {
            this.socket.close();
            this.socket = null;
            this.transferListener.onTransferEnd();
            this.packetRemaining = 0;
            this.packetsReceived = 0;
            this.opened = false;
        }
    }

    public int read(byte[] buffer, int offset, int readLength) throws MulticastDataSourceException {
        if (this.packetRemaining == 0) {
            if (this.packetsReceived == TRANSFER_LISTENER_PACKET_INTERVAL) {
                this.transferListener.onTransferEnd();
                this.transferListener.onTransferStart();
                this.packetsReceived = 0;
            }
            try {
                this.socket.receive(this.packet);
                this.packetRemaining = this.packet.getLength();
                this.transferListener.onBytesTransferred(this.packetRemaining);
                this.packetsReceived++;
            } catch (IOException e) {
                throw new MulticastDataSourceException(e);
            }
        }
        if (this.packetRemaining < readLength) {
            readLength = this.packetRemaining;
        }
        System.arraycopy(this.packetBuffer, this.packet.getLength() - this.packetRemaining, buffer, offset, readLength);
        this.packetRemaining -= readLength;
        return readLength;
    }

    public String getUri() {
        return this.dataSpec == null ? null : this.dataSpec.uri.toString();
    }
}
