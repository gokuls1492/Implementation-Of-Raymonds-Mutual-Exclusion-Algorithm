
public class Node {
	private String hostName;
	private int portNum;
	
	public Node(String hostName, int portNum) {
		super();
		this.hostName = hostName;
		this.portNum = portNum;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getPortNum() {
		return portNum;
	}
	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}
}
