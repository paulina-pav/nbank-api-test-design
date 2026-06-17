package api.generators;

public class ServiceMessageHelpMethods {

    public static String createDeleteUserSuccessfulMessage(Integer id){
        return "User with ID" +  " " + id + " deleted successfully.";
    }

    public static String userNotFoundMessage(Integer id){
        return "Error: User with ID" +  " " + id + " not found.";
    }
}
