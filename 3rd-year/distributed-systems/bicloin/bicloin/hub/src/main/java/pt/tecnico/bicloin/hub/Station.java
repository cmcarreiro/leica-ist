package pt.tecnico.bicloin.hub;

public class Station {

    private String name;
    private String code;
    private Double latitude;
    private Double longitude;
    private Integer dockCapacity;
    private Integer initNumBikes;
    private Integer prize;

    public Station(String name, String code, Double latitude, Double longitude, Integer dockCapacity, Integer initNumBikes, Integer prize) {
        this.name = name;
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dockCapacity = dockCapacity;
        this.initNumBikes = initNumBikes;
        this.prize = prize;
    }

    String getName() {
        return name;
    }

    String getCode() {
        return code;
    }

    Double getLatitude() {
        return latitude;
    }

    Double getLongitude() {
        return longitude;
    }

    Integer getDockCapacity() {
        return dockCapacity;
    }

    Integer getInitNumBikes() {
        return initNumBikes;
    }

    Integer getPrize() {
        return prize;
    }
}