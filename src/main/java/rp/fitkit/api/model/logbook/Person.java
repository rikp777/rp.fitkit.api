package rp.fitkit.api.model.logbook;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rp.fitkit.api.model.root.Visibility;

@Data
@Table("persons")
public class Person {

    @Id
    @Column("person_id")
    private String id;

    @Column("user_id")
    private String userId;

    @Column("full_name")
    private String fullName;

    @Column("short_bio")
    private String shortBio;

    /**
     * Determines who can see this person entity.
     * Maps to the 'visibility' column in the database.
     */
    private Visibility visibility;
}