package pt.tecnico.rec;

public class Register {

    private String name;
    private Integer val;
    private Integer seq;

    public Register(String name, Integer val, Integer seq) {
        this.name = name;
        this.val = val;
        this.seq = seq;
    }
    
    Integer getVal() {
        return val;
    }

    Integer getSeq() {
        return seq;
    }

}