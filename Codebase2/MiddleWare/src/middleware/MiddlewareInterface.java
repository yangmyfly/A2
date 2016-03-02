/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package middleware;

import java.rmi.*;
import java.util.HashMap;

/**
 *
 * @author ashwin
 */
public interface MiddlewareInterface extends Remote {

    public String login(String userName, char[] password) throws RemoteException ;
    
    public Boolean logout(String token) throws RemoteException ;

    public HashMap<String,String> getOrder(String orderID,String SQLServerIP,String token) throws RemoteException;
    
    public HashMap<String,String> updateOrder(String orderID, String SQLServerIP,String token) throws RemoteException;
    
    public HashMap<String,String> getOrders(String SQLServerIP,int status,String token) throws RemoteException;
    
     public String[] getAllItems(String serverIP, String databaseName, String inventoryName) throws RemoteException;
     
     public String[] getSelectedIteam(String inventorySelection, String sTotalCost) throws RemoteException;
     
     public String[] submitOrder(String first, String last, String address, String phone, String serverIP, String sTotalCost, String[] items) throws RemoteException;

     public String addItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String description, Integer quantity, Float perUnitCost, String token) throws RemoteException;

    public String deleteItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String token) throws RemoteException;

    public String decrementItems(String SQLServerIP, String databaseName, String inventoryName, String productID, String token) throws RemoteException;

    public String getItems(String SQLServerIP, String databaseName, String inventoryName, String token) throws RemoteException;
}
