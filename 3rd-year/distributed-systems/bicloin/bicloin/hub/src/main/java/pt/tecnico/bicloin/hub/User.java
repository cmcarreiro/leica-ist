package pt.tecnico.bicloin.hub;

public class User {

    private String code;
    private String name;
    private String phone;

    public User(String code, String name, String phone) {
        this.code = code;
        this.name = name;
        this.phone = phone;
    }

    String getCode() {
        return code;
    }

    String getName() {
        return name;
    }

    String getPhone() {
        return phone;
    }

}