package middleware;

public class LoginAndOut {
    public static String login(String username, String password) throws Exception{
        return Authn.login(username,password);
    }

    public static Boolean logout(String token) throws Exception{
        return Authn.logout(token);
    }
    
    public static Boolean isLoggedIn(String token) throws Exception{
        return Authn.IsLoggedIn(token);
    }
}
