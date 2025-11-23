package packets;

public class PingPacket extends Packet {
    private static final long serialVersionUID = 1L;
    
    private long clientTimestamp;
    
    public PingPacket(long clientTimestamp) {
        super(PacketType.PING);
        this.clientTimestamp = clientTimestamp;
    }
    
    public long getClientTimestamp() { return clientTimestamp; }
}