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
@XmlRootElement(name = "Identity")
public class Identity {
    //private final Channel queue;
    //private final Channel queueRet;
    private final static String queueName = "CATZ_AUTH";
    private final static String queueNameRet = "CATZ_AUTH_RET";
    private String message = "user:pw";
    private String messageRet;
  
    /*
    public Identity() throws Exception{
        
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
        Channel channel = connectionRet.createChannel();
        queue.queueDeclare(queueNameRet, false, false, false, null);
        //queueRet = channelRet;
    }*/
    
    public void getToken() throws Exception{
        //SEND MESSAGE
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.49.81");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
                channel.queueDeclare(queueName, false, false, false, null);
                //queue = channel;
                channel.basicPublish("", queueName, null, message.getBytes());
            }
        //queue.basicPublish("", queueName, null, message.getBytes());
        
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
    }

    public String getMessageRet() {
        return messageRet;
    }

    public void setMessageRet(String messageRet) {
        this.messageRet = messageRet;
    }
    
}
