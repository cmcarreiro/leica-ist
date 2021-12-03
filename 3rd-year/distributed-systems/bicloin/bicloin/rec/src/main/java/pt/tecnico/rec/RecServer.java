package pt.tecnico.rec;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;

public class RecServer {

	public static void main(String[] args) throws Exception {
		
		String zooHost = args[0];
		String zooPort = args[1];
		String recHost = args[2];
		String recPort = args[3];
		String recPath = "/grpc/bicloin/rec/" + args[4];

		ZKNaming zkNaming = null;

		if (args.length < 5) {
			System.err.println("[rec] Argument(s) missing!");
			return;
		}

		zkNaming = new ZKNaming(zooHost, zooPort);

		//takes care of SIGTERM
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(recHost, recPort, recPath, zkNaming));
		zkNaming.rebind(recPath, recHost, recPort);

		final BindableService impl = new RecServiceImpl(recPath);
		Server server = ServerBuilder.forPort(Integer.valueOf(recPort)).addService(impl).build();
		server.start();
		System.out.println("[rec] Server started");
		server.awaitTermination();
	}
	
}