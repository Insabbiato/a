/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.catalog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mycompany.catalog.Products;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.List;

/**
 *
 * @author biar
 */
public class CatalogMS {
    private final static String QUEUE_NAME = "CATZ_CATALOG";
    private final static String QUEUE_NAME_RET = "CATZ_CATALOG_RET";
    private final static String HOST = "192.168.49.81";

    public static void main(String[] argv) throws Exception {
        
        // CREATE PRODUCTS
        Products prod = new Products();
        for(int i=0;i<10;i++)prod.addProduct("Control");
        for(int i=0;i<10;i++)prod.addProduct("Durex");
        for(int i=0;i<10;i++)prod.addProduct("Akuel");
        for(int i=0;i<10;i++)prod.addProduct("Pesante");
        for(int i=0;i<10;i++)prod.addProduct("Esp");
        for(int i=0;i<10;i++)prod.addProduct("Masculan");
        for(int i=0;i<10;i++)prod.addProduct("Serena");
        
        // JSON CATALOG
        JsonArray jcat = new JsonArray();
        List<Product> lprods = prod.getProducts();
        for(int i=0;i<lprods.size();i++){
            Product p = lprods.get(i);
            JsonObject jp = new JsonObject();
            jp.addProperty("name", p.getName());
            jp.addProperty("id", p.getId());
            jcat.add(jp);
        }
        //-------------------------------------------------
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // RECEIVE QUEUE
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            if(message.equals("DammeErCatalogo")){
                System.out.println("\t->Sending Catalog");
                channel.basicPublish("", QUEUE_NAME_RET, null,jcat.toString().getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
                
            }
        };
        // SEND QUEUE
            channel.queueDeclare(QUEUE_NAME_RET, false, false, false, null);

            
        // START CONSUME
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        
    }
}

/*
{
    "data":[

        {"name":"prod_name", "id":"prod_id"},
        {"name":"prod_name", "id":"prod_id"}

    ]
}
*/

