package api.requests.steps;

import common.helpers.StepLogger;
import api.generators.RandomModelGenerator;

import api.models.NewUserRequest;
import api.models.User;
import api.models.CreatedUser;
import api.models.NewUserResponse;
import api.models.DeleteByUserIdResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.common.mapper.TypeRef;

import java.util.ArrayList;
import java.util.List;

public class AdminSteps {


    public static List<User> getAllUsers() {
        List<User> users = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.GET_ALL_USER,
                ResponseSpecs.requestReturnsOK()
        ).get()
                .extract()
                .as(new TypeRef<List<User>>() {
                });

        return users;
    }


    public static CreatedUser createUser() {
        NewUserRequest userRequest =
                RandomModelGenerator.generate(NewUserRequest.class);

        return StepLogger.log("Admin creates user " + userRequest.getUsername(), () -> {

                    NewUserResponse response = new ValidatedCrudRequester<NewUserResponse>(
                            RequestSpecs.adminSpec(),
                            Endpoint.ADMIN_USER,
                            ResponseSpecs.entityWasCreated())
                            .post(userRequest);

                    return new CreatedUser(userRequest, response);
                }
        );
    }

    public static String deletesUser(CreatedUser user) {

            return StepLogger.log("Admin deletes user " + user.getRequest().getUsername(), () -> {

                String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.DELETE_USER_BY_ID,
                        ResponseSpecs.requestReturnsOK()
                ).delete(user.getResponse().getId());

                return successMessage;
            });

    }

    public static List<CreatedUser> createsTwoUsers() {

        CreatedUser newUser1 = AdminSteps.createUser();
        CreatedUser newUser2 = AdminSteps.createUser();

        return StepLogger.log("Admin creates two users " + newUser1.getRequest().getUsername() + " "
                + newUser2.getRequest().getUsername(), () -> {

            List<CreatedUser> users = new ArrayList<>();
            users.add(newUser1);
            users.add(newUser2);

            return users;
        });
    }

    public static void deleteTwoUsersByIds(Integer firstId, Integer secondId) {

        List<User> users = getAllUsers();

        List<Integer> ids = new ArrayList<>();
        ids.add(firstId);
        ids.add(secondId);


        for (Integer id : ids) {
            User user = users.stream()
                    .filter(u -> u.getId().equals(id))
                    .findAny().get();

            StepLogger.log("Admin deletes users by IDs ", () -> {

                String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.DELETE_USER_BY_ID,
                        ResponseSpecs.requestReturnsOK()
                ).delete(user.getId());
            });
        }
    }

    public static void deleteAllUsers() {
        List<User> users = getAllUsers();
        List<Integer> ids = new ArrayList<>();

        for (User user : users) {
            ids.add(user.getId());
        }

        for (Integer id: ids) {
            String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.DELETE_USER_BY_ID,
                    ResponseSpecs.requestReturnsOK()
            ).delete(id);
            System.out.println("successMessage");
        }
    }
}
