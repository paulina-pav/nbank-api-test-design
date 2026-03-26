package api.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GetAccountTransactionsRequest extends BaseModel{
    private Long id;

}
