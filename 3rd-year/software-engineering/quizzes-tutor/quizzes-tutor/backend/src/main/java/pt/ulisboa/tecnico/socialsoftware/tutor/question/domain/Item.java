package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto;

import javax.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONTENT_FOR_ITEM;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_GROUP_FOR_ITEM;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_SEQUENCE_FOR_ITEM;

@Entity
@Table(name = "items")
public class Item implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;

    @ElementCollection()
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> combinations = new ArrayList<String>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer groupId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private ItemCombinationQuestion questionDetails;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<ItemAnswer> itemAnswers = new HashSet<>();

    public Item() {
    }

    public Item(ItemDto item) {
        setSequence(item.getSequence());
        setContent(item.getContent());
        setCombinations(item.getCombinations());
        setGroupId(item.getGroupId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitItem(this);
    }

    public Integer getId() {
        return id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        if (sequence == null || sequence < 0)
            throw new TutorException(INVALID_SEQUENCE_FOR_ITEM);

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
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_ITEM);

        this.content = content;
    }

    public Integer getGroupId(){ return groupId; }

    public void setGroupId(Integer n){
        if(n != 1 && n!= 2)
            throw new TutorException(INVALID_GROUP_FOR_ITEM);
        this.groupId = n;
    }

    public ItemCombinationQuestion getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(ItemCombinationQuestion question) {
        this.questionDetails = question;
        question.addItem(this);
    }

    public Set<ItemAnswer> getItemAnswers() {
        return itemAnswers;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", sequence=" + sequence +
                ", combinations=" + combinations +
                ", groupId='" + groupId +
                ", content='" + content + '\'' +
                ", question=" + questionDetails.getId() +
                ", itemAnswers=" + itemAnswers +
                '}';
    }

    public void remove() {
        this.questionDetails = null;
        this.combinations.clear();
        this.itemAnswers.clear();
    }
}