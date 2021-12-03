package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceBlockingStub;
import pt.ulisboa.tecnico.sdis.zk.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Collection;

public class HubFrontend {

	private ManagedChannel hubChannel;
	private HubServiceBlockingStub hubStub;

	public HubFrontend(String zooHost, String zooPort, String hubPath) {

		if(hubPath.equals("")) {
			try {
				ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
				Collection<ZKRecord> hubRecords = zkNaming.listRecords("/grpc/bicloin/hub");
				ZKRecord hubRecord = hubRecords.iterator().next();
				String hubTarget = hubRecord.getURI();
				hubChannel = ManagedChannelBuilder.forTarget(hubTarget).usePlaintext().build();
				hubStub = HubServiceGrpc.newBlockingStub(hubChannel);
			} catch (ZKNamingException e) {
				System.err.println(e.getMessage());
			}
		} else {
			try {
				ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
				ZKRecord hubRecord = zkNaming.lookup(hubPath);
				String hubTarget = hubRecord.getURI();
				hubChannel = ManagedChannelBuilder.forTarget(hubTarget).usePlaintext().build();
				hubStub = HubServiceGrpc.newBlockingStub(hubChannel);
			} catch (ZKNamingException e) {
				System.err.println(e.getMessage());
			}
		
		}
		
	}

	public BalanceResponse balance(BalanceRequest request) {
		return hubStub.balance(request);
	}

	public TopUpResponse topUp(TopUpRequest request) {
		return hubStub.topUp(request);
	}

	public InfoStationResponse infoStation(InfoStationRequest request) {
		return hubStub.infoStation(request);
	}

	public LocateStationResponse locateStation(LocateStationRequest request) {
		return hubStub.locateStation(request);
	}

	public BikeResponse bikeUp(BikeRequest request) {
		return hubStub.bikeUp(request);
	}

	public BikeResponse bikeDown(BikeRequest request) {
		return hubStub.bikeDown(request);
	}

	public PingHubResponse ping(PingHubRequest request) {
		return hubStub.ping(request);
	}

	public SysStatusResponse sysStatus(SysStatusRequest request) {
		return hubStub.sysStatus(request);
	}

	public void shutdown() {
		hubChannel.shutdownNow();
	}

}