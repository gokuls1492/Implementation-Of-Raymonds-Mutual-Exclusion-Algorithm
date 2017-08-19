import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private int srcNodeId;
	private String msgType;

	public Message(int srcNodeId, String msgType) {
		super();
		this.srcNodeId = srcNodeId;
		this.msgType = msgType;
	}

	public int getSrcNodeId() {
		return srcNodeId;
	}

	public void setSrcNodeId(int srcNodeId) {
		this.srcNodeId = srcNodeId;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

}
