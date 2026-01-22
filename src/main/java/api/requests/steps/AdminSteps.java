package api.requests.steps;

import api.generators.RandomModelGenerator;
import io.restassured.common.mapper.TypeRef;
import api.models.DeleteByUserIdResponse;
import api.models.NewUserRequest;
import api.models.NewUserResponse;
import api.models.User;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {

    public static NewUserRequest createUser(){
        NewUserRequest userRequest =
                RandomModelGenerator.generate(NewUserRequest.class);

        new ValidatedCrudRequester<NewUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return userRequest;
    }

   public static String deletesUser(NewUserRequest userRequest){


       List<User> users = new CrudRequester(
               RequestSpecs.adminSpec(),
               Endpoint.GET_ALL_USER,
               ResponseSpecs.requestReturnsOK()
       ).get()
               .extract()
               .as(new TypeRef<List<User>>() {
               });

       User user = users.stream()
               .filter(u ->u.getUsername().equals(userRequest.getUsername()))
               .findAny().get();

       String successMessage =new ValidatedCrudRequester<DeleteByUserIdResponse>(
               RequestSpecs.adminSpec(),
               Endpoint.DELETE_USER_BY_ID,
               ResponseSpecs.requestReturnsOK()
       ).delete(user.getId());

        return successMessage;

    }


}
