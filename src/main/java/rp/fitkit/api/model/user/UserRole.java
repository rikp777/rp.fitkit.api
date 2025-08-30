package rp.fitkit.api.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("user_role")
public class UserRole {

    @Column("user_id")
    private String userId;

    @Column("role_name")
    private String roleName;
}

