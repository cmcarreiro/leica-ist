package pt.tecnico.bicloin.hub;
import pt.tecnico.bicloin.hub.grpc.Hub.PingHubRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingHubResponse;
import io.grpc.StatusRuntimeException;

public class HubPing {
	
	public static void main(String[] args) throws Exception {	

        if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			return;
		}

		String zooHost = args[0];
		String zooPort = args[1];

		HubFrontend hubFrontend = new HubFrontend(zooHost, zooPort, "");

        PingHubRequest pingRequest = PingHubRequest.newBuilder().build();
        PingHubResponse pingResponse = hubFrontend.ping(pingRequest);
        String status = pingResponse.getStatus();
        System.out.println(status);

		hubFrontend.shutdown();
	}
	
}