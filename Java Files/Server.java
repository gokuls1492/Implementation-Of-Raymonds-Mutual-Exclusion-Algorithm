
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable {
	static final int MSG_SIZE = 1000;
	ArrayList<Node> nodeList = new ArrayList<>();
	volatile Integer parent;
	static int thisNode;
	volatile Semaphore csEnter = new Semaphore(1, true);
	// ReentrantLock csLock = new ReentrantLock();
	volatile boolean locked = false;
	volatile boolean token = false;
	volatile Queue<Integer> queue1 = new ConcurrentLinkedQueue<>();
	volatile LinkedList<Integer> queue = new LinkedList<>();
	volatile boolean tokenInUse = false;
	volatile static long count = 0;

	volatile static Server server = null;

	public static Server getServer(ArrayList<Node> nodes, Integer parentNodeId, int thisNodeId, boolean tokenVal)
			throws InterruptedException {
		if (server == null) {
			server = new Server(nodes, parentNodeId, thisNodeId, tokenVal);
		}
		return server;
	}

	public static Server getServer() {
		return server;
	}

	private Server(ArrayList<Node> nodes, Integer parentNodeId, int thisNodeId, boolean tokenVal)
			throws InterruptedException {
		this.nodeList = nodes;
		if (parentNodeId == -1)
			this.parent = null;
		else
			this.parent = parentNodeId;
		this.thisNode = thisNodeId;
		this.token = tokenVal;
		csEnter.acquire();
		if (parent != null) {

			locked = true;
		}
	}

	@Override
	public void run() {
		try {
//			System.out.println("Token: " + token);
//			System.out.println("Parent: " + parent);
			SctpServerChannel serverChannel = SctpServerChannel.open();
			InetSocketAddress inetSockAddress = new InetSocketAddress(nodeList.get(thisNode).getPortNum());
			serverChannel.bind(inetSockAddress);
			ByteBuffer byteBufferRecv = ByteBuffer.allocate(MSG_SIZE);
			while (true) {
				//Server starts accepting when the port is opened
				SctpChannel sctpChannel = serverChannel.accept();
				count++;
				System.out.println("Message Count:"+count);

				//Message will be received with binded information
				MessageInfo messageInfo = sctpChannel.receive(byteBufferRecv, null, null);
				byteBufferRecv.position(0);
				byteBufferRecv.limit(MSG_SIZE);
				byte[] bytes = new byte[byteBufferRecv.remaining()];
				byteBufferRecv.get(bytes,0,bytes.length);
				Message msg = (Message) convertBytesToObject(bytes);
				byteBufferRecv.clear();
				byteBufferRecv.put(new byte[MSG_SIZE]);
				byteBufferRecv.clear();
				parseMessage(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object convertBytesToObject(byte[] bytes) throws Exception{
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		return objectInputStream.readObject();
	}


	public synchronized boolean readToken() {
		return token;
	}

	public synchronized void writeToken(boolean tokenVal) {
		token = tokenVal;
	}

	private void parseMessage(Message msg) throws UnknownHostException, IOException, InterruptedException {
		String msgType = msg.getMsgType();
		System.out.println("Received " + msg.getMsgType() + " message from node " + msg.getSrcNodeId());
		if (msgType.equals("Request")) {
			if (readToken()) {
				if (!tokenInUse) {
					// System.out.println("InRequest");
					sendGrant(msg.getSrcNodeId());
				} else
					addToQueue(msg.getSrcNodeId());
			} else {
				makeRequest(msg.getSrcNodeId());
			}
		} else if (msgType.equals("Grant")) {
			int next = removeFromQueue();
			// token = true;
			if (next != -1) {
				if (next == thisNode) {
					writeToken(true);
					parent = null;
					tokenInUse = true;
					release();
				} else {
					sendGrant(next);
				}
			} else {
				writeToken(true);
				parent = null;
			}
		}
	}

	private void sendRequest(Integer parentId) throws UnknownHostException, IOException {
//		System.out.println("sendRequest(): " + parentId);
		Message msg = new Message(thisNode, "Request");
		sendMessage(msg, parentId);
	}

	public synchronized void sendGrant(int srcNodeId) throws UnknownHostException, IOException, InterruptedException {
		Message msg = new Message(thisNode, "Grant");
		writeToken(false);
		parent = srcNodeId;
		sendMessage(msg, srcNodeId);
		if (!queueEmpty()) {
			sendRequest(parent);
		}
		System.out.println("System end time:"+System.currentTimeMillis()+"ms");
	}

	private void sendMessage(Message msg, int nodeId) throws UnknownHostException, IOException {
		System.out.println("Sending " + msg.getMsgType() + " to node: " + nodeId);
		Client client = new Client(msg, nodeList.get(nodeId));
		client.sendMsg();
	}

	public synchronized void makeRequest(int nodeId) throws UnknownHostException, IOException {
		boolean temp = queueEmpty();
//		System.out.println("Queue is Empty:" + temp);
		// if(!readToken())
		addToQueue(nodeId); // add to queue and then send request if necessary
		if (temp && !readToken()) {
			sendRequest(parent);
		}
	}

	public synchronized void addToQueue(int node) {
		boolean greedy = false;
		if (greedy) {
			if (node == thisNode)
				queue.addFirst(node);
			else
				queue.add(node);
		} else {
			queue.add(node);
		}
		System.out.println("Adding node " + node + " to queue");
		System.out.println(queue);
	}

	public synchronized int removeFromQueue() {
		if (queue.isEmpty())
			return -1;
		int ret = queue.remove();
		System.out.println("Removing node " + ret + " from queue");
		System.out.println(queue);
		return ret;

	}

	private synchronized boolean queueEmpty() {
		return queue.isEmpty();
	}

	private synchronized int queueSize() {
		return queue.size();
	}

	public void lock() throws InterruptedException {
		System.out.println("Acquired Lock");
//		System.out.println(csEnter.availablePermits());
		csEnter.acquire();
	}

	private void release() {
		System.out.println("Released Lock");
		csEnter.release();
	}
}
