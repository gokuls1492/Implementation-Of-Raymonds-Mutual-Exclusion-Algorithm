import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class Application {
	public static int numNodes = 0;
	public static int meanDelay = 0;
	public static int csTime = 0;
	public static volatile int numReq = 0;
	public static int thisNode;
	private static long startTime = 0;
	private static long endTime = 0;
	private static long systemStartTime = 0;
	private static long systemEndTime = 0;

	public static void readConfig(String path, int nodeNum) throws IOException, InterruptedException {
		thisNode = nodeNum;

		File f = new File(path);
		Scanner sc = new Scanner(f);
		boolean nodeConf = false;
		while (sc.hasNextLine() && !nodeConf) {
			String s = sc.nextLine();
			if (s.startsWith("#"))
				continue;
			String[] conf = s.split("\\s+");
			if (!nodeConf) {
				numNodes = Integer.parseInt(conf[0]);
				meanDelay = Integer.parseInt(conf[1]);
				csTime = Integer.parseInt(conf[2]);
				numReq = Integer.parseInt(conf[3]);
				nodeConf = true;
			}
		}
		sc.close();
		csApp();
	}

	public static void csApp() throws InterruptedException, UnknownHostException, IOException {
		Thread.sleep(2000);
		// Server srv = Server.getServer();
		int count = 0;
		File f = new File("/home/012/g/gx/gxs161530/AOS/mutex/respTime.txt");
		systemStartTime = System.currentTimeMillis();
		System.out.println("System start time:"+systemStartTime+"ms");
		while (numReq != 0) {
			startTime = System.currentTimeMillis();
			MutexImpl.csEnter();
			criticalSection();
			MutexImpl.csLeave();
			endTime = System.currentTimeMillis();
			
			FileWriter fw = new FileWriter(f.getAbsoluteFile(), true);
			fw.write("Node " + thisNode + "- Req Number " + (count++) + ":" + (endTime - startTime) + "ms\n");
			fw.flush();
			fw.close();
			numReq--;
			Random r = new Random();
			int interval = (int) ((Math.log(1 - r.nextDouble()) / (-meanDelay))) * 1000;
			Thread.sleep(interval);
		}
		systemEndTime = System.currentTimeMillis();		
		System.out.println("System end time:"+systemEndTime+"ms");
	}

	private static void criticalSection() throws InterruptedException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		Date startTime = new Date();
		System.out.println("CS started at " + dateFormat.format(startTime));
		File f = new File("/home/012/g/gx/gxs161530/AOS/mutex/output.txt");
		FileWriter fw = new FileWriter(f.getAbsoluteFile(), true);
		fw.write("Node " + thisNode + " entered CS @ " + dateFormat.format(startTime) + "\n");
		Random r = new Random();
		int  cs = (int) ((Math.log(1 - r.nextDouble()) / (-csTime))) * 1000;
		Thread.sleep(cs);
		Date endTime = new Date();
		System.out.println("CS Ended at " + dateFormat.format(endTime));
		fw.write("Node " + thisNode + " ended CS @ " + dateFormat.format(endTime) + "\n");
		fw.flush();
		fw.close();
	}
}
