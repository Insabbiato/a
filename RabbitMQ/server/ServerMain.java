/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.serverrest;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.*;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

/**
 *
 * @author biar
 */
public class ServerMain {
    
    public static void main(String[] args) throws Exception{
        Identity auth = new Identity();
        Catalog catalogo = new Catalog();
        
        auth.getToken();
        
        catalogo.getCatalog();
    }
    
}
