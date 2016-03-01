package middleware;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Date;
import java.sql.*;
import java.security.SecureRandom;
import java.math.BigInteger;


public class Authn {

/*
users table schema:
VARCHAR username
VARCHAR password

sessions table schema:
DATETIME timestamp
VARCHAR username
VARCHAR sessionID

logs table schema:
DATETIME timestamp
VARCHAR username
VARCHAR eventDescription
*/

//Connect to SQL database
    private static Connection makeConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection("jdbc:mysql://ec2-54-210-249-133.compute-1.amazonaws.com:3306/users?" +
                "user=remote&password=remote_pass");
        return connect;
//        return Connection connect = DriverManager.getConnection("jdbc:mysql://URLHERE" +
//                "user=USERNAMEHERE&password=PASSWORDHERE");
    }

    //Only for use by the LoginAndOut class
//If username and passwords match, log the user in
//and return a sessionID
    public static String login(String usernameToVerify, String passwordToVerify) throws Exception {
        Connection connection = makeConnection();
        PreparedStatement preparedStatementUsers = connection.prepareStatement(
                "select password from users where username = ?"
        );
        preparedStatementUsers.setString(1,usernameToVerify);
        ResultSet resultSet = preparedStatementUsers.executeQuery();
        if(!resultSet.first()){
            return "Error";
        }
        String passwordInDB;
        passwordInDB = resultSet.getString("password");
//        try {
//            passwordInDB = resultSet.getString("password");
//        }catch(SQLException e){
//            return "Error";
//        }

        //Check if username corresponds to a password in the user database
        if (passwordToVerify.equals(passwordInDB)) {
            //generate unique random string (cannot be elsewhere in token attribute)
            //(Found this on stack overflow)
            SecureRandom random = new SecureRandom();
            String sessionID = new BigInteger(130,random).toString(32);

            //check if string is already in table //TO DO

            //update sessions table to add new session
            Date today = new java.util.Date();
            Timestamp timestamp = new Timestamp(today.getTime());
            //update token attribute for user to be the random string
            PreparedStatement preparedStatementSessions = connection.prepareStatement(
                    "insert into sessions (timestamp,username,sessionID) values (?,?,?);"
            );
            preparedStatementSessions.setTimestamp(1,timestamp);
            preparedStatementSessions.setString(2,usernameToVerify);
            preparedStatementSessions.setString(3,sessionID);
            preparedStatementSessions.execute();

            //update logs
            updateLogs(connection,usernameToVerify,new String("Login Success"));

            //close DB
            connection.close();

            //return the random string (this is the session token that
            //must be passed with each subsequent request)
            return sessionID;

        }
        else {
            updateLogs(connection,usernameToVerify,"Login Fail");
            //close DB
            connection.close();
            return new String("Error");
        }
    }

    public static Boolean logout(String token) throws Exception {
        //Check if token exists in user database
        Connection connection = makeConnection();
        PreparedStatement preparedStatementSessionsUname = connection.prepareStatement(
                "select username from sessions where sessionID = ?"
        );
        preparedStatementSessionsUname.setString(1,token);
        ResultSet resultSet = preparedStatementSessionsUname.executeQuery();


        String username;
        if (resultSet.first()) { //user is logged in because this token exists in DB
//            resultSet.first();
            username = resultSet.getString("username");
//            String passwordInDB = resultSet.first().getArray("username");
            //Delete corresponding rows from sessions database
            PreparedStatement preparedStatementSessionsID = connection.prepareStatement(
                    "delete from sessions where sessionID = ?"
            );
            preparedStatementSessionsID.setString(1,token);
            int numSessions = preparedStatementSessionsID.executeUpdate();
            //update logs
            updateLogs(connection,username,new String("Logout Success"));
            connection.close();
            return true;
        }
        else{ //user is not logged in
            //update logs
            updateLogs(connection,"XXX-XXX", "Logout Invalid");
            connection.close();
            return false;
        }
    }

    private static Boolean updateLogs(Connection connection, String username, String eventDescription) throws Exception {
        //Add a record to the logs with username, actionType (e.g., login, logout), and time
        //If successful return true, else return false
        Date today = new java.util.Date();
        Timestamp timestamp = new Timestamp(today.getTime());
        //update logs
        PreparedStatement preparedStatementLogs = connection.prepareStatement(
                "insert into logs (timestamp,username,eventDescription) values (?,?,?);"
        );
        preparedStatementLogs.setTimestamp(1,timestamp);
        preparedStatementLogs.setString(2,username);
        preparedStatementLogs.setString(3,eventDescription);
        preparedStatementLogs.execute();
        return true;
    }

    public static Boolean IsLoggedIn(String token) throws Exception {
        //Check if token exists in user database
        //If so return true
        //Else return false

        //Check if token exists in user database
        Connection connection = makeConnection();
        PreparedStatement preparedStatementSessionsUname = connection.prepareStatement(
                "select username from sessions where sessionID = ?"
        );
        preparedStatementSessionsUname.setString(1,token);
        ResultSet resultSet = preparedStatementSessionsUname.executeQuery();

        String username;
        if (resultSet.first()) { //user is logged in because this token exists in DB
//            resultSet.first();
            username = resultSet.getString("username");
//            username = resultSet.first().getArray("username");
            //update logs
            updateLogs(connection,username,"Transaction Request Success");
            connection.close();
            return true;
        }
        else {
            updateLogs(connection,"","Transaction Request Failed");
            return false;
        }
    }
}
