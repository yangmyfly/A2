/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import middleware.MiddleWare;
import middleware.MiddlewareInterface;

/**
 *
 * @author ashwin
 */
public class MiddlewareServer extends UnicastRemoteObject {

    public MiddlewareServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
//            System.setProperty("java.rmi.server.hostname","localhost");
             Registry registry = LocateRegistry.createRegistry(5050);
            // create a new service named myMessage
            MiddlewareInterface obj = (MiddlewareInterface) new MiddleWare();
            MiddlewareInterface stubServer = (MiddlewareInterface) UnicastRemoteObject.exportObject(obj,5050);
            Naming.rebind("//127.0.0.1:5050/middleware", stubServer);
            System.out.println("Server is connected and ready for operation.");
//            for(;;){
//            }
        } catch (Exception e) {
            System.out.println("Server not connected: " + e);
        }
    }
}
