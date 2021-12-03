package pt.tecnico.rec;

//import java.io.IOException;
//import java.util.Properties;

import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.RecFrontend;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

public class WriteIT {

    private static RecFrontend recFrontend;

    @BeforeAll
	public static void oneTimeSetUp(){
		String zooHost = System.getProperty("zooHost");
		String zooPort = System.getProperty("zooPort");
		recFrontend = new RecFrontend(zooHost, zooPort);
	}

    @Test
	public void WriteOKTest() {
		WriteRequest request = WriteRequest.newBuilder().setName("blahlah").setVal(10).build();
		WriteResponse response = recFrontend.write(request);
		ReadRequest readRequest = ReadRequest.newBuilder().setName("blahlah").build();
		ReadResponse readResponse = recFrontend.read(readRequest);
		assertEquals(10, readResponse.getVal());
	}

}