/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import com.mycompany.catalog.Product;


public class Products{
    private ArrayList<Product> products;
    public Products(){
        products = new ArrayList<Product>();
    }
    
    public void addProduct(String name){
        Stream<Product> stream = this.products.stream();
        Stream<Integer> ids = stream.map(p -> new Integer(p.getId()));
 
        Object[] idsArray;
        idsArray = ids.toArray();
        Random rand = new Random();
        int idx;
        while(true){
            idx = rand.nextInt();
            boolean used = false;
            for (int i = 0;i<idsArray.length;i++){
                if(idsArray[i].equals(new Integer(idx))){
                    used = true;
                    break;
                }
            }
            if(!used)break;
            else continue;  
        }
        Product p = new Product(idx,name);
        this.products.add(p);
 
    }
    
    public List<Product> getProducts(){
        return (List<Product>)this.products.clone();
    }
}