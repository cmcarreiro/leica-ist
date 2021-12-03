package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDetailsDto;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
public class ItemAnswerItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private Integer itemId;

    @ElementCollection
    private Set<String> combinations;

    public ItemAnswerItem() {}

    public ItemAnswerItem(ItemStatementAnswerDetailsDto itemStatementAnswerDto) {
        itemId = itemStatementAnswerDto.getItemId();
        combinations = itemStatementAnswerDto.getCombinations();
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Set<String> getCombinations() {
        return combinations;
    }

    public void setAssignedOrder(Set<String> combinations) {
        this.combinations = combinations;
    }
}