import java.io.IOException;

public class Launcher {
	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
		MutexImpl.readConfig(args[1], Integer.parseInt(args[0]));
		Thread.sleep(1000);
		Application.readConfig(args[1], Integer.parseInt(args[0]));
	}
}
