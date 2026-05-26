package common.storage;

import api.models.CreatedUser;
import api.requests.steps.AdminSteps;

import java.util.ArrayList;
import java.util.List;

public class UserForApiStorage {
    private final List<CreatedUser> users = new ArrayList<>();

    public void createAndAddUserToStorage() {
        CreatedUser user = AdminSteps.createUser();
        users.add(user);
    }

    public CreatedUser getUser(int number) {
        return users.get(number);
    }

    public List<CreatedUser> getAllUsers() {
        return users;
    }


    public void clearContextAndDeleteUsers(){
        for(CreatedUser user: users){
            AdminSteps.deletesUser(user);
        }
        users.clear();
    }


    public CreatedUser getFirstUser() {
        return getUser(0);
    }

    public CreatedUser getSecondUser() {
        return getUser(1);
    }

}
