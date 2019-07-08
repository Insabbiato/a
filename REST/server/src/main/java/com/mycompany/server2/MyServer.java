/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server2;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.*;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;


/**
 *
 * @author biar
 */
public class MyServer {
    
    public static void main(String[] args) throws Exception{
        JAXRSServerFactoryBean factoryBean = new JAXRSServerFactoryBean();
        factoryBean.setResourceClasses(CourseRepository.class);
        factoryBean.setResourceProvider(new SingletonResourceProvider(new CourseRepository()));
        factoryBean.setAddress("http://localhost:8080/");	
        Server server = factoryBean.create();
    }
    
}
