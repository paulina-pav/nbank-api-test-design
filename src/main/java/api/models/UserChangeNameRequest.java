package api.models;


import api.generators.GeneratingRule;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UserChangeNameRequest extends BaseModel{
    @GeneratingRule(regex = "^[A-Z]{3}[ ]{1}[a-z]{4}$")
    private String name;

    @Override
    public String toString() {
        return name;
    }

}
