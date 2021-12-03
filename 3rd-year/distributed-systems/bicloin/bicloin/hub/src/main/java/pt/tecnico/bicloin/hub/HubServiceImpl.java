package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.ulisboa.tecnico.sdis.zk.*;
import pt.tecnico.rec.RecFrontend;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.stub.StreamObserver;
import static io.grpc.Status.*;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {

	private String zooHost;
	private String zooPort;
	private RecFrontend recFrontend;
	private HashMap<String, User> codeToUser;
	private HashMap<String, Station> codeToStation;

	public HubServiceImpl(String zooHost, String zooPort, RecFrontend recFrontend, HashMap<String, User> codeToUser, HashMap<String, Station> codeToStation, Boolean initRec) {
		super();
		this.zooHost = zooHost;
		this.zooPort = zooPort;
		this.recFrontend = recFrontend;
		this.codeToUser = codeToUser;
		this.codeToStation = codeToStation;
		
		if(initRec) {
			try {
				for(User user : codeToUser.values()) {
					WriteRequest request = WriteRequest.newBuilder().setName(user.getCode() + "_balance").setVal(0).build();
					recFrontend.write(request);
					request = WriteRequest.newBuilder().setName(user.getCode() + "_hasBike").setVal(0).build();
					recFrontend.write(request);
				}
				
				for(Station station : codeToStation.values()) {
					WriteRequest request = WriteRequest.newBuilder().setName(station.getCode() + "_numberBikes").setVal(station.getInitNumBikes()).build();
					recFrontend.write(request);
					request = WriteRequest.newBuilder().setName(station.getCode() + "_up").setVal(0).build();
					recFrontend.write(request);
					request = WriteRequest.newBuilder().setName(station.getCode() + "_down").setVal(0).build();
					recFrontend.write(request);
				}
			} catch (StatusRuntimeException e) {
				System.out.printf("ERRO: %s\n", e.getStatus().getDescription());
			}
		}
	}

	@Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
		String code = request.getCode();

		// verifications
		if (code.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("User name cannot be empty!").asRuntimeException());
			return;
		}

		// read balance
		ReadRequest recRequest = ReadRequest.newBuilder().setName(code + "_balance").build();
		ReadResponse recResponse = recFrontend.read(recRequest);
		Integer balance = recResponse.getVal();

		BalanceResponse response = BalanceResponse.newBuilder().setBalance(balance).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
		String code = request.getCode();
		Integer amountEuros = request.getAmount();
		String phone = request.getPhone();

		//verifications
		if (code.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("User name cannot be empty!").asRuntimeException());
			return;
		}

		if (amountEuros < 1 || amountEuros > 20) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid amount of money!").asRuntimeException());
			return;
		}

		User user = codeToUser.get(code);

		if (!(user.getPhone().equals(phone)) ) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Phone numbers do not match!").asRuntimeException());
			return;
		}
		
		// create amount of bicloin
		Integer amountBic = 10 * amountEuros;

		Integer finalBalance = null;
		synchronized(recFrontend) {
			// read balance
			ReadRequest recReadRequest = ReadRequest.newBuilder().setName(code + "_balance").build();
			ReadResponse recReadResponse = recFrontend.read(recReadRequest);
			Integer initialBalance = recReadResponse.getVal();

			//change balance
			finalBalance = initialBalance + amountBic;

			// write new balance
			WriteRequest recWriteRequest = WriteRequest.newBuilder().setName(code + "_balance").setVal(finalBalance).build();
			recFrontend.write(recWriteRequest);
		}

		TopUpResponse response = TopUpResponse.newBuilder().setBalance(finalBalance).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void bikeUp(BikeRequest request, StreamObserver<BikeResponse> responseObserver) {
		String userCode = request.getUserCode();
		double userLatitude = request.getUserLatitude();
		double userLongitude = request.getUserLongitude();
		String stationCode = request.getStationCode();

		// verifications
		if (userCode.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("User name cannot be empty!").asRuntimeException());
			return;
		}


		User user = codeToUser.get(userCode); //obtain user from hash map
		if (user == null ) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("User does not exist!").asRuntimeException());
			return;
		}

		Station station = codeToStation.get(stationCode); //obtain station from hash map
		if (station == null ) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Station does not exist!").asRuntimeException());
			return;
		}

		if(haversine(userLatitude, userLongitude, station.getLatitude(), station.getLongitude()) > 200) {
			responseObserver.onError(PERMISSION_DENIED.withDescription("Station too far away!").asRuntimeException());
			return;
		}

		synchronized(recFrontend) {
			// read user balance
			ReadRequest recRequest = ReadRequest.newBuilder().setName(userCode + "_balance").build();
			ReadResponse recResponse = recFrontend.read(recRequest);
			Integer balance = recResponse.getVal();
			if ( balance < 10 ) {
				responseObserver.onError(PERMISSION_DENIED.withDescription("User does not have enough money!").asRuntimeException());
				return;
			}

			// read station number of bikes
			ReadRequest recReadRequest = ReadRequest.newBuilder().setName(stationCode + "_numberBikes").build();
			ReadResponse recReadResponse = recFrontend.read(recReadRequest);
			Integer initialBikeNumber = recReadResponse.getVal();
			if(initialBikeNumber < 1) {
				responseObserver.onError(PERMISSION_DENIED.withDescription("Not enough bikes on the station!").asRuntimeException());
				return;
			}

			// read user has bike
			recReadRequest = ReadRequest.newBuilder().setName(userCode + "_hasBike").build();
			recReadResponse = recFrontend.read(recReadRequest);
			Integer hasBike = recReadResponse.getVal();
			if(hasBike == 1) {
				responseObserver.onError(PERMISSION_DENIED.withDescription("User already has a bike!").asRuntimeException());
				return;
			}

			Integer finalBikeNumber = initialBikeNumber - 1;

			// write station new number of bikes
			WriteRequest recWriteRequest = WriteRequest.newBuilder().setName(stationCode + "_numberBikes").setVal(finalBikeNumber).build();
			recFrontend.write(recWriteRequest);

			// read station up
			recReadRequest = ReadRequest.newBuilder().setName(stationCode + "_up").build();
			recReadResponse = recFrontend.read(recReadRequest);
			Integer initialUpNumber = recReadResponse.getVal();
			Integer finalUpNumber = initialUpNumber + 1;

			// write station up
			recWriteRequest = WriteRequest.newBuilder().setName(stationCode + "_up").setVal(finalUpNumber).build();
			recFrontend.write(recWriteRequest);

			Integer initialBalance = balance;
			Integer finalBalance = initialBalance - 10;

			// write user new balance
			recWriteRequest = WriteRequest.newBuilder().setName(userCode + "_balance").setVal(finalBalance).build();
			recFrontend.write(recWriteRequest);

			// write user new has bike
			recWriteRequest = WriteRequest.newBuilder().setName(userCode + "_hasBike").setVal(1).build();
			recFrontend.write(recWriteRequest);
		}

		BikeResponse response = BikeResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void bikeDown(BikeRequest request, StreamObserver<BikeResponse> responseObserver) {
		String userCode = request.getUserCode();
		double userLatitude = request.getUserLatitude();
		double userLongitude = request.getUserLongitude();
		String stationCode = request.getStationCode();

		//verifications
		if (userCode.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("User name cannot be empty!").asRuntimeException());
			return;
		}

		Station station = codeToStation.get(stationCode);
		if (station == null ) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Station does not exist!").asRuntimeException());
			return;
		}

		if(haversine(userLatitude, userLongitude, station.getLatitude(), station.getLongitude()) > 200 ) {
			responseObserver.onError(PERMISSION_DENIED.withDescription("Station too far away!").asRuntimeException());
			return;
		}

		synchronized(recFrontend) {
			// read station number of bikes
			ReadRequest recReadRequest = ReadRequest.newBuilder().setName(stationCode + "_numberBikes").build();
			ReadResponse recReadResponse = recFrontend.read(recReadRequest);
			Integer availableBikes = recReadResponse.getVal();
			Integer dockCapacity = station.getDockCapacity();

			Integer availableDocks = dockCapacity - availableBikes;

			if (availableDocks == 0 ) {
				responseObserver.onError(PERMISSION_DENIED.withDescription("No docks available!").asRuntimeException());
				return;
			}

			// read user has bike
			recReadRequest = ReadRequest.newBuilder().setName(userCode + "_hasBike").build();
			recReadResponse = recFrontend.read(recReadRequest);
			Integer hasBike = recReadResponse.getVal();
			if(hasBike == 0) {
				responseObserver.onError(PERMISSION_DENIED.withDescription("User does not have a bike!").asRuntimeException());
				return;
			}

			Integer initialBikeNumber = availableBikes;
			Integer finalBikeNumber = initialBikeNumber + 1;

			// write station new number of bikes
			WriteRequest recWriteRequest = WriteRequest.newBuilder().setName(stationCode + "_numberBikes").setVal(finalBikeNumber).build();
			recFrontend.write(recWriteRequest);

			// write station down
			recReadRequest = ReadRequest.newBuilder().setName(stationCode + "_down").build();
			recReadResponse = recFrontend.read(recReadRequest);
			Integer initialDownNumber = recReadResponse.getVal();

			Integer finalDownNumber = initialDownNumber + 1;

			// write station new down
			recWriteRequest = WriteRequest.newBuilder().setName(stationCode + "_down").setVal(finalDownNumber).build();
			recFrontend.write(recWriteRequest);

			// read user balance
			recReadRequest = ReadRequest.newBuilder().setName(userCode + "_balance").build();
			recReadResponse = recFrontend.read(recReadRequest);
			Integer initialBalance = recReadResponse.getVal();

			Integer finalBalance = initialBalance + station.getPrize();

			// write user new balance
			recWriteRequest = WriteRequest.newBuilder().setName(userCode + "_balance").setVal(finalBalance).build();
			recFrontend.write(recWriteRequest);

			// write user new has bike
			recWriteRequest = WriteRequest.newBuilder().setName(userCode + "_hasBike").setVal(0).build();
			recFrontend.write(recWriteRequest);
		

		}

		BikeResponse response = BikeResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
		String code = request.getCode();

		//verification
		Station station = codeToStation.get(code);
		if (station == null ) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Station does not exist!").asRuntimeException());
			return;
		}

		Integer bikeNumber = null;
		Integer upNumber = null;
		Integer downNumber = null;

		synchronized(recFrontend) {
			// read number of bikes
			ReadRequest recReadRequest = ReadRequest.newBuilder().setName(code + "_numberBikes").build();
			ReadResponse recReadResponse = recFrontend.read(recReadRequest);
			bikeNumber = recReadResponse.getVal();

			// read up
			recReadRequest = ReadRequest.newBuilder().setName(code + "_up").build();
			recReadResponse = recFrontend.read(recReadRequest);
			upNumber = recReadResponse.getVal();

			// read down
			recReadRequest = ReadRequest.newBuilder().setName(code + "_down").build();
			recReadResponse = recFrontend.read(recReadRequest);
			downNumber = recReadResponse.getVal();
		}

		InfoStationResponse response = InfoStationResponse.newBuilder()
										.setName(station.getName())
										.setLatitude(station.getLatitude())
										.setLongitude(station.getLongitude())
										.setDockCapacity(station.getDockCapacity())
										.setPrize(station.getPrize())
										.setAvailableBikes(bikeNumber)
										.setUp(upNumber)
										.setDown(downNumber)
										.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
		Double userLatitude = request.getLatitude();
		Double userLongitude = request.getLongitude();
		Integer k = request.getK();

		//verification
		if ( k == null || k <= 0 ) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("k needs to be bigger than 0!").asRuntimeException());
			return;
		}

		TreeMap<Double, String> distanceToStation = new TreeMap<>();
		for (Station station : codeToStation.values()) {
			distanceToStation.put(haversine(userLatitude, userLongitude, station.getLatitude(), station.getLongitude()), station.getCode());
		}
		ArrayList<String> stationsOrderedByDistance = new ArrayList<>(distanceToStation.values());
		ArrayList<String> kStationsOrderedByDistance = new ArrayList<>(stationsOrderedByDistance.subList(0, k));
		LocateStationResponse response = LocateStationResponse.newBuilder().addAllStationsInfo(kStationsOrderedByDistance).build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void ping(PingHubRequest request, StreamObserver<PingHubResponse> responseObserver) {
		PingHubResponse response = PingHubResponse.newBuilder().setStatus("Hub is running.").build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {

		SysStatusResponse.Builder responseBuilder = SysStatusResponse.newBuilder();
		
		RecFrontend recFrontend = new RecFrontend(zooHost, zooPort);
		RecsStatusRequest recsStatusRequest = RecsStatusRequest.newBuilder().build();
		RecsStatusResponse recsStatusResponse = recFrontend.recsStatus(recsStatusRequest);
		List<RecStatus> recsStatus = recsStatusResponse.getRecsStatusList();
		for(RecStatus recStatus : recsStatus) {
			String path = recStatus.getPath();
			String status = recStatus.getStatus();
			responseBuilder.addServersStatus(ServerStatus.newBuilder().setPath(path).setStatus(status).build());
		}

		try {
			ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
			
			Collection<ZKRecord> hubRecords = zkNaming.listRecords("/grpc/bicloin/hub");
			Iterator<ZKRecord> hubRecordsIterator = hubRecords.iterator();
			ZKRecord hubRecord = null; 
			while(hubRecordsIterator.hasNext()) {
				hubRecord = hubRecordsIterator.next();
				HubFrontend hubFrontend = new HubFrontend(zooHost, zooPort, hubRecord.getPath());
				PingHubRequest pingHubRequest = PingHubRequest.newBuilder().build();
				PingHubResponse pingHubResponse = hubFrontend.ping(pingHubRequest);
				String status = pingHubResponse.getStatus();
				if(status.equals("Hub is running.")) {
					responseBuilder.addServersStatus(ServerStatus.newBuilder().setPath(hubRecord.getPath()).setStatus("up").build());
				} else {
					responseBuilder.addServersStatus(ServerStatus.newBuilder().setPath(hubRecord.getPath()).setStatus("down").build());
				}
			hubFrontend.shutdown();
			}
		} catch (ZKNamingException e) {
			System.err.println(e.getMessage());
		}
		
		SysStatusResponse response = responseBuilder.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	private Double haversine(Double userLatitude, Double userLongitude, Double stationLatitude, Double stationLongitude) {
		Double distanceLatitude  = Math.toRadians((stationLatitude - userLatitude));
		Double distanceLongitude = Math.toRadians((stationLongitude - userLongitude));

		Double a = Math.pow(Math.sin(distanceLatitude/2),2) + Math.cos(Math.toRadians(userLatitude)) * Math.cos(Math.toRadians(userLatitude)) * Math.pow(Math.sin(distanceLongitude/2),2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return 6371000 * c;
	}
	
}