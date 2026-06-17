package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class ForbiddenResponse extends BaseModel {
    /*
    {
    "timestamp": "2026-06-17T03:38:25.032+00:00",
    "status": 403,
    "error": "Forbidden",
    "path": "/api/v1/admin/users/20"
}
     */
    private String timestamp;
    private Integer status;
    private String error;
    private String path;
}
