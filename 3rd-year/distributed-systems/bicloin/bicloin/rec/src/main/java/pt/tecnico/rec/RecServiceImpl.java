package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;
import pt.tecnico.rec.grpc.RecServiceGrpc;
import io.grpc.stub.StreamObserver;
//import static io.grpc.Status.INVALID_ARGUMENT;
import java.util.concurrent.ConcurrentHashMap;

public class RecServiceImpl extends RecServiceGrpc.RecServiceImplBase {

	private String recPath;
	private ConcurrentHashMap<String, Register> nameToRegister;

	public RecServiceImpl(String recPath) {
		super();
		this.recPath = recPath;
		this.nameToRegister = new ConcurrentHashMap<>();
	}

	@Override
	public void writeAux(WriteAuxRequest request, StreamObserver<WriteAuxResponse> responseObserver) {
		String name = request.getName();
		Integer val = request.getVal();
		Integer seq = request.getSeq();

		//creates new register with name, val, and sequence number
		//and adds to the hashmap, replacing the previous one
		nameToRegister.put(name, new Register(name, val, seq));

		//System.out.printf("[write] name:%s val:%d seq:%d\n", name, val, seq);

		WriteAuxResponse response = WriteAuxResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void readAux(ReadAuxRequest request, StreamObserver<ReadAuxResponse> responseObserver) {
		String name = request.getName();

		//if register doesn't exist we create a new register
		if (!nameToRegister.containsKey(name)) nameToRegister.put(name, new Register(name, -1, 0));
		
		Register reg = nameToRegister.get(name);
		Integer val = reg.getVal();
		Integer seq = reg.getSeq();

		//System.out.printf("[read] name:%s val:%d seq:%d\n", name, val, seq);

		ReadAuxResponse response = ReadAuxResponse.newBuilder().setVal(val).setSeq(seq).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void ping(PingRecRequest request, StreamObserver<PingRecResponse> responseObserver) {
		PingRecResponse response = PingRecResponse.newBuilder().setPath(recPath).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

}