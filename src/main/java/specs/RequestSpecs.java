package specs;

import Requests.UserLoginAuthRequester;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.UserLoginAuth;

import java.util.List;

public class RequestSpecs {
    private RequestSpecs(){}


    //одинаковое во всех запросах -- хидеры
    private static RequestSpecBuilder defaultRequestBuilder(){
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri("http://127.0.0.1:5000");
    }


    //неавто пользователь
    public static RequestSpecification unAuthSpec(){
        return defaultRequestBuilder().build(); //взяли дефолтную сборку без авторизации
    }

    //добавляем админа
    public static RequestSpecification adminAuth(){
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    //логин пользователя -- по факту вызов метода user login
    public static RequestSpecification authAsUser(String username, String password){
        String token = new UserLoginAuthRequester(RequestSpecs.unAuthSpec(), ResponseSpecs.isOk())
                .post(UserLoginAuth.builder()
                        .username(username)
                        .password(password)
                        .build())
                .extract()
                .header("Authorization");

        return defaultRequestBuilder()
                .addHeader("Authorization", token)
                .build();
    }



}
