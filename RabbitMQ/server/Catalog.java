/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.serverrest;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.util.*;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlRootElement;
import org.json.JSONObject;

/**
 *
 * @author biar
 */
@XmlRootElement(name = "Catalog")
public class Catalog {
    private static List<String> catalogList;
    //private final Channel queue;
    //private final Channel queueRet;
    private final static String queueName = "CATZ_CATALOG";
    private final static String queueNameRet = "CATZ_CATALOG_RET";
    private String message = "DammeErCatalogo";
    private String messageRet;

    public static List<String> getCatalogList() {
        return catalogList;
    }

    public static void setCatalogList(List<String> catalogList) {
        Catalog.catalogList = catalogList;
    }
    
    /*
    public Catalog() throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.49.81");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
                channel.queueDeclare(queueName, false, false, false, null);
                queue = channel;
            }
        
        ConnectionFactory factoryRet = new ConnectionFactory();
        factoryRet.setHost("192.168.49.81");
        Connection connectionRet = factoryRet.newConnection();
        Channel channelRet = connectionRet.createChannel();
        channelRet.queueDeclare(queueNameRet, false, false, false, null);
        queueRet = channelRet;
    }
    */
    
    @GET
    @Path("catalog")
    public List<String> getCatalog() throws Exception{
        //SEND MESSAGE
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.49.81");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
                channel.queueDeclare(queueName, false, false, false, null);
                //queue = channel;
                channel.basicPublish("", queueName, null, message.getBytes());
            }
        
        //RECEIVE RESPONSE
        ConnectionFactory factoryRet = new ConnectionFactory();
        factoryRet.setHost("192.168.49.81");
        Connection connectionRet = factoryRet.newConnection();
        Channel channel = connectionRet.createChannel();
        channel.queueDeclare(queueNameRet, false, false, false, null);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageRet = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + messageRet + "'");
            this.messageRet = messageRet;
        };
        channel.basicConsume(queueNameRet, true, deliverCallback, consumerTag -> { });
        //JSONObject catalog = new JSONObject(messageRet);
        

        return new ArrayList<>();
    }

}
