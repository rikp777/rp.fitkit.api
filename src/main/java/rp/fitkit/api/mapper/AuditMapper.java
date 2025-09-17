package rp.fitkit.api.mapper;

import org.mapstruct.Mapper;
import rp.fitkit.api.dto.audit.AuditLogDto;
import rp.fitkit.api.model.audit.AuditLog;

@Mapper(componentModel = "spring")
public interface AuditMapper {
    AuditLogDto toDto(AuditLog auditLog);
}
