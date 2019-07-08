# JMS

![](JMS.png | width=400)

## Server

### Main

    public class StockMarketServer {
        //I create the two object that run on the server side
    	public static void main(String args[]) throws Exception {
            NotificatoreAcquisto n = new NotificatoreAcquisto();
            n.start();
            ProduttoreQuotazioni q = new ProduttoreQuotazioni();
            q.start();
    	}
    }

### ProduttoreQuotazioni

    public class ProduttoreQuotazioni {
    	final String titoli[] = { "Telecom", "Finmeccanica", "Banca_Intesa",
    			"Oracle", "Parmalat", "Mondadori", "Vodafone", "Barilla" };
    
    	private String scegliTitolo() {
    		int whichMsg;
    		Random randomGen = new Random();
    		whichMsg = randomGen.nextInt(this.titoli.length);
    		return this.titoli[whichMsg];
    	}
    
    	private float valore() {
    		Random randomGen = new Random();
    		float val = randomGen.nextFloat() * this.titoli.length * 10;
    		return val;
    	}
    
        private static final Logger LOG = LoggerFactory.getLogger(ProduttoreQuotazioni.class);
    
    	public void start() throws NamingException, JMSException {
    
            //initialize all the object for JNDI
            Context jndiContext = null;
            ConnectionFactory connectionFactory = null;
            Connection connection = null;
            Session session = null;
            Destination destination = null;
            MessageProducer producer = null;
            String destinationName = "dynamicTopics/Quotazioni";
    
            /*
             * Create a JNDI API InitialContext object
             */
            try {
                Properties props = new Properties();
                props.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
                props.setProperty(Context.PROVIDER_URL,"tcp://localhost:61616");
                jndiContext = new InitialContext(props);
            } catch (NamingException e) {
                LOG.info("ERROR in JNDI: " + e.toString());
                System.exit(1);
            }
    
            /*
             * Look up connection factory and destination.
             */
            try {
                connectionFactory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");
                destination = (Destination)jndiContext.lookup(destinationName);
            } catch (NamingException e) {
                LOG.info("JNDI API lookup failed: " + e);
                System.exit(1);
            }
    
            /*
             * Create connection. Create session from connection; false means
             * session is not transacted. Create sender and text message. Send
             * messages, varying text slightly. Send end-of-messages message.
             * Finally, close connection.
             */
            try {
                //from connectionfactory I create the session and from the
                //session I create the producer
                //now my producer is ablet o send message through quotazioni
                connection = connectionFactory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                producer = session.createProducer(destination);
    
                TextMessage message = null;
                String messageType = null;
                message = session.createTextMessage();
    
                float quotazione;
                int i = 0;
                while (true) { //continuously I send message
                    i++;
                    messageType = scegliTitolo();
                    quotazione = valore();
                    //initialize the properies
                    message.setStringProperty("Nome", messageType);
                    message.setFloatProperty("Valore", quotazione);
                    //main text
                    message.setText(
                            "Item " + i + ": " + messageType + ", Valore: "
                            + quotazione);
                    //in the log I put what I send
                    LOG.info(
                        this.getClass().getName() +
                            "Invio quotazione: " + message.getText());
                    producer.send(message);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (JMSException e) {
                LOG.info("Exception occurred: " + e);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException e) {
                    }
                }
            }
        }
    }

### NotificatoreAcquisto

    public class NotificatoreAcquisto implements MessageListener {
    
        private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NotificatoreAcquisto.class);
    
        Properties properties = null;
        Context jndiContext = null;
    	private TopicConnectionFactory connectionFactory = null;
    	private TopicConnection connection = null;
    	private TopicSession session = null;
    	private Topic destination = null;
    	private TopicSubscriber subscriber = null;
    	private TopicPublisher publisher = null;
    
    	private Random randomGen = new Random();
    
    	public void start() throws NamingException, JMSException {
    
            InitialContext ctx = null;
    
            try {
                    properties = new Properties();
                    properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
                    properties.setProperty(Context.PROVIDER_URL,"tcp://localhost:61616");
                    jndiContext = new InitialContext(properties);
            } catch (NamingException e) {
                LOG.info("ERROR in JNDI: " + e.toString());
                System.exit(1);
            }
    
    		ctx = new InitialContext(properties);
    		this.connectionFactory = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");
    		this.destination = (Topic) ctx.lookup("dynamicTopics/Ordini");
    
            //here I create the two arrow from and to Ordini
    		this.connection = this.connectionFactory.createTopicConnection();
    		this.session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    		this.subscriber = this.session.createSubscriber(this.destination, null, true);
    		this.publisher = this.session.createPublisher(this.destination);
    		this.connection.start();
    
    		Logger.getLogger(this.getClass().getName()).info("In attesa di richieste di acquisto...");
    
    		subscriber.setMessageListener(this);
    	}
    
        //when I receive a message I elaborate it and send a reply
    	public void onMessage(Message mex) {
    		TextMessage message;
    		String utente = null;
    		String nome = null;
    		float prezzo;
    		int quantita;
    		boolean status = randomGen.nextFloat() < 0.5;
    		try {
                //I should know that the message is Text type, the interface doesn't tell me it
    			message = (TextMessage) mex;
    			utente = message.getStringProperty("Utente");
    			nome = message.getStringProperty("Nome");
    			prezzo = message.getFloatProperty("Prezzo");
    			quantita = message.getIntProperty("Quantita");
    		} catch (Exception e) {
    			e.printStackTrace();
    			return;
    		}
    		try {
                //I read the message in the previuos part and I respond to it
    			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    			publisher = session.createPublisher(destination);
    
    			message = session.createTextMessage();
    			message.setStringProperty("Utente", utente);
    			message.setStringProperty("Nome", nome);
    			message.setBooleanProperty("Status", status);
    			message.setIntProperty("Quantita", quantita);
    			message.setFloatProperty("Prezzo", prezzo);
    
    			Logger.getLogger(
    					this.getClass().getName()
    				).info(
    					"************************************************" + "\n" +
    					"Notifica richiesta di acquisto" + "\n" +
    					"ID utente: " + utente + "\n" +
    					"Titolo: " + nome + "\n" +
    					"Quantit\u00e0: " + quantita + "\n" +
    					"Prezzo: " + prezzo + "\n" +
    					"Accettato: " + status + "\n" +
    					"************************************************"
    				);
    
    			publisher.send(message); //I send the reply
    		} catch (Exception err) {
    			err.printStackTrace();
    		}
    	}
    }

## Client

### Main

    public class StockMarketClient {
    	public static void main(String[] args) {
                AzioniFrame a = new AzioniFrame();
    	}
    }

### CompraJMSManager

    public class CompraJMSManager extends Observable implements MessageListener {
    	
            Properties properties = null;
            Context jndiContext = null;
    	private TopicConnectionFactory connectionFactory = null;
    	private TopicConnection connection = null;
    	private TopicSession session = null;
    	private Topic destination = null;
    	private TopicSubscriber sub;
            private TopicPublisher topicPublisher;


​    
    	public CompraJMSManager(Observer osservatore) {
    		super.addObserver(osservatore);
                    System.out.println("COMPRANDO");
    		try {
    			
                        InitialContext ctx = null;
                        properties = new Properties();
    		    properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
                        properties.setProperty(Context.PROVIDER_URL,"tcp://localhost:61616");
                        jndiContext = new InitialContext(properties);        
                    
                        ctx = new InitialContext(properties);
    		    this.connectionFactory =
    			(TopicConnectionFactory) ctx.lookup("ConnectionFactory");
    		    this.destination =
    			(Topic) ctx.lookup("dynamicTopics/Ordini");
    
    		    this.connection =
    			this.connectionFactory.createTopicConnection();
    		    this.session =
    			this.connection.createTopicSession(
    					false, Session.AUTO_ACKNOWLEDGE
    				);
                     this.topicPublisher = session.createPublisher(destination);
    		} catch (NamingException err) {
    			err.printStackTrace();
    		} catch (JMSException err) {
    			err.printStackTrace();
    		} 
    	}
    	
            public boolean compra(String nome, float prezzo, int quantita) {
    		String utente = Utente.getInstance().getUtente();
    		
    		if (utente == null)
    			return false;
    		
    		try {
    			TextMessage sendMex = session.createTextMessage();
    			
    			sendMex.setStringProperty("Utente",
    					utente
    				);
    			sendMex.setStringProperty("Nome",
    					nome
    				);
    			sendMex.setFloatProperty("Prezzo",
    					prezzo
    				);
    
    			sendMex.setIntProperty("Quantita", quantita);
    			String query =
    					"Utente = '" + utente + "'" +
    					" AND " +
    					"Nome = '" + nome + "'";
    			sub = session.createSubscriber(destination, query, true);
    			sub.setMessageListener(this);
    			connection.start();
    			topicPublisher.publish(sendMex);
    		} catch (JMSException err) {
    			err.printStackTrace();
    			return false;
    		}
    		return true;
    	}
    
    	/**
    	 * Invocato quando la notifica positiva o negativa dell'acquisto giunge a
    	 * destinazione
    	 * 
    	 * @param mex
    	 *            Il messaggio di notifica.
    	 */
    	public void onMessage(Message mex) {
    		String infoMex = null;
    		try {
    			TextMessage recMex = (TextMessage) mex;
    			
    			if (recMex != null)
    				if (recMex.getBooleanProperty("Status"))
    					infoMex = "L'acquisto \u00e8 andato a buon fine";
    				else
    					infoMex = "L'acquisto non \u00e8 andato a buon fine";
    			/*
    			 * Chiude la connessione del subscriber asincrono in modo da non
    			 * ricevere altri messaggi di notifica
    			 */
    			sub.close();
    		} catch (NumberFormatException err) {
    			infoMex = "Errore nel riempimento di alcuni campi";
    		} catch (JMSException err) {
    			err.printStackTrace();
    		}
    		
    		if (infoMex != null) {
    			super.setChanged();	// rende attivo il cambiamento di stato
    			super.notifyObservers(infoMex);
    		}
    	}
    }

### AzioniJMSListener

    public class AzioniJMSListener extends Observable implements MessageListener {
    
        private TopicConnection topicConnection;
        private TopicSession topicSession = null;
        private Destination destination = null;
        private MessageProducer producer = null;
    
        public AzioniJMSListener(Observer[] osservatori) {
    
            for (Observer osservatore : osservatori) {
                System.out.println(osservatore);
                super.addObserver(osservatore);
            }
    
            Context jndiContext = null;
            ConnectionFactory topicConnectionFactory = null;
    
            String destinationName = "dynamicTopics/Quotazioni";
    
            try {
    
                Properties props = new Properties();
    
                props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
                props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
                jndiContext = new InitialContext(props);
    
                topicConnectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
                destination = (Destination) jndiContext.lookup(destinationName);
                topicConnection = (TopicConnection) topicConnectionFactory.createConnection();
                topicSession = (TopicSession) topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
                TopicSubscriber topicSubscriber
                        = topicSession.createSubscriber((Topic) destination);
    
                topicSubscriber.setMessageListener(this);
            } catch (JMSException err) {
                err.printStackTrace();
            } catch (NamingException err) {
                err.printStackTrace();
            }
        }
    
        /**
         * Chiude la ricezione dei messaggi sulla topic quotazioni
         */
        public void stop() {
            try {
                topicConnection.stop();
            } catch (JMSException err) {
                err.printStackTrace();
            }
        }
    
        /**
         * Apre la ricezione dei messaggi sulla topic quotazioni
         */
        public void start() {
            try {
                topicConnection.start();
            } catch (JMSException err) {
                err.printStackTrace();
            }
        }
    
        public void onMessage(Message mex) {
            try {
                String nome = mex.getStringProperty("Nome");
                float valore = mex.getFloatProperty("Valore");
    
                Quotazione quotazione = new Quotazione(nome, valore);
    
                super.setChanged();	// rende attivo il cambiamento di stato
                super.notifyObservers(quotazione);
            } catch (JMSException err) {
                err.printStackTrace();
            }
        }
    
    }