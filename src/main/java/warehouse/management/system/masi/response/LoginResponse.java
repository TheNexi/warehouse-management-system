package warehouse.management.system.masi.response;

import warehouse.management.system.masi.model.enums.EmployeeRole;

public record LoginResponse(Long employeeId, String username, EmployeeRole role) {
}
