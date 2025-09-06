package rp.fitkit.api.model.root;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Table("daily_logs")
public class DailyLog {

    @Id
    @Column("log_id")
    private Long id;

    @Column("user_id")
    private String userId;

    @Column("log_date")
    private LocalDate logDate;
}

