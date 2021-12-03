package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;
import pt.tecnico.rec.grpc.RecServiceGrpc;
import pt.tecnico.rec.grpc.RecServiceGrpc.RecServiceStub;
import pt.ulisboa.tecnico.sdis.zk.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Collection;
import java.util.ArrayList;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import io.grpc.stub.StreamObserver;

public class RecFrontend {

	private ZKNaming zkNaming;
	private Collection<ZKRecord> records;
	//private Integer numReads = 0;
	//private Integer numWrites = 0;

	public RecFrontend(String zooHost, String zooPort) {
		this.zkNaming = new ZKNaming(zooHost, zooPort);
		this.records = null;
		try {
			this.records = zkNaming.listRecords("/grpc/bicloin/rec");
		} catch (ZKNamingException e) {
			System.err.println(e.getMessage());
		}
	}

	public RecsStatusResponse recsStatus(RecsStatusRequest request) {

		// request to be sent to all recs
		PingRecRequest pingRecRequest = PingRecRequest.newBuilder().build();

		// init collector to get all the responses
		ResponseCollector collector = new ResponseCollector(records.size(), true);
		CopyOnWriteArrayList<PingRecResponse> responsesList = null;
		synchronized(collector) {
			collector.start();

			Iterator<ZKRecord> recordsIterator = records.iterator();
			
			// for all recs
			while(recordsIterator.hasNext()) {
				ZKRecord record = recordsIterator.next();
				String target = record.getURI();
				ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
				RecServiceStub stub = RecServiceGrpc.newStub(channel);

				// send request
				stub.ping(pingRecRequest, new RecObserver<PingRecResponse>(collector));

				channel.shutdown();
			}

			try {
				// wait for collector to receive enough responses
				// or for timeout to expire
				collector.wait(5000);
				responsesList = collector.getResponsesList();
			}
			catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}

		// process responses
		ArrayList<String> upRecsPathList = new ArrayList<>();
		for(PingRecResponse response : responsesList) upRecsPathList.add(response.getPath());
		
		RecsStatusResponse.Builder responseBuilder = RecsStatusResponse.newBuilder();
		
		for(ZKRecord thisRecord : records) {
			String path = thisRecord.getPath();
			if(upRecsPathList.contains(path))
				responseBuilder.addRecsStatus(RecStatus.newBuilder().setPath(path).setStatus("up").build());
			else
				responseBuilder.addRecsStatus(RecStatus.newBuilder().setPath(path).setStatus("down").build());
		}
		RecsStatusResponse response = responseBuilder.build();
		return response;
	}

	public ReadResponse read(ReadRequest readRequest) {

		ReadAuxRequest readAuxRequest = ReadAuxRequest.newBuilder().setName(readRequest.getName()).build();
		CopyOnWriteArrayList<ReadAuxResponse> readAuxResponses = readAux(readAuxRequest);

		// returns most recent val
		Integer maxSeq = -1;
		Integer valMaxSeq = null;
		for(ReadAuxResponse readAuxResponse : readAuxResponses) {
			if(readAuxResponse.getSeq() >= maxSeq) {
				maxSeq = readAuxResponse.getSeq();
				valMaxSeq = readAuxResponse.getVal();
			}
		}

		return ReadResponse.newBuilder().setVal(valMaxSeq).build();
	}

	public WriteResponse write(WriteRequest writeRequest) {
		ReadAuxRequest readAuxRequest = ReadAuxRequest.newBuilder().setName(writeRequest.getName()).build();
		CopyOnWriteArrayList<ReadAuxResponse> readAuxResponses = readAux(readAuxRequest);

		// returns most recent seq
		Integer maxSeq = -1;
		for(ReadAuxResponse readAuxResponse : readAuxResponses) {
			if(readAuxResponse.getSeq() >= maxSeq) {
				maxSeq = readAuxResponse.getSeq();
			}
		}

		// generates new seq
		Integer newSeq = maxSeq + 1;

		WriteAuxRequest writeAuxRequest = WriteAuxRequest.newBuilder().setName(writeRequest.getName()).setVal(writeRequest.getVal()).setSeq(newSeq).build();
		CopyOnWriteArrayList<WriteAuxResponse> writeAuxResponses = writeAux(writeAuxRequest);
		WriteResponse writeResponse = WriteResponse.newBuilder().build();
		return writeResponse;
	}

	private CopyOnWriteArrayList<ReadAuxResponse> readAux(ReadAuxRequest readAuxRequest) {

		// init collector to get all the responses
		ResponseCollector collector = new ResponseCollector(records.size(), false);
		CopyOnWriteArrayList<ReadAuxResponse> responsesList = null;
		synchronized(collector) {
			collector.start();

			Iterator<ZKRecord> recordsIterator = records.iterator();
			
			//start clock
			//Long startTime = System.currentTimeMillis();

			// for all recs
			while(recordsIterator.hasNext()) {
				ZKRecord record = recordsIterator.next();
				String target = record.getURI();
				ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
				RecServiceStub stub = RecServiceGrpc.newStub(channel);

				// send request
				stub.readAux(readAuxRequest, new RecObserver<ReadAuxResponse>(collector));

				channel.shutdown();
			}

			try {
				// wait for collector to receive enough responses
				collector.wait(0);

				//Long stopTime = System.currentTimeMillis();
				//Long timeEllapsedMillis = stopTime - startTime;
				//System.out.printf("READ TIME: %d\n", timeEllapsedMillis);
				
				//numReads++;
				//System.out.printf("READ COUNT: %d\n", numReads);

				responsesList = collector.getResponsesList();
			}
			catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		return responsesList;
	}

	private CopyOnWriteArrayList<WriteAuxResponse> writeAux(WriteAuxRequest writeAuxRequest) {

		// init collector to get all the responses
		ResponseCollector collector = new ResponseCollector(records.size(), false);
		CopyOnWriteArrayList<WriteAuxResponse> responsesList = null;
		synchronized(collector) {
			collector.start();

			Iterator<ZKRecord> recordsIterator = records.iterator();
			
			//start clock
			//Long startTime = System.currentTimeMillis();

			// for all recs
			while(recordsIterator.hasNext()) {
				ZKRecord record = recordsIterator.next();
				String target = record.getURI();
				ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
				RecServiceStub stub = RecServiceGrpc.newStub(channel);

				// send request
				stub.writeAux(writeAuxRequest, new RecObserver<WriteAuxResponse>(collector));

				channel.shutdown();
			}

			try {
				// wait for collector to receive enough responses
				collector.wait(0);
				
				//Long stopTime = System.currentTimeMillis();
				//Long timeEllapsedMillis = stopTime - startTime;
				//System.out.printf("WRITE TIME: %d\n", timeEllapsedMillis);
				
				//numWrites++;
				//System.out.printf("WRITE COUNT: %d\n", numWrites);

				responsesList = collector.getResponsesList();
			}
			catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		return responsesList;
	}

}