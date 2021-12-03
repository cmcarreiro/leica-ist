package pt.tecnico.rec;

//import java.io.IOException;
//import java.util.Properties;

import pt.tecnico.rec.grpc.Rec.RecsStatusRequest;
import pt.tecnico.rec.grpc.Rec.RecsStatusResponse;
import pt.tecnico.rec.grpc.Rec.RecStatus;
import pt.tecnico.rec.RecFrontend;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import java.util.List;

public class PingIT {

    private static RecFrontend recFrontend;

    @BeforeAll
	public static void oneTimeSetUp() {
		String zooHost = System.getProperty("zooHost");
		String zooPort = System.getProperty("zooPort");
		recFrontend = new RecFrontend(zooHost, zooPort);
	}

    @Test
	public void pingOKTest() {
		RecsStatusRequest recsStatusRequest = RecsStatusRequest.newBuilder().build();
        RecsStatusResponse recsStatusResponse = recFrontend.recsStatus(recsStatusRequest);
        List<RecStatus> recsStatusList = recsStatusResponse.getRecsStatusList();
        assertEquals("/grpc/bicloin/rec/1", recsStatusList.get(0).getPath());
        assertEquals("up", recsStatusList.get(0).getStatus());
        assertEquals("/grpc/bicloin/rec/2", recsStatusList.get(1).getPath());
        assertEquals("up", recsStatusList.get(1).getStatus());
        assertEquals("/grpc/bicloin/rec/3", recsStatusList.get(2).getPath());
        assertEquals("up", recsStatusList.get(2).getStatus());
        assertEquals("/grpc/bicloin/rec/4", recsStatusList.get(3).getPath());
        assertEquals("up", recsStatusList.get(3).getStatus());
        assertEquals("/grpc/bicloin/rec/5", recsStatusList.get(4).getPath());
        assertEquals("up", recsStatusList.get(4).getStatus());
	}

}