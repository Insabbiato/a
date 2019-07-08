/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.catalog;

import java.util.List;


/**
 *
 * @author biar
 */
public class Product {
    final private int id;
    private String Name;
    
    public Product(int id,String Name){
        this.id = id;
        this.Name = Name;
    }
    
    public int getId(){return this.id;}
    public String getName(){return this.Name;}
    
}




