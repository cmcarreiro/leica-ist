package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item;

import javax.persistence.*;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

@Entity
public class ItemAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private ItemCombinationAnswer itemCombinationAnswer;

    @ManyToOne(optional = false)
    private Item item;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> combinations = new HashSet<>();

    public ItemAnswer() {
    }

    public ItemAnswer(Item item, ItemCombinationAnswer itemCombinationAnswer, Set<String> combinations) {
        setItem(item);
        setItemCombinationAnswer(itemCombinationAnswer);
        setCombinations(combinations);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ItemCombinationAnswer getItemCombinationAnswer() {
        return itemCombinationAnswer;
    }

    public void setItemCombinationAnswer(ItemCombinationAnswer itemCombinationAnswer) {
        this.itemCombinationAnswer = itemCombinationAnswer;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Set<String> getCombinations() {
        return combinations;
    }

    public void setCombinations(Set<String> combinations) {
        this.combinations = combinations;
    }

    public void remove() {
        this.item.getItemAnswers().remove(this);
        this.item = null;
    }

    public boolean isCorrect() {

        return combinations.equals(new HashSet<>(item.getCombinations()));

    }
}