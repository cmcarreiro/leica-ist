package pt.tecnico.bicloin.app;

import pt.tecnico.bicloin.app.Coordinates;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import io.grpc.StatusRuntimeException;

public class AppMain {

	private static String userName;
	private static String phoneNumber;
	private static Coordinates userCoordinates;
	private static HubFrontend hubFrontend;
	private static HashMap<String, Coordinates> tagToCoordinates = new HashMap<>();
	
	public static void main(String[] args) {

		if (args.length < 6) {
			System.err.println("Argument(s) missing!");
			return;
		}

		String zooHost = args[0];
		String zooPort = args[1];
		userName = args[2];
		phoneNumber = args[3];
		userCoordinates = new Coordinates(Double.valueOf(args[4]), Double.valueOf(args[5]));

		hubFrontend = new HubFrontend(zooHost, zooPort, "");
		
		Scanner inputReader = new Scanner(System.in);
		
		while(true) {
			System.out.printf("> ");
			String inputArgsString = null;

    		try {
				inputArgsString = inputReader.nextLine();
			} catch (NoSuchElementException e) {
				break;
			}
			
			String[] inputArgs = inputArgsString.split(" ");
			String command = inputArgs[0];
			String[] commandArgs = Arrays.copyOfRange(inputArgs, 1, inputArgs.length);
			
			if(command.equals("balance")) 			balance(commandArgs);
			else if (command.equals("top-up")) 		topUp(commandArgs);
			else if (command.equals("tag")) 		tag(commandArgs);
			else if (command.equals("move")) 		move(commandArgs);
			else if (command.equals("at")) 			at(commandArgs);
			else if (command.equals("scan")) 		scan(commandArgs);
			else if (command.equals("info")) 		info(commandArgs);
			else if (command.equals("bike-up")) 	bikeUp(commandArgs);
			else if (command.equals("bike-down")) 	bikeDown(commandArgs);
			else if (command.equals("zzz")) 		sleep(commandArgs);
			else if (command.equals("#")) 			comment(commandArgs);
			else if (command.equals("ping")) 		ping(commandArgs);
			else if (command.equals("sys-status")) 	sysStatus(commandArgs);
			else if (command.equals("help")) 		help(commandArgs);
			else if (command.equals("exit")) 		{exit(commandArgs); return;}
		}
		inputReader.close();
		hubFrontend.shutdown();
	}

	private static void balance(String[] args) {
		try {
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode(userName).build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		System.out.printf("%s %d BIC\n", userName, balanceResponse.getBalance());
		
		} 
		catch (StatusRuntimeException e) //should never happen
		{
			System.out.printf("ERRO"); 
		}
	}

	private static void topUp(String[] args) {
		try {
			TopUpRequest topUpRequest = null;
			Integer amount = Integer.valueOf(args[0]);
			topUpRequest = topUpRequest.newBuilder().setCode(userName).setAmount(amount).setPhone(phoneNumber).build();
			TopUpResponse topUpResponse = hubFrontend.topUp(topUpRequest);
			System.out.printf("%s %d BIC\n", userName, topUpResponse.getBalance());
		} 

		catch (StatusRuntimeException e) {
			System.out.printf("ERRO: %s\n", e.getStatus().getDescription());
		}
	}

	private static void tag(String[] args) {
		Double newLocationLatitude = Double.valueOf(args[0]);
		Double newLocationLongitude = Double.valueOf(args[1]);
		String newTag = args[2];
		tagToCoordinates.put(newTag, new Coordinates(newLocationLatitude, newLocationLongitude));
		System.out.printf("OK\n");
	}

	private static void move(String[] args) {
		if(isDouble(args[0]) && isDouble(args[1])) {
			Double latitude = Double.valueOf(args[0]);
			Double longitude = Double.valueOf(args[1]);
			userCoordinates = new Coordinates(latitude, longitude);
		} else {
			String tag = args[0];
			userCoordinates = tagToCoordinates.get(tag);
		}
		System.out.printf("%s em https://www.google.com/maps/place/%.4f,%.4f\n", userName, userCoordinates.getLatitude(), userCoordinates.getLongitude());
	}

	private static void at(String[] args) {
		System.out.printf("%s em https://www.google.com/maps/place/%.4f,%.4f\n", userName, userCoordinates.getLatitude(), userCoordinates.getLongitude());
	}

	private static void scan(String[] args) {
		try {
			LocateStationRequest locateStationRequestScan = null;
			InfoStationRequest infoStationRequestScan = null;
			Integer nScan = Integer.valueOf(args[0]);
			locateStationRequestScan = locateStationRequestScan.newBuilder().setLatitude(userCoordinates.getLatitude()).setLongitude(userCoordinates.getLongitude()).setK(nScan).build();
			LocateStationResponse locateStationResponseScan = hubFrontend.locateStation(locateStationRequestScan);
			List<String> stationsList = locateStationResponseScan.getStationsInfoList();
			for(String stationId: stationsList) {
				infoStationRequestScan = infoStationRequestScan.newBuilder().setCode(stationId).build();
				InfoStationResponse infoStationResponseScan = hubFrontend.infoStation(infoStationRequestScan);
				Coordinates stationCoordinates = new Coordinates(infoStationResponseScan.getLatitude(), infoStationResponseScan.getLongitude());
				Integer dockCapacityScan = infoStationResponseScan.getDockCapacity();
				Integer prizeScan = infoStationResponseScan.getPrize();
				Integer availableBikesScan = infoStationResponseScan.getAvailableBikes();
				double distanceScan = haversine(userCoordinates, stationCoordinates);
				System.out.printf("%s, lat %.4f, %.4f long, %d docas, %d BIC prémio, %d bicicletas, a %d metros\n", stationId, stationCoordinates.getLatitude(), stationCoordinates.getLongitude(), dockCapacityScan, prizeScan, availableBikesScan, (int) Math.round(distanceScan));
			}
		}

		catch (StatusRuntimeException e) {
			System.out.printf("ERRO: %s\n", e.getStatus().getDescription());
		}
	}

	private static void info(String[] args) {
		try {
		InfoStationRequest infoStationRequest = null;
		String stationId = args[0];
		infoStationRequest = infoStationRequest.newBuilder().setCode(stationId).build();
		InfoStationResponse infoStationResponse = hubFrontend.infoStation(infoStationRequest);
		String stationName = infoStationResponse.getName();
		Coordinates stationCoordinates = new Coordinates(infoStationResponse.getLatitude(), infoStationResponse.getLongitude());
		Integer dockCapacity = infoStationResponse.getDockCapacity();
		Integer prize = infoStationResponse.getPrize();
		Integer availableBikes = infoStationResponse.getAvailableBikes();
		Integer bikesUp = infoStationResponse.getUp();
		Integer bikesDown = infoStationResponse.getDown();
		double distance = haversine(userCoordinates, stationCoordinates);
		System.out.printf("%s, lat %.4f, %.4f long, %d docas, %d BIC prémio, %d bicicletas, %d levantamentos, %d devoluções, https://www.google.com/maps/place/%.4f,%.4f\n", stationName, stationCoordinates.getLatitude(), stationCoordinates.getLongitude(), dockCapacity, prize, availableBikes, bikesUp, bikesDown, stationCoordinates.getLatitude(), stationCoordinates.getLongitude());
		}

		catch (StatusRuntimeException e) {
			System.out.printf("ERRO: %s\n", e.getStatus().getDescription());
		}
	}

	private static void bikeUp(String[] args) {
		try {
		BikeRequest bikeUpRequest = null;
		String stationId = args[0];
		bikeUpRequest = bikeUpRequest.newBuilder().setUserCode(userName).setUserLatitude(userCoordinates.getLatitude()).setUserLongitude(userCoordinates.getLongitude()).setStationCode(stationId).build();
		BikeResponse bikeUpResponse = hubFrontend.bikeUp(bikeUpRequest);
		System.out.printf("OK\n");
		}

		catch (StatusRuntimeException e) {
			System.out.printf("ERRO: %s\n", e.getStatus().getDescription());
		}
	}

	private static void bikeDown(String[] args) {
		try {
			BikeRequest bikeDownRequest = null;
			String stationId = args[0];
			bikeDownRequest = bikeDownRequest.newBuilder().setUserCode(userName).setUserLatitude(userCoordinates.getLatitude()).setUserLongitude(userCoordinates.getLongitude()).setStationCode(stationId).build();
			BikeResponse bikeDownResponse = hubFrontend.bikeDown(bikeDownRequest);
			System.out.printf("OK\n");
		}
		
		catch (StatusRuntimeException e) {
			System.out.printf("ERRO: %s\n", e.getStatus().getDescription());
		}
	}

	private static void sleep(String[] args) {
		try {
			Integer milliseconds = Integer.valueOf(args[0]);
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			System.out.printf("ERRO"); //shouldn't happen
		}
	}

	private static void comment(String[] args) {
	}

	private static void ping(String[] args) {
		PingHubRequest pingHubRequest = PingHubRequest.newBuilder().build();
		PingHubResponse pingHubResponse = hubFrontend.ping(pingHubRequest);
		System.out.printf("%s\n", pingHubResponse.getStatus());
	}

	private static void sysStatus(String[] args) {
		SysStatusRequest sysStatusRequest = SysStatusRequest.newBuilder().build();
		SysStatusResponse sysStatusResponse = hubFrontend.sysStatus(sysStatusRequest);
		for(ServerStatus serverInfo: sysStatusResponse.getServersStatusList()) {
			System.out.printf("path:%s status:%s\n", serverInfo.getPath(), serverInfo.getStatus());
		}
	}

	private static void help(String[] args) {
		System.out.println("balance\t\t\tget user balance");
		System.out.println("top-up\t\t\tadds money to user account");
		System.out.println("tag\t\t\tcreates a location tag");
		System.out.println("move\t\t\tmoves user to specified location");
		System.out.println("at\t\t\tprints user current location");
		System.out.println("scan\t\t\tlocates n closest stations");
		System.out.println("info\t\t\tgives information about the station");
		System.out.println("bike-up\t\t\tlets user pick up a bike");
		System.out.println("bike-down\t\tlets user return a bike");
		System.out.println("ping\t\t\treturns server status");
		System.out.println("sys-status\t\treturns system status");
		System.out.println("zzz\t\t\tsleeps for a certain amount of milliseconds");
		System.out.println("#\t\t\tcomments");
		System.out.println("help\t\t\tlists all possible commands and what they do");
		System.out.println("exit\t\t\tterminates app gracefully");
	}

	private static void exit(String[] args) {
		if (hubFrontend != null) hubFrontend.shutdown();
	}

	private static Double haversine(Coordinates user, Coordinates station) {
		Double distanceLatitude  = Math.toRadians((station.getLatitude() - user.getLatitude()));
		Double distanceLongitude = Math.toRadians((station.getLongitude() - user.getLongitude()));

		Double a = Math.pow(Math.sin(distanceLatitude/2),2) + Math.cos(Math.toRadians(userCoordinates.getLatitude())) * Math.cos(Math.toRadians(userCoordinates.getLatitude())) * Math.pow(Math.sin(distanceLongitude/2),2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return 6371000 * c;
	}

	private static Boolean isDouble(String str) {
		if(str == null) return false;
		try {
			Double.valueOf(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
}
