package warehouse.management.system.masi.request;

import lombok.Getter;
import lombok.Setter;
import warehouse.management.system.masi.model.enums.EmployeeRole;

@Getter
@Setter
public class EmployeeRequest {

    private String firstName;
    private String lastName;
    private String position;
    private EmployeeRole role;
    private String username;
    private String password;
}
