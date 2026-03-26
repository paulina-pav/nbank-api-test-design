package models;

import java.util.List;

public class GetAllUsersResponse {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Object> accounts;

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public List<Object> getAccounts() { return accounts; }


}
