package pt.tecnico.bicloin.hub;

import pt.ulisboa.tecnico.sdis.zk.*;
import pt.tecnico.rec.RecFrontend;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import static io.grpc.Status.INVALID_ARGUMENT;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class HubServer {

	public static void main(String[] args) throws Exception {

		if (args.length < 7) {
			System.err.println("Argument(s) missing!");
			return;
		}
		
		String zooHost = args[0];
		String zooPort = args[1];
		String hubHost = args[2];
		String hubPort = args[3];
		String hubPath = "/grpc/bicloin/hub/" + args[4];
		String fileUsers = args[5];
		String fileStations = args[6];
		boolean initRec = false;
		if(args.length == 8 && args[7].equals("initRec")) initRec = true;

		HashMap<String, User> codeToUser = loadUsers(fileUsers);
		HashMap<String, Station> codeToStation = loadStations(fileStations);
		if (codeToUser == null || codeToStation == null ) return;

		ZKNaming zkNaming = null;
		RecFrontend recFrontend = null;
		try {
			zkNaming = new ZKNaming(zooHost, zooPort);
			zkNaming.rebind(hubPath, hubHost, hubPort);
			recFrontend = new RecFrontend(zooHost, zooPort);
			BindableService impl = new HubServiceImpl(zooHost, zooPort, recFrontend, codeToUser, codeToStation, initRec);
			Server server = ServerBuilder.forPort(Integer.valueOf(hubPort)).addService(impl).build();
			server.start();
			System.out.println("Server started");
			server.awaitTermination();
		} catch (ZKNamingException e) {
			System.err.println(e.getMessage());
		} finally  {
			if (zkNaming != null) zkNaming.unbind(hubPath, hubHost, hubPort);
		}

	}

	private static HashMap<String, User> loadUsers(String fileUsers) {
		try {
			String absolutePath = new File("").getAbsolutePath();
			Scanner csvScannerUsers = new Scanner(new File(absolutePath, fileUsers));
			HashMap<String, User> codeToUser = new HashMap<>();

			while (csvScannerUsers.hasNextLine()) {
        		String row = csvScannerUsers.nextLine();

				//System.out.printf("[user] %s\n", row);

				String[] rowTokens = row.split(",");
				String code = rowTokens[0];
				String name = rowTokens[1];
				String phone = rowTokens[2];

				//verifications
				if (code.length() < 3 || code.length() > 10) {
					System.err.println("User identifier must be between 3 and 10 characters!");
					return null;
				}
				if (name.length() < 3 || name.length() > 30) {
					System.err.println("User name must be between 3 and 30 characters!");
					return null;
				}
				
				codeToUser.put(code, new User(code, name, phone));
			}
			csvScannerUsers.close();
			return codeToUser;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private static HashMap<String, Station> loadStations(String fileStations) {
		try {
			String absolutePath = new File("").getAbsolutePath();
			Scanner csvScannerStations = new Scanner(new File(absolutePath, fileStations));
			HashMap<String, Station> codeToStation = new HashMap<>();

			while (csvScannerStations.hasNextLine()) {
        		String row = csvScannerStations.nextLine();

				//System.out.printf("[station] %s\n", row);

				String[] rowTokens = row.split(",");
				String name = rowTokens[0];
				String code = rowTokens[1];
				Double latitude = Double.valueOf(rowTokens[2]);
				Double longitude = Double.valueOf(rowTokens[3]);
				Integer dockCapacity = Integer.valueOf(rowTokens[4]);
				Integer initNumBikes = Integer.valueOf(rowTokens[5]);
				Integer prize = Integer.valueOf(rowTokens[6]);

				//verifications
				if (code.length() != 4) {
					System.err.println("Station identifier must be 4 characters long!");
					return null;
				}
				if (dockCapacity < 0) {
					System.err.println("Number of docks must be non-negative!");
					return null;
				}
				if (initNumBikes < 0) {
					System.err.println("Number of bikes must be non-negative!");
					return null;
				}
				if (prize < 0) {
					System.err.println("Prize must be non-negative!");
					return null;
				}

				codeToStation.put(code, new Station(name, code, latitude, longitude, dockCapacity, initNumBikes, prize));
			}
			csvScannerStations.close();
			return codeToStation;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

}
