package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.RecsStatusRequest;
import pt.tecnico.rec.grpc.Rec.RecsStatusResponse;
import pt.tecnico.rec.grpc.Rec.RecStatus;

import io.grpc.StatusRuntimeException;

import java.util.List;

public class RecPing {

	public static void main(String[] args) throws Exception {	

		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			return;
		}

		String zooHost = args[0];
		String zooPort = args[1];

		RecFrontend recFrontend = new RecFrontend(zooHost, zooPort);
		
		RecsStatusRequest recsStatusRequest = RecsStatusRequest.newBuilder().build();
		RecsStatusResponse recsStatusResponse = recFrontend.recsStatus(recsStatusRequest);
		List<RecStatus> recsStatus = recsStatusResponse.getRecsStatusList();
		for(RecStatus recStatus : recsStatus) {
			String path = recStatus.getPath();
			String status = recStatus.getStatus();
			System.out.println(path + ":" +  status);
		}
	}

}