package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.HubFrontend;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static io.grpc.Status.*;
import io.grpc.StatusRuntimeException;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HubIT {

    private static HubFrontend hubFrontend;

    @BeforeAll
	public static void oneTimeSetUp(){
		String zooHost = System.getProperty("zooHost");
		String zooPort = System.getProperty("zooPort");
		hubFrontend = new HubFrontend(zooHost, zooPort, "");
	}

    @Test
    @Order(1)
	public void BalanceOkTest() {
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(0, balanceResponse.getBalance());
	}

    @Test
    @Order(2)
    public void BikeUpNotEnoughMoneyTest() {
        BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("ista").build();
        assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeUp(bikeUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(0, balanceResponse.getBalance());
    }
	
    @Test
    @Order(3)
	public void BalanceEmptyUserNameTest() {
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("").build();
		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.balance(balanceRequest);
            }).getStatus().getCode()
        );
	}

    
    @Test
    @Order(4)
	public void TopUpOkTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setCode("alice").setAmount(3).setPhone("+35191102030").build();
		TopUpResponse topUpResponse = hubFrontend.topUp(topUpRequest);
		assertEquals(30, topUpResponse.getBalance());
	}

    @Test
    @Order(5)
	public void TopUpEmptyUserNameTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setCode("").setAmount(10).setPhone("+35191102030").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.topUp(topUpRequest);
            }).getStatus().getCode()
        );
	}

    @Test
    @Order(6)
	public void TopUpInvalidAmountTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setCode("alice").setAmount(100).setPhone("+35191102030").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.topUp(topUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(30, balanceResponse.getBalance());
	}

    @Test
    @Order(7)
	public void TopUpInvalidPhoneNumberTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setCode("alice").setAmount(10).setPhone("+35112345").build();
		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.topUp(topUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(30, balanceResponse.getBalance());
	}

    @Test
    @Order(8)
	public void BikeUpStationTooFarAwayTest() {
		BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("cais").build();
		assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeUp(bikeUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(30, balanceResponse.getBalance());
	}

    @Test
    @Order(9)
    public void BikeUpEmptyUserTest() {
        BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("ista").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeUp(bikeUpRequest);
            }).getStatus().getCode()
        );
    }

    @Test
    @Order(10)
    public void BikeUpStationDoesntExistTest() {
        BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("blah").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeUp(bikeUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(30, balanceResponse.getBalance());
    }
	
	@Test
    @Order(11)
    public void BikeUpStationDoesntHaveBikesTest() {
        BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7097).setUserLongitude(-9.1336).setStationCode("cate").build();
        assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeUp(bikeUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(30, balanceResponse.getBalance());
    }

    @Test
    @Order(12)
	public void BikeUpOkTest() {
		BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("ista").build();
		hubFrontend.bikeUp(bikeUpRequest);
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(20, balanceResponse.getBalance());
	}

    @Test
    @Order(13)
    public void BikeUpUserAlreadyHasBikeTest() {
        BikeRequest bikeUpRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7097).setUserLongitude(-9.1366).setStationCode("ista").build();
        assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeUp(bikeUpRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(20, balanceResponse.getBalance());
    }

    @Test
    @Order(14)
	public void BikeDownStationTooFarAwayTest() {
		BikeRequest bikeDownRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("cais").build();
		assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeDown(bikeDownRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(20, balanceResponse.getBalance());
	}

    @Test
    @Order(15)
    public void BikeDownEmptyUserTest() {
        BikeRequest bikeDownRequest = BikeRequest.newBuilder().setUserCode("").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("ista").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeDown(bikeDownRequest);
            }).getStatus().getCode()
        );
    }

    @Test
    @Order(16)
    public void BikeDownStationDoesntExistTest() {
        BikeRequest bikeDownRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("blah").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeDown(bikeDownRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(20, balanceResponse.getBalance());
    }
	
	@Test
    @Order(17)
    public void BikeDownStationDoesntHaveDocksTest() {
        BikeRequest bikeDownRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7376).setUserLongitude(-9.1545).setStationCode("gulb").build();
        assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeDown(bikeDownRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(20, balanceResponse.getBalance());
    }

    @Test
    @Order(18)
	public void BikeDownOkTest() {
		BikeRequest bikeDownRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7369).setUserLongitude(-9.1366).setStationCode("ista").build();
		hubFrontend.bikeDown(bikeDownRequest);
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(23, balanceResponse.getBalance());
	}
    
    @Test
    @Order(19)
    public void BikeDownUserDoesntHaveBikeTest() {
        BikeRequest bikeDownRequest = BikeRequest.newBuilder().setUserCode("alice").setUserLatitude(38.7097).setUserLongitude(-9.1366).setStationCode("ista").build();
        assertEquals(
            PERMISSION_DENIED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.bikeDown(bikeDownRequest);
            }).getStatus().getCode()
        );
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setCode("alice").build();
		BalanceResponse balanceResponse = hubFrontend.balance(balanceRequest);
		assertEquals(23, balanceResponse.getBalance());
    }

    @Test
    @Order(20)
	public void InfoStationOkTest() {
		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setCode("ista").build();
		InfoStationResponse infoStationResponse = hubFrontend.infoStation(infoStationRequest);
		assertEquals("IST Alameda", infoStationResponse.getName());
		assertEquals(38.7369, infoStationResponse.getLatitude());
		assertEquals(-9.1366, infoStationResponse.getLongitude());
		assertEquals(20, infoStationResponse.getDockCapacity());
		assertEquals(3, infoStationResponse.getPrize());
        assertEquals(19, infoStationResponse.getAvailableBikes());
		assertEquals(1, infoStationResponse.getUp());
		assertEquals(1, infoStationResponse.getDown());
	}

    @Test
    @Order(21)
	public void InfoStationStationDoesNotExistTest() {
		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setCode("blah").build();
		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.infoStation(infoStationRequest);
            }).getStatus().getCode()
        );
	}

    @Test
    @Order(22)
	public void LocateStationOkTest() {
		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setLatitude(38.7372).setLongitude(-9.1400).setK(2).build();
		LocateStationResponse locateStationResponse = hubFrontend.locateStation(locateStationRequest);
		List<String> stationsList = locateStationResponse.getStationsInfoList();
		assertEquals("ista", stationsList.get(0));
		assertEquals("gulb", stationsList.get(1));
	}

    @Test
    @Order(23)
	public void LocateStationInvalidKTest() {
		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setLatitude(38.7372).setLongitude(-9.1400).setK(-1).build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> {
                hubFrontend.locateStation(locateStationRequest);
            }).getStatus().getCode()
        );
	}

    @Test
    @Order(24)
	public void PingOkTest() {
		PingHubRequest pingRequest = PingHubRequest.newBuilder().build();
        PingHubResponse pingResponse = hubFrontend.ping(pingRequest);
        assertEquals("Hub is running.", pingResponse.getStatus());
    }

    @Test
    @Order(25)
	public void SysStatusOkTest() {
		SysStatusRequest sysStatusRequest = SysStatusRequest.newBuilder().build();
        SysStatusResponse sysStatusResponse = hubFrontend.sysStatus(sysStatusRequest);
        List<ServerStatus> serverStatusList = sysStatusResponse.getServersStatusList();
        assertEquals("/grpc/bicloin/rec/1", serverStatusList.get(0).getPath());
        assertEquals("up", serverStatusList.get(0).getStatus());
        assertEquals("/grpc/bicloin/rec/2", serverStatusList.get(1).getPath());
        assertEquals("up", serverStatusList.get(1).getStatus());
        assertEquals("/grpc/bicloin/rec/3", serverStatusList.get(2).getPath());
        assertEquals("up", serverStatusList.get(2).getStatus());
        assertEquals("/grpc/bicloin/rec/4", serverStatusList.get(3).getPath());
        assertEquals("up", serverStatusList.get(3).getStatus());
        assertEquals("/grpc/bicloin/rec/5", serverStatusList.get(4).getPath());
        assertEquals("up", serverStatusList.get(4).getStatus());
        assertEquals("/grpc/bicloin/hub/1", serverStatusList.get(5).getPath());
        assertEquals("up", serverStatusList.get(5).getStatus());
    }

	@AfterAll
	public static void cleanup() {
		hubFrontend.shutdown();
	}
    
}
