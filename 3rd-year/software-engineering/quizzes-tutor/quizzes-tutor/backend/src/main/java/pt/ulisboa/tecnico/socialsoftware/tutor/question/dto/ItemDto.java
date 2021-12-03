package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

public class ItemDto implements Serializable {
    private Integer id;
    private Integer sequence;
    private List<String> combinations = new ArrayList<String>();
    private String content;
    private Integer groupId;

    public ItemDto() {
    }

    public ItemDto(Item item) {
        setId(item.getId());
        setSequence(item.getSequence());
        setContent(item.getContent());
        setGroupId(item.getGroupId());
        setCombinations(item.getCombinations());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id){
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public List<String> getCombinations() {
        return combinations;
    }

    public void setCombinations(List<String> combinations) {
        this.combinations = combinations;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getGroupId(){ return this.groupId; }

    public void setGroupId(Integer group) { this.groupId = group; }

    @Override
    public String toString() {
        return "ItemDto{" +
                "id=" + id +
                ", combinations=" + combinations +
                ", groupId='" + groupId +
                ", content='" + content + '\'' +
                '}';
    }
}