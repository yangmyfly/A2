/**
 * Created by joe on 16/2/29.
 */
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class InventoryApp {

    public String addItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String description, Integer quantity, Float perUnitCost, String token){
        Boolean connectError = false;   // Error flag
        Connection DBConn = null;       // MySQL connection handle
        Boolean executeError = false;   // Error flag
        String errString = null;        // String for displaying errors
        int executeUpdateVal;           // Return value from execute indicating effected rows
        Boolean fieldError = true;      // Error flag
        String msgString = null;        // String for displaying non-error messages
        ResultSet res = null;           // SQL query result set pointer
        String tableSelected = null;    // String used to determine which data table to use
        java.sql.Statement s = null;    // SQL statement pointer
        String SQLstatement = null;     // String for building SQL queries

        String responseInformation = "";  //response information
        try
        {
            responseInformation += "\n" + ">> Establishing Driver...";

            //load JDBC driver class for MySQL
            Class.forName( "com.mysql.jdbc.Driver" );

            responseInformation += "\n" + ">> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306";

            responseInformation += "\n" + ">> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db
            DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");

        } catch (Exception e) {

            responseInformation +=  "\nProblem connecting to database:: " + e;
            connectError = true;
            return responseInformation;

        } // end try-catch


        //If there is not connection error, then we form the SQL statement
        //and then execute it.

        if (!connectError)
        {
            try
            {
                responseInformation = "";
                // create an SQL statement variable and create the INSERT
                // query to insert the new inventory into the database

                s = DBConn.createStatement();

                // now we build a query to list the inventory table contents
                // for the user
                if (databaseName.equals("inventory")) {
                    SQLstatement = ( "INSERT INTO " + databaseName + "." + inventoryName + " (product_code, " +
                            "description, quantity, price) VALUES ( '" +
                            productID + "', " + "'" + description + "', " +
                            quantity + ", " + perUnitCost + ");");
                }

                if (databaseName.equals("leaftech")) {
                    SQLstatement = ( "INSERT INTO " + databaseName + "." + inventoryName + " (productid, " +
                            "productdescription, productquantity, productprice) VALUES ( '" +
                            productID + "', " + "'" + description + "', " +
                            quantity + ", " + perUnitCost + ");");
                }

                // if trees are selected then insert inventory into trees
                // table
                if (inventoryName.equals("trees"))
                {
                    tableSelected = "TREES";

                }

                // if shrubs are selected then insert inventory into strubs
                // table
                if (inventoryName.equals("shrubs"))
                {
                    tableSelected = "SHRUBS";
                }

                // if seeds are selected then insert inventory into seeds
                // table
                if (inventoryName.equals("seeds"))
                {
                    tableSelected = "SEEDS";
                }

                if (inventoryName.equals("cultureboxes"))
                {
                    tableSelected = "CULTUREBOXES";
                }

                if (inventoryName.equals("processing"))
                {
                    tableSelected = "PROCESSING";
                }

                if (inventoryName.equals("referencematerials"))
                {

                    tableSelected = "REFERENCEMATERIALS";
                }

                if (inventoryName.equals("genomics"))
                {

                    tableSelected = "GENOMICS";
                }

                // execute the update
                executeUpdateVal = s.executeUpdate(SQLstatement);

                // let the user know all went well

                responseInformation += "\nINVENTORY UPDATED... The following was added to the " + tableSelected + " inventory...\n";
                responseInformation += "\nProduct Code:: " + productID;
                responseInformation += "\nDescription::  " + description;
                responseInformation += "\nQuantity::     " + quantity;
                responseInformation += "\nUnit Cost::    " + perUnitCost;

            } catch(Exception e) {

                errString =  "\nProblem adding inventory:: " + e;
                responseInformation += errString;
                executeError = true;
            } // try
        }
        return responseInformation;
    }

    public String deleteItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String token){
        Boolean connectError = false;   // Error flag
        Connection DBConn = null;       // MySQL connection handle
        Boolean executeError = false;   // Error flag
        String errString = null;        // String for displaying errors
        int executeUpdateVal;           // Return value from execute indicating effected rows
        Boolean fieldError = true;      // Error flag
        String msgString = null;        // String for displaying non-error messages
        ResultSet res = null;           // SQL query result set pointer
        String tableSelected = null;    // String used to determine which data table to use
        java.sql.Statement s = null;    // SQL statement pointer
        String SQLstatement = null;     // String for building SQL queries

        String responseInformation = "";  //response information
        try
        {
            responseInformation += "\n" + ">> Establishing Driver...";

            //load JDBC driver class for MySQL
            Class.forName( "com.mysql.jdbc.Driver" );

            responseInformation += "\n" + ">> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306";

            responseInformation += "\n" + ">> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db
            DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");

        } catch (Exception e) {

            responseInformation +=  "\nProblem connecting to database:: " + e;
            connectError = true;
            return responseInformation;

        } // end try-catch


        //If there is not connection error, then we form the SQL statement
        //and then execute it.

        if (!connectError)
        {
            try
            {
                responseInformation = "";
                // create an SQL statement variable and create the INSERT
                // query to insert the new inventory into the database

                s = DBConn.createStatement();

                // now we build a query to delete
                // for the user
                SQLstatement = ( "DELETE FROM " + databaseName + "." + inventoryName + " WHERE productid = '" + productID + "';");

                // execute the update
                executeUpdateVal = s.executeUpdate(SQLstatement);

                // let the user know all went well

                responseInformation += executeUpdateVal;
            } catch(Exception e) {

                errString =  "\nProblem with delete:: " + e;
                responseInformation += errString;
                executeError = true;
            } // try
        }
        return responseInformation;
    }

    public String decrementItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String token){
        Boolean connectError = false;   // Error flag
        Connection DBConn = null;       // MySQL connection handle
        Boolean executeError = false;   // Error flag
        String errString = null;        // String for displaying errors
        int executeUpdateVal;           // Return value from execute indicating effected rows
        Boolean fieldError = true;      // Error flag
        String msgString = null;        // String for displaying non-error messages
        ResultSet res = null;           // SQL query result set pointer
        String tableSelected = null;    // String used to determine which data table to use
        java.sql.Statement s = null;    // SQL statement pointer
        String SQLstatement1 = null;        // String for building SQL queries
        String SQLstatement2 = null;        // String for building SQL queries

        String responseInformation = "";  //response information
        try
        {
            responseInformation += "\n" + ">> Establishing Driver...";

            //load JDBC driver class for MySQL
            Class.forName( "com.mysql.jdbc.Driver" );

            responseInformation += "\n" + ">> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306";

            responseInformation += "\n" + ">> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db
            DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");

        } catch (Exception e) {

            responseInformation +=  "\nProblem connecting to database:: " + e;
            connectError = true;
            return responseInformation;

        } // end try-catch


        //If there is not connection error, then we form the SQL statement
        //and then execute it.

        if (!connectError)
        {
            try
            {
                responseInformation = "";
                // create an SQL statement variable and create the INSERT
                // query to insert the new inventory into the database

                s = DBConn.createStatement();

                if (databaseName.equals("inventory")) {
                    SQLstatement1 = ("UPDATE " + databaseName + "." + inventoryName + " set quantity=(quantity-1) where product_code = '" + productID + "';");
                    SQLstatement2 = ("SELECT * from " + databaseName + "." + inventoryName + " where product_code = '" + productID + "';");
                }

                if (databaseName.equals("leaftech")) {
                    SQLstatement1 = ("UPDATE " + databaseName + "." + inventoryName + " set productquantity=(productquantity-1) where productid = '" + productID + "';");
                    SQLstatement2 = ("SELECT * from " + databaseName + "." + inventoryName + " where productid = '" + productID + "';");
                }

                // if trees are selected then insert inventory into trees
                // table
                if (inventoryName.equals("trees"))
                {
                    tableSelected = "TREES";

                }

                // if shrubs are selected then insert inventory into strubs
                // table
                if (inventoryName.equals("shrubs"))
                {
                    tableSelected = "SHRUBS";
                }

                // if seeds are selected then insert inventory into seeds
                // table
                if (inventoryName.equals("seeds"))
                {
                    tableSelected = "SEEDS";
                }

                if (inventoryName.equals("cultureboxes"))
                {
                    tableSelected = "CULTUREBOXES";
                }

                if (inventoryName.equals("processing"))
                {
                    tableSelected = "PROCESSING";
                }

                if (inventoryName.equals("referencematerials"))
                {

                    tableSelected = "REFERENCEMATERIALS";
                }

                if (inventoryName.equals("genomics"))
                {

                    tableSelected = "GENOMICS";
                }

                // let the user know all went well

                // execute the update, then query the BD for the table entry for the item just changed
                // and display it for the user

                executeUpdateVal = s.executeUpdate(SQLstatement1);
                res = s.executeQuery(SQLstatement2);

                responseInformation += "\n\n" + productID + " inventory decremented...";

                while (res.next())
                {
                    msgString = tableSelected + ">> " + res.getString(1) + " :: " + res.getString(2) +
                            " :: "+ res.getString(3) + " :: " + res.getString(4);
                    responseInformation += "\n"+msgString;

                } // while

                responseInformation += "\n\n Number of items updated: " + executeUpdateVal;

            } catch(Exception e) {

                errString =  "\nProblem with decrement: " + e;
                responseInformation += errString;
                executeError = true;
            } // try
        }
        return responseInformation;
    }

    public String getItems(String SQLServerIP, String databaseName, String inventoryName, String token){
        Boolean connectError = false;   // Error flag
        Connection DBConn = null;       // MySQL connection handle
        Boolean executeError = false;   // Error flag
        String errString = null;        // String for displaying errors
        Boolean fieldError = true;      // Error flag
        String msgString = null;        // String for displaying non-error messages
        ResultSet res = null;           // SQL query result set pointer
        String tableSelected = null;    // String used to determine which data table to use
        java.sql.Statement s = null;    // SQL statement pointer

        String responseInformation = "";  //response information
        try
        {
            responseInformation += "\n" + ">> Establishing Driver...";

            //load JDBC driver class for MySQL
            Class.forName( "com.mysql.jdbc.Driver" );

            responseInformation += "\n" + ">> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306";

            responseInformation += "\n" + ">> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db
            DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");

        } catch (Exception e) {

            responseInformation +=  "\nProblem connecting to database:: " + e;
            connectError = true;
            return responseInformation;

        } // end try-catch


        //If there is not connection error, then we form the SQL statement
        //and then execute it.

        if (!connectError)
        {
            try
            {
                responseInformation = "";
                // create an SQL statement variable and create the INSERT
                // query to insert the new inventory into the database

                s = DBConn.createStatement();

                // now we build a query to list the inventory table contents
                // for the user

                res = s.executeQuery( "Select * from " + databaseName + "." + inventoryName );
                if (inventoryName.equals("trees"))
                    tableSelected = "TREE";
                if (inventoryName.equals("shrubs"))
                    tableSelected = "SHRUB";
                if (inventoryName.equals("seeds"))
                    tableSelected = "SEED";
                if (inventoryName.equals("cultureboxes"))
                    tableSelected = "CULTUREBOX";
                if (inventoryName.equals("processing"))
                    tableSelected = "PROCESSING";
                if (inventoryName.equals("referencematerials"))
                    tableSelected = "REFERENCEMATERIALS";
                if (inventoryName.equals("genomics"))
                    tableSelected = "GENOMICS";


                // Now we list the inventory for the selected table
                while (res.next())
                {
                    msgString = tableSelected+">>" + res.getString(1) + "::" + res.getString(2) +
                            " :: "+ res.getString(3) + "::" + res.getString(4);
                    responseInformation += "\n"+msgString;
                } // while

            } catch(Exception e) {

                errString =  "\nProblem with " + tableSelected +" query:: " + e;
                responseInformation += errString;
                executeError = true;
            } // try
        }
        return responseInformation;
    }
}
