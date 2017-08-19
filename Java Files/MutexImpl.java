import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class MutexImpl {
	public static int thisNode;
	static Server serverObj = null;

	public static void readConfig(String path, int nodeNum) throws IOException, InterruptedException {
		Integer pointer = 0;
		ArrayList<Node> nodes = new ArrayList<>();
		boolean token = false;
		thisNode = nodeNum;

		File f = new File(path);
		Scanner sc = new Scanner(f);
		boolean nodeConf = false;
		while (sc.hasNextLine()) {
			String s = sc.nextLine();
			if (s.startsWith("#"))
				continue;
			String[] conf = s.split("\\s+");
			if (!nodeConf) {
				nodeConf = true;
			} else {
				nodes.add(new Node(conf[1], Integer.parseInt(conf[2])));
				if (Integer.parseInt(conf[0]) == thisNode) {
					token = false;
					pointer = Integer.parseInt(conf[3]);
					if (pointer == thisNode) {
						pointer = -1;
						token = true;
					}
				}
			}
		}
		sc.close();
		Server serverObj = Server.getServer(nodes, pointer, thisNode, token);
		Thread t = new Thread(serverObj);
		t.start();
	}

	public synchronized static void csEnter() throws InterruptedException, UnknownHostException, IOException {
		Server serverObj = Server.getServer();
		if (!serverObj.readToken()) {
			System.out.println("Waiting for token");
			serverObj.makeRequest(thisNode);
			// serverObj.locked = true;
			// System.out.println("CSEnter");
			serverObj.lock();
			System.out.println("Token:" + serverObj.readToken());
			System.out.println("Received token | Entering CS");
		} else if (serverObj.readToken() && serverObj.tokenInUse) {
			System.out.println("Token in Use");
			serverObj.addToQueue(thisNode);
			serverObj.lock();
			System.out.println("Received token | Entering CS");
		} else if (serverObj.readToken()) {
			serverObj.makeRequest(thisNode);
			serverObj.lock();
		}
		serverObj.tokenInUse = true;
	}

	public synchronized static void csLeave() throws UnknownHostException, IOException, InterruptedException {

		Server serverObj = Server.getServer();
		int next = serverObj.removeFromQueue();
		if (next != -1) {
			serverObj.sendGrant(next);
		}
		serverObj.tokenInUse = false;
		System.out.println("CS Leave");
	}
	
}
