/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package middleware;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ashwin
 */
public class MiddleWare implements MiddlewareInterface {

    public String login(String userName, char[] password) {
        String token = null;
        try {
            LoginAndOut obj = new LoginAndOut();
            token = obj.login(userName, String.valueOf(password));
            //handle login here
        } catch (Exception ex) {
            Logger.getLogger(MiddleWare.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }

    public Boolean logout(String token) {
        boolean res = false;
        try {
            LoginAndOut obj = new LoginAndOut();
            res = obj.logout(token);
            //handle login here
        } catch (Exception ex) {
            Logger.getLogger(MiddleWare.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public HashMap<String, String> getOrder(String orderID, String SQLServerIP, String token) throws RemoteException {

        Boolean connectError = false;       // Error flag
        Connection DBConn = null;           // MySQL connection handle
        String msgString = null;            // String for displaying non-error messages
        ResultSet res = null;               // SQL query result set pointer
        Statement s = null;                 // SQL statement pointer
        String SQLStatement;                // SQL query
        HashMap map = new HashMap<String, String>();
        map.put("error", "false");
        LoginAndOut obj = new LoginAndOut();
        try {
            if (!obj.isLoggedIn(token)) {
                map.put("expired", "true");
                return map;
            }
            // If an order was selected, then connect to the orderinfo database. In
            // all normal situations this would be impossible to do since the select
            // button is disabled until an order is selected... just in case the
            // check is here.
        } catch (Exception ex) {
            Logger.getLogger(MiddleWare.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            msgString = ">> Establishing Driver...";

            //Load J Connector for MySQL - explicit loads are not needed for
            //connectors that are version 4 and better
            //Class.forName( "com.mysql.jdbc.Driver" );
            msgString += "\n>> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306/orderinfo";

            msgString += "\n>> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db - note the default account is "remote"
            //and the password is "remote_pass" - you will have to set this
            //account up in your database
            DBConn = DriverManager.getConnection(sourceURL, "remote", "remote_pass");

        } catch (Exception e) {

            msgString = "\nProblem connecting to orderinfo database:: " + e;
            connectError = true;
        } // end try-catch
        map.put("conmessage", msgString);

        if (!connectError) {
            try {
                s = DBConn.createStatement();
                SQLStatement = "SELECT * FROM orders WHERE order_id = " + Integer.parseInt(orderID);
                res = s.executeQuery(SQLStatement);
                // Get the information from the database. Display the
                // first and last name, address, phone number, address, and
                // order date. Same the ordertable name - this is the name of
                // the table that is created when an order is submitted that
                // contains the list of order items.
                while (res.next()) {
                    map.put("orderTable", res.getString(9));
                    map.put("firstname", res.getString(3)); // first name
                    map.put("lastname", res.getString(4)); // last name
                    map.put("phone", res.getString(6)); // phone
                    map.put("orderdate", res.getString(2)); // order date
                    map.put("address", res.getString(5));  // address

                } // for each element in the return SQL query

                // get the order items from the related order table
                SQLStatement = "SELECT * FROM " + map.get("orderTable");
                res = s.executeQuery(SQLStatement);

                // list the items on the form that comprise the order
                msgString = "";
                while (res.next()) {
                    msgString += res.getString(1) + ":  PRODUCT ID: " + res.getString(2)
                            + "  DESCRIPTION: " + res.getString(3) + "  PRICE $" + res.getString(4);
                    msgString += "\n";

                } // while

                map.put("result", msgString);
                map.put("orderID", orderID);
                // Update the form
                msgString = "RECORD RETRIEVED...";

            } catch (Exception e) {
                msgString = "\nProblem getting order items:: " + e;
                map.put("error", "true");
            } // end try-catch
            map.put("quemessage", msgString);

        } // connect and blank order check
        return map;

    }

    @Override
    public HashMap<String, String> updateOrder(String orderID, String SQLServerIP, String token) throws RemoteException {

        Boolean connectError = false;       // Error flag
        Connection DBConn = null;           // MySQL connection handle
        String msgString = null;            // String for displaying non-error messages
        Statement s = null;                 // SQL statement pointer
        String SQLStatement = null;         // SQL statement string
        HashMap map = new HashMap<String, String>();
        map.put("error", "false");
        LoginAndOut obj = new LoginAndOut();
        
            
         
        // Connect to the order database
        try {
            
            if (!obj.isLoggedIn(token)) {
                map.put("expired", "true");
                return map;
            }
            msgString = "\n>> Establishing Driver...";

            //load JDBC driver class for MySQL
            Class.forName("com.mysql.jdbc.Driver");

            msgString += "\n>> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306/orderinfo";

            msgString += "\n>> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db - note the default account is "remote"
            //and the password is "remote_pass" - you will have to set this
            //account up in your database
            DBConn = DriverManager.getConnection(sourceURL, "remote", "remote_pass");

        } catch (Exception e) {

            msgString = "\nProblem connecting to orderinfo database:: " + e;
            connectError = true;

        } // end try-catch
        map.put("message", msgString);

        // If we are connected, then we update the shipped status
        if (!connectError) {
            try {
                // first we create the query
                s = DBConn.createStatement();
                SQLStatement = "UPDATE orders SET shipped=" + true + " WHERE order_id=" + orderID;

                // execute the statement
                map.put("rows", "" + s.executeUpdate(SQLStatement));

            } catch (Exception e) {

                msgString = "\nProblem updating status:: " + e;
                map.put("error", "true");
                map.put("emessage", msgString);
            } // end try-catch

        } // if connect check
        return map;
    }

    @Override
    public HashMap<String, String> getOrders(String SQLServerIP, int status, String token) throws RemoteException {

        // This method is responsible for querying the orders database and
        // getting the list of orders that have been shipped. The list of shipped
        // orders is written to jTextArea1.
        String prependString;
        prependString = (status == 1) ? "SHIPPED" : "PENDING";
        Boolean connectError = false;       // Error flag
        Connection DBConn = null;           // MySQL connection handle
        String msgString = null;            // String for displaying non-error messages
        ResultSet res = null;               // SQL query result set pointer
        Statement s = null;                 // SQL statement pointer
        int shippedStatus;                  // if 0, order not shipped, if 1 order shipped
        HashMap map = new HashMap<String, String>();
        map.put("error", "false");
        LoginAndOut obj = new LoginAndOut();


            
      

        // Connect to the order database
        try {
            if (!obj.isLoggedIn(token)) {
                map.put("expired", "true");
                return map;
            }

            //load JDBC driver class for MySQL
            Class.forName("com.mysql.jdbc.Driver");

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306/orderinfo";

            //create a connection to the db - note the default account is "remote"
            //and the password is "remote_pass" - you will have to set this
            //account up in your database
            DBConn = DriverManager.getConnection(sourceURL, "remote", "remote_pass");

        } catch (Exception e) {
            connectError = true;

        } // end try-catch
        // If we are connected, then we get the list of trees from the
        // inventory database
        if (!connectError) {
            try {
                // Create a query to get all the rows from the orders database
                // and execute the query.
                s = DBConn.createStatement();
                res = s.executeQuery("Select * from orders");

                // For each row returned, we check the shipped status. If it is
                // equal to 0 it means it has not been shipped as of yet, so we
                // display it in TextArea 1. Note that we use an integer because
                // MySQL stores booleans and a TinyInt(1), which we interpret
                // here on the application side as an integer. It works, it just
                // isn't very elegant.
                msgString="";
                while (res.next()) {
                    shippedStatus = Integer.parseInt(res.getString(8));

                    if (shippedStatus == status) {
                        msgString += prependString + " ORDER # " + res.getString(1) + " : " + res.getString(2)
                                + " : " + res.getString(3) + " : " + res.getString(4);
                        msgString += "\n";

                    } // shipped status check

                } // while
                map.put("result", msgString);
                msgString = "\n" + prependString + " ORDERS RETRIEVED...";

            } catch (Exception e) {

                msgString = "\nProblem getting tree inventory:: " + e;
                map.put("error", "true");
            } // end try-catch
            map.put("message", msgString);
        } // connect check

        return map;
    }

    @Override
    public String[] getAllItems(String serverIP, String databaseName, String inventoryName, String token) throws RemoteException{
        String information = "";  /*results including failure information*/
        Connection DBConn = null;           // MySQL connection handle
        Boolean connectError = false;       // Error flag
        ResultSet res = null;               // SQL query result set pointer
        Statement s = null;                 // SQL statement pointer
        String [] error = null;
        
        // connect
        try{
            if (!LoginAndOut.isLoggedIn(token)) {
                return null;
            }
            information = "\n>> Establishing Driver..."; 
            Class.forName("com.mysql.jdbc.Driver");
            information += "\n>> Setting up URL...";
            String sourceURL = "jdbc:mysql://" + serverIP + ":3306/" + databaseName;
            information += "\n>> Establishing connection with: " + sourceURL + "...";
            DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");
            
        } catch (Exception e) {
            information += "\nProblem connecting to database:: " + e;
            connectError = true;
            String[] result = new String[1];
            result[0] = information;
            return result;
        }
        String[] result = new String[2];
        result[0] = information;
        
        if ( !connectError )
        {
            try
            {
                s = DBConn.createStatement();
                res = s.executeQuery( "Select * from " + inventoryName );
                
                information = "";

                while (res.next())
                {
                    information += res.getString(1) + " : " + res.getString(2) +
                            " : $"+ res.getString(4) + " : " + res.getString(3)
                            + " units in stock\n";
                } // while
                
            } catch (Exception e) {
                information =  "\nProblem getting " + inventoryName +" inventory:: " + e;
            } // end try-catch
        }
        result[1] = information;
        return result;
    }
    
    public String[] getSelectedIteam(String inventorySelection, String sTotalCost, String token) throws RemoteException{
        int beginIndex;                     // Parsing index
        int endIndex;                       // Parsing index
        Float fCost;                        // Item cost
        String productDescription = null;   // Product description
        String productID = null;            // Product ID pnemonic
        String sCost;            // String order and total cost values
        Boolean IndexNotFound;              // Flag indicating a string index was not found.
        
        IndexNotFound = false;
        sCost = null;
        String[] error = null;
        try {
            if (!LoginAndOut.isLoggedIn(token)) {
                return error;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        if ( inventorySelection != null )
        {
            beginIndex = 0;
            endIndex = inventorySelection.indexOf(" : ",beginIndex);
            if (endIndex < 0 ) {
                IndexNotFound = true;
            } else {
                productID = inventorySelection.substring(beginIndex,endIndex);
            }
            
            if ( !IndexNotFound )
            {
                // get the product description
                beginIndex = endIndex + 3; //skip over " : "
                endIndex = inventorySelection.indexOf(" : ",beginIndex);
                if (endIndex < 0 ) {
                    IndexNotFound = true;
                } else {
                    productDescription = inventorySelection.substring(beginIndex,endIndex);
                }              
            }
            
            // get the string cost value
            if ( !IndexNotFound )
            {
                beginIndex = endIndex + 4; //skip over " : $"
                endIndex = inventorySelection.indexOf(" : ",beginIndex);
                if (endIndex < 0 ) {
                    IndexNotFound = true;
                } else {
                    sCost = inventorySelection.substring(beginIndex,endIndex);
                }
            }
            
            if ( !IndexNotFound )
            {
                String []information = new String [2];
                information[0] = productID + " : " + productDescription + " : $" + sCost + "\n";
                
                beginIndex = 0;
                beginIndex = sTotalCost.indexOf("$",beginIndex)+1;
                sTotalCost = sTotalCost.substring(beginIndex, sTotalCost.length());
                fCost = Float.parseFloat(sTotalCost) + Float.parseFloat(sCost);            
                information[1] = "$" + fCost.toString();
                
                return information;
            } else {
                String []information = new String [1];
                information[0] = "\nNo items selected...\nSELECT ENTIRE INVENTORY LINE TO ADD ITEM TO ORDER\n(TRIPLE CLICK ITEM LINE)";
                return information;      
            }       
        } else {
            String []information = new String [1];
            information[0] = "\nNo items selected...\nSELECT ENTIRE INVENTORY LINE TO ADD ITEM TO ORDER\n(TRIPLE CLICK ITEM LINE)";
            return information;  
        }
    }
    
    public String[] submitOrder(String first, String last, String address, String phone, String serverIP, String sTotalCost, String[] items , String token) throws RemoteException {
        // This is the submit order button. This handler will check to make sure
        // that the customer information is provided, then create an entry in
        // the orderinfo::orders table. It will also create another table where
        // the list of items is stored. This table is also in the orderinfo
        // database as well.
        
        int beginIndex;                 // String parsing index
        Boolean connectError = false;   // Error flag
        int endIndex;                   // String paring index
        Connection DBConn = null;       // MySQL connection handle
        float fCost;                    // Total order cost
        String description;             // Tree, seed, shrub, cultureboxrs, genomics, processing, or referencematerials description
        Boolean executeError = false;   // Error flag
        String errString = null;        // String for displaying errors
        int executeUpdateVal;           // Return value from execute indicating effected rows
        String msgString = null;        // String for displaying non-error messages
        String orderTableName = null;   // This is the name of the table that lists the items
        String sPerUnitCost = null;     // String representation of per unit cost
        String orderItem = null;        // Order line item from jTextArea2
        Float perUnitCost;              // Cost per tree, seed, , shrub, cultureboxrs, genomics, processing, or referencematerials unit
        String productID = null;        // Product id of tree, seed, shrub, cultureboxrs, genomics, processing, or referencematerials
        Statement s = null;             // SQL statement pointer
        String SQLstatement = null;     // String for building SQL queries
        
        String information = "";
        String[] error = null;

        // Check to make sure there is a first name, last name, address and phone
        if ((first.length()>0) && (last.length()>0) && (address.length()>0) && (phone.length()>0))
        {
            try
            {
//                if (!LoginAndOut.isLoggedIn(token)) {
//                    return error;
//                }
                information = "\n>> Establishing Driver...";
                //load JDBC driver class for MySQL
                Class.forName( "com.mysql.jdbc.Driver" );

                information += "\n>> Setting up URL...";
              
                String sourceURL = "jdbc:mysql://" + serverIP + ":3306/orderinfo";

                information += "\n>> Establishing connection with: " + sourceURL + "...";               

                //create a connection to the db - note the default account is "remote"
                //and the password is "remote_pass" - you will have to set this
                //account up in your database

                DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");

            } catch (Exception e) {

                information =  "\nError connecting to orderinfo database\n" + e;              
                connectError = true;
                String[] result = new String [1];
                result[0] = information;
                return result;
            } // end try-catch

        } else {

            information =  "\nMissing customer information!!!\n";
            connectError = true;
            String[] result = new String [1];
            result[0] = information;
            return result;
        }// customer data check

        //If there is not a connection error, then we form the SQL statement
        //to submit the order to the orders table and then execute it.

        if (!connectError )
        {
            Calendar rightNow = Calendar.getInstance();

            int TheHour = rightNow.get(rightNow.HOUR_OF_DAY);
            int TheMinute = rightNow.get(rightNow.MINUTE);
            int TheSecond = rightNow.get(rightNow.SECOND);
            int TheDay = rightNow.get(rightNow.DAY_OF_WEEK);
            int TheMonth = rightNow.get(rightNow.MONTH);
            int TheYear = rightNow.get(rightNow.YEAR);
            orderTableName = "order" + String.valueOf(rightNow.getTimeInMillis());

            String dateTimeStamp = TheMonth + "/" + TheDay + "/" + TheYear + " "
                    + TheHour + ":" + TheMinute  + ":" + TheSecond;

            // Get the order data
            beginIndex = 0;
            beginIndex = sTotalCost.indexOf("$",beginIndex)+1;
            sTotalCost = sTotalCost.substring(beginIndex, sTotalCost.length());
            fCost = Float.parseFloat(sTotalCost);
                
            try
            {
                s = DBConn.createStatement();

                SQLstatement = ( "CREATE TABLE " + orderTableName +
                            "(item_id int unsigned not null auto_increment primary key, " +
                            "product_id varchar(20), description varchar(80), " +
                            "item_price float(7,2) );");

                executeUpdateVal = s.executeUpdate(SQLstatement);

            } catch (Exception e) {

                information +=  "\nProblem creating order table " + orderTableName +":: " + e;
                executeError = true;
                String[] result = new String [1];
                result[0] = information;
                return result;
            } // try

            if ( !executeError )
            {
                try
                {
                    SQLstatement = ( "INSERT INTO orders (order_date, " + "first_name, " +
                        "last_name, address, phone, total_cost, shipped, " +
                        "ordertable) VALUES ( '" + dateTimeStamp + "', " +
                        "'" + first + "', " + "'" + last + "', " +
                        "'" + address + "', " + "'" + phone + "', " +
                        fCost + ", " + false + ", '" + orderTableName +"' );");

                    executeUpdateVal = s.executeUpdate(SQLstatement);
                    
                } catch (Exception e1) {

                    information +=  "\nProblem with inserting into table orders:: " + e1;
                    executeError = true;

                    try
                    {
                        SQLstatement = ( "DROP TABLE " + orderTableName + ";" );
                        executeUpdateVal = s.executeUpdate(SQLstatement);

                    } catch (Exception e2) {

                        information +=  "\nProblem deleting unused order table:: " +
                                orderTableName + ":: " + e2;

                    } // try
                    String[] result = new String [1];
                    result[0] = information;
                    return result;
                } // try

            } //execute error check

        } 

        // Now, if there is no connect or SQL execution errors at this point, 
        // then we have an order added to the orderinfo::orders table, and a 
        // new ordersXXXX table created. Here we insert the list of items in
        // jTextArea2 into the ordersXXXX table.
        String information2 = "";
        String information3 = "";
        String information4 = "";
        
        if ( !connectError && !executeError )
        {
            // Now we create a table that contains the itemized list
            // of stuff that is associated with the order

            for (int i = 0; i < items.length; i++ )
            {
                orderItem = items[i];
                
                // Check just to make sure that a blank line was not stuck in
                // there... just in case.
                
                if (orderItem.length() > 0 )
                {
                    // Parse out the product id
                    beginIndex = 0;
                    endIndex = orderItem.indexOf(" : ",beginIndex);
                    productID = orderItem.substring(beginIndex,endIndex);

                    // Parse out the description text
                    beginIndex = endIndex + 3; //skip over " : "
                    endIndex = orderItem.indexOf(" : ",beginIndex);
                    description = orderItem.substring(beginIndex,endIndex);

                    // Parse out the item cost
                    beginIndex = endIndex + 4; //skip over " : $"
                    //endIndex = orderItem.indexOf(" : ",orderItem.length());
                    //sPerUnitCost = orderItem.substring(beginIndex,endIndex);
                    sPerUnitCost = orderItem.substring(beginIndex,orderItem.length());
                    perUnitCost = Float.parseFloat(sPerUnitCost);

                    SQLstatement = ( "INSERT INTO " + orderTableName +
                        " (product_id, description, item_price) " +
                        "VALUES ( '" + productID + "', " + "'" +
                        description + "', " + perUnitCost + " );");
                    try
                    {
                        executeUpdateVal = s.executeUpdate(SQLstatement);
                        information2 =  "\nORDER SUBMITTED FOR: " + first + " " + last;
                            
                    } catch (Exception e) {

                        information3 =  "\nProblem with inserting into table " + orderTableName +
                            ":: " + e;
                        information4 = Integer.toString(i);
                        String[] result = new String[4];
                        result[0] = information;
                        result[1] = information2;
                        result[2] = information3;
                        result[3] = information4;
                        return result;
                    } // try

                } // line length check

            } //for each line of text in order table
            
        }
        String[] result = new String[4];
        result[0] = information;
        result[1] = information2; 
        return result;
    }


    public String addItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String description, Integer quantity, Float perUnitCost, String token) throws RemoteException {
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

        try {
            if (!LoginAndOut.isLoggedIn(token)) {
                return "falseAuth";
            }
        }catch(Exception e){
            e.printStackTrace();
        }

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

    public String deleteItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String token) throws RemoteException {
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

        try {
            if (!LoginAndOut.isLoggedIn(token)) {
                return "falseAuth";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
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

    public String decrementItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String token) throws RemoteException {
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

        try {
            if (!LoginAndOut.isLoggedIn(token)) {
                return "falseAuth";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
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

    public String getItems(String SQLServerIP, String databaseName, String inventoryName, String token) throws RemoteException {
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

        try {
            if (!LoginAndOut.isLoggedIn(token)) {
                return "falseAuth";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
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
