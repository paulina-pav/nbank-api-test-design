package api.models;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MakeDepositRequest extends BaseModel {

    private Double balance;

    private Long id;
}
