import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Client {
	private Message msg;
	private Node dstNode;

	static final int MSG_SIZE = 1000;

	public Client(Message msg, Node dstNode) {
		super();
		this.msg = msg;
		this.dstNode = dstNode;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

	public Node getDstNode() {
		return dstNode;
	}

	public void setDstNode(Node dstNode) {
		this.dstNode = dstNode;
	}

	public void sendMsg() throws UnknownHostException, IOException {
		ByteBuffer byteBufferSend = ByteBuffer.allocate(MSG_SIZE);
		InetSocketAddress socketAddress = new InetSocketAddress(dstNode.getHostName(), dstNode.getPortNum());
		SctpChannel sctpChannel = SctpChannel.open(socketAddress,0,0);
		MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
		byteBufferSend.put(convertObjectToBytes(msg));
		byteBufferSend.flip();
		sctpChannel.send(byteBufferSend, messageInfo);
		byteBufferSend.clear();
		sctpChannel.close();
	}

	public byte[] convertObjectToBytes(Object obj) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
		out.writeObject(obj);
		return byteArrayOutputStream.toByteArray();
	}

}
