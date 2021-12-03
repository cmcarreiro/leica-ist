package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item;

import java.io.Serializable;

public class ItemStatementQuestionDetailsDto implements Serializable {
    private Integer itemId;
    private String content;
    private Integer groupId;

    public ItemStatementQuestionDetailsDto(Item item) {
        itemId = item.getId();
        content = item.getContent();
        groupId = item.getGroupId();
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
