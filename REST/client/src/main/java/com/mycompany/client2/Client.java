/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.clientlab2;

import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 *
 * @author biar
 */
public class MyClient {
    
    public static void main(String[] args) throws Exception{
        WebClient client = WebClient.create("http://localhost:8080/course");
        
        //GET course
        Course course = client.path("courses/1").accept("text/xml").get().readEntity(Course.class);
        System.out.println(course.getName());
        
        //POST student
        Student student = new Student();
        student.setId(100);
        student.setName("MASSIMO DECIMO MECELLO");
        Response r = client.path("students").post(student);
        System.out.println(r.getStatus());
        
        //GET student
        Student mecello = client.path("100").get().readEntity(Student.class);
        System.out.println(mecello.getName());
        
    }
    
}
