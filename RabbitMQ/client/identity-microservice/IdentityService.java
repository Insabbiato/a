/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.identity.microservice;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Timestamp;
import javax.xml.bind.DatatypeConverter;


class TokenMapEntry {
    long token;
    long timestamp;
    
    TokenMapEntry(long token, long timestamp) {
        this.token = token;
        this.timestamp = timestamp;
    }
}


public class IdentityService {
    private final static String QUEUE_AUTH = "CATZ_AUTH";
    private final static String QUEUE_AUTH_RET = "CATZ_AUTH_RET";
    private final static String QUEUE_VERIFY = "CATZ_VERIFY";
    private static final Map<String, TokenMapEntry> users_tokens = new HashMap<>();
    private static final Map<String, String> users_pw = new HashMap<>();
    private static final Random RND = new Random(42);
    
    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.49.81");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        channel.queueDeclare(QUEUE_AUTH, false, false, false, null);
        channel.queueDeclare(QUEUE_AUTH_RET, false, false, false, null);
        channel.queueDeclare(QUEUE_VERIFY, false, false, false, null);
        
        DeliverCallback authCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received in AUTH'" + message + "'");
            
            // retrieve user and pw
            String[] msg_split = message.split(":");
            if (msg_split.length != 2) {
                System.out.println("skipping message "+message);
                return;
            }
            String user = msg_split[0];
            String pw = msg_split[1];
            
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(pw.getBytes(StandardCharsets.UTF_8));
                String hash_str = bytesToHex(hash);
                System.out.println("hashed "+hash_str);
                
                // hash from db
                Boolean user_contained = users_pw.containsKey(user);
                if (!user_contained) {
                    System.out.println("not in db");
                    return;
                }

                String true_hash = users_pw.get(user);
                
                if (true_hash.equals(hash_str)) {
                    System.out.println("authenticated");
                    long new_token = RND.nextLong();
                    long timestamp = System.currentTimeMillis();
                    TokenMapEntry entry = new TokenMapEntry(new_token, timestamp);
                    users_tokens.put(user, entry);
                    channel.basicPublish("", QUEUE_AUTH_RET, null, String.valueOf(new_token).getBytes("UTF-8"));
                    System.out.println("new token "+String.valueOf(new_token));
                } else {
                    System.out.println("not authenticated");
                    channel.basicPublish("", QUEUE_AUTH_RET, null, "NO".getBytes("UTF-8"));
                }

            } catch (NoSuchAlgorithmException ex) {
                System.out.println("mucho errore");
            }
            
        };
        
        DeliverCallback verifyCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received in VERIFY'" + message + "'");
        };
        
        channel.basicConsume(QUEUE_AUTH, true, authCallback, consumerTag -> { });
        channel.basicConsume(QUEUE_VERIFY, true, authCallback, consumerTag -> { });
        
        
        // TEST
        String dummy_hash = "30C952FAB122C3F9759F02A6D95C3758B246B4FEE239957B2D4FEE46E26170C4";
        users_pw.put("user", dummy_hash);
        String message = "user:pw";
        channel.basicPublish("", QUEUE_AUTH, null, message.getBytes("UTF-8"));
        System.out.println("message sent");
    }
    
    private static String bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash);
    }
}
