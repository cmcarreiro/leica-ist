package pt.tecnico.rec;

import pt.ulisboa.tecnico.sdis.zk.*;

public class ShutdownThread extends Thread {

    private String recHost;
    private String recPort;
    private String recPath;
    private ZKNaming zkNaming;

    public ShutdownThread(String recHost, String recPort, String recPath, ZKNaming zkNaming) {
        this.recHost = recHost;
        this.recPort = recPort;
        this.recPath = recPath;
        this.zkNaming = zkNaming;
    }

    @Override
	public void run() {
        try {
            if(zkNaming != null)
                zkNaming.unbind(recPath,recHost,recPort);
        } catch (ZKNamingException e) {
			System.err.println(e.getMessage());
        }
    }

}