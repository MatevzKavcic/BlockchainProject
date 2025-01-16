package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import util.LogLevel;
import util.Logger;

import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingService extends Thread {
    private final BlockingQueue<String> messageQueue;
    private ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers;

    public Blockchain blockchain;

    private UTXOPool utxoPool;

    private TransactionManager transactionManager;
    private TransactionPool transactionPool;
    MiningCoordinator miningCoordinator;
    public MessagingService(BlockingQueue<String> messageQueue, ConcurrentHashMap<PublicKey, PeerInfo> connectedPeers, String hostName, int portNumber, PublicKey publicKey, PrivateKey privateKey, Blockchain blockchain, UTXOPool utxoPool, TransactionManager transactionManager, MiningCoordinator miningCoordinator) {
        this.messageQueue = messageQueue;
        this.connectedPeers = connectedPeers;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.blockchain  = blockchain;
        this.utxoPool = UTXOPool.getInstance();  // Ensure singleton instance of UTXO pool
        this.transactionManager = transactionManager;
        this.transactionPool = TransactionPool.getInstance();  // Use the singleton instance
        this.miningCoordinator = miningCoordinator;
        this.setName("Messaging service Thread");
    }


    String hostName;
    int portNumber;

    private PublicKey publicKey;

    private PrivateKey privateKey;


    Gson gson = new Gson();

    private final Queue<Message> pendingBlockchainMessages = new LinkedList<>();

    @Override
    public void run() {
        try {

            // bussy waiting for someone to put something in the queue. when that happens it proceses it.
            while (true) {
                // Wait for a message to process
                String message = messageQueue.take();
                Type type = new TypeToken<ArrayList<Integer>>() {}.getType();

                Message messageObject = gson.fromJson(message,Message.class);

                //thre is an option that block comes before the blockchain and you need to handle that
                if (Blockchain.getInstance()==null && messageObject.getHeader()== MessageType.BLOCK){
                    Logger.log("BLOCCKERCOCKER ga ni tle.",LogLevel.Error);
                    pendingBlockchainMessages.add(messageObject);
                    continue;
                }
                if (Blockchain.getInstance()!=null&& !pendingBlockchainMessages.isEmpty()){
                    handleMessageBlockQueue();
                }

                PublicKey sender  = stringToPublicKey(messageObject.getPublicKey()) ;
                String senderName = generateNameFromPublicKey(messageObject.getPublicKey());
                switch (messageObject.getHeader()) {
                    case HANDSHAKE -> {
                    }
                    case HANDSHAKEKEYRETURN -> {

                    }
                    case PEERLIST -> {
                        ArrayList<Integer> arrayListOfPorts = gson.fromJson(messageObject.getBody(), type);

                        // in this case i need to connnect to all peers in the list
                        Logger.log("i recived this array of ports i neeed to connect to : " + arrayListOfPorts, LogLevel.Info);

                        for (Integer connectToPort : arrayListOfPorts) {
                            //Is special variable has to be true so the servir will get the correct header.
                            int temp = 6000;
                            temp = connectToPort-temp;
                            String tmp = String.valueOf(temp);
                            String povezi = "blockchainproject-normalnode"+temp+"-1";


                            Logger.log("I AM CONNECTING TO PORT " + connectToPort + " on hostname ? --> " +povezi);
                            Client novaPovezava = new Client(povezi,portNumber,messageQueue,connectedPeers,publicKey,privateKey,connectToPort,true);
                            //Client novaPovezava = new Client(hostName,portNumber,messageQueue,connectedPeers,publicKey,privateKey,connectToPort,true);
                            novaPovezava.start();


                        }
                    }
                    case PEERLISTRETURN -> {
                    }
                    case BLOCKCHAINREQUEST -> {
                        Logger.log("recived REQUEST BLOCKCHAIN message from : "+ senderName, LogLevel.Status);

                        PeerInfo peerInfo = connectedPeers.get(sender);

                        WriteMeThread thread = (WriteMeThread) peerInfo.getThread();
                        String pkString = publicKeyToString(publicKey);
                        Message m = new Message(MessageType.BLOCKCHAINRESPONSE,gson.toJson(blockchain), pkString);
                        String mString = gson.toJson(m);
                        thread.sendMessage( mString);
                    }
                    case BLOCKCHAINRESPONSE -> {
                        Logger.log("recived RESPONSE BLOCKCHAIN message from : "+ senderName, LogLevel.Status);
                        // I got the BLockchain from a peer now i want to update it localy.
                        Blockchain.setInstance(gson.fromJson(messageObject.getBody(), Blockchain.class));
                        synchronized (SharedResources.LOCK) {
                            notifyUpdates();
                        }
                    }

                    case UTXOPOOLINITIALIZATION -> {
                        utxoPool= gson.fromJson(messageObject.getBody(), UTXOPool.class);

                        Logger.log(blockchain.getUTXOPool().toString() ,LogLevel.Warn);

                    }

                    case TRANSACTION -> {
                        Logger.log("RECIEVED A NEW TRANSACTION FROM : "+ senderName,LogLevel.Success);

                        if (utxoPool==null){
                            Logger.log("UTXOpool is null, cannot continue");
                        }

                        Transaction transaction = gson.fromJson(messageObject.getBody(),Transaction.class);
                        transactionManager.validateNewTransaction(transaction);

                    }
                    case REQUESTTRANSPOOL -> {
                        Logger.log("recived REQTRANSPOOL message from : "+ senderName, LogLevel.Status);
                        transactionManager.sendTransactionPool(sender);
                    }
                    case RESPONSETRANSPOOL ->{
                        Logger.log("recived RESPONSE TRANSPOOL message from : "+ senderName, LogLevel.Status);
                        transactionManager.updateTransactionPool(messageObject.getBody());
                    }
                    case REQUESTUTXOPOOL -> {
                        Logger.log("recived REQUESTUTXOPOOL message from : "+ senderName, LogLevel.Status);
                        transactionManager.sendUTXOPool(sender);
                    }
                    case RESPONSEUTXOPOOL->{
                        Logger.log("recived RESPONSE UTXOPOOL message from : "+ senderName+ messageObject.getBody(), LogLevel.Status);
                        transactionManager.updateUTXOPool(messageObject.getBody());
                        }
                    case BLOCK -> {
                        handleNewBlock(senderName,messageObject);

                    }
                    case BLOCKERROR -> {
                        Logger.log("Recieved a BLOCKERROR message  :" +messageObject.getBody()+" from -->" + generateNameFromPublicKey(messageObject.getPublicKey()));

                        handleBlockchainLengthMessage(messageObject);
                    }
                    case MISSING_BLOCKS -> {
                        handleMissingBlocksMessage(messageObject);
                    }
                }
            }
        } catch (Exception e) {
            Logger.log("Failed to parse JSON message: " + e.getMessage(), LogLevel.Error);
            Logger.log("failed inside MESSAGING SERVICE");
            Logger.log("sending my status to all ", LogLevel.Error);

            for (Map.Entry<PublicKey, PeerInfo> entry : connectedPeers.entrySet()) {
                PublicKey publicKeyOf = entry.getKey();
                PeerInfo peerInfo = entry.getValue();

                WriteMeThread thread = (WriteMeThread) peerInfo.getThread();
                String pkString = publicKeyToString(publicKey);
                int blockchainLenght = Blockchain.getInstance().getChain().size();
                String blockchainLenghtString = Integer.toString(blockchainLenght);

                Message m = new Message(MessageType.BLOCKERROR,
                        blockchainLenghtString,
                        pkString);

                String mString = gson.toJson(m);
                thread.sendMessage( mString);
                Logger.log("Sent a message :"+ m.getHeader() + " --- " +m.getBody()+" --- to -->" + generateNameFromPublicKey(publicKeyToString(publicKeyOf)));
            }


        }
    }
    private void handleMissingBlocksMessage(Message message) {
        try {
            // Deserialize the received blocks

            Logger.log("got the missingBLock HEADER and a lis tof blocks");
            Type blockListType = new TypeToken<List<Block>>() {}.getType();
            List<Block> receivedBlocks = gson.fromJson(message.getBody(), blockListType);

            // Add the received blocks to the blockchain
            Blockchain blockchain = Blockchain.getInstance();
            Logger.log("Trying to add blocks i got --->:" + receivedBlocks);

            for (Block block : receivedBlocks) {
                Logger.log("Trying to add block on lenght:" + block.getIndex());

                blockchain.handleAddBlockForksAndSpoons(block);
            }

            Logger.log("Successfully added " + receivedBlocks.size() + " blocks to the blockchain.", LogLevel.Info);
        } catch (Exception e) {
            Logger.log("Failed to process missing blocks message: " + e.getMessage(), LogLevel.Error);
        }
    }

    public void handleBlockchainLengthMessage(Message message) {
        try {
            // Extract the blockchain length from the message body
            int peerBlockchainLength = Integer.parseInt(message.getBody());

            // Get the current blockchain instance
            Blockchain blockchain = Blockchain.getInstance();
            int localBlockchainLength = blockchain.getChain().size();

            PublicKey senderKey = stringToPublicKey(message.getPublicKey()); // od message public key je recipient k njemu posljes

            // Check if the peer is missing blocks
            if (localBlockchainLength > peerBlockchainLength) {
                // Send the missing blocks
                sendMissingBlocks(senderKey, peerBlockchainLength);
            } else {
                Logger.log("Peer's blockchain is up to date or ahead. No blocks to send.", LogLevel.Info);
            }
        } catch (Exception e) {
            Logger.log("Failed to handle blockchain length message: " + e.getMessage(), LogLevel.Error);
        }
    }
    private void sendMissingBlocks(PublicKey recipientKey, int startIndex) {
        Blockchain blockchain = Blockchain.getInstance();

        // Check if the start index is valid
        if (startIndex < 0 || startIndex >= blockchain.getChain().size()) {
            Logger.log("Invalid start index for sending missing blocks.", LogLevel.Error);
            return;
        }

        // Collect the missing blocks
        List<Block> missingBlocks = blockchain.getChain().subList(startIndex, blockchain.getChain().size());

        // Serialize the blocks into JSON format
        String serializedBlocks = gson.toJson(missingBlocks);
        Logger.log("AN UNIMPORTANT LOG I NEED TO SE IF IT INSIDE MESSAGE SERVICE sendMissingBlocks");


        // Create a new message with the blocks
        Message blockMessage = new Message(
                MessageType.MISSING_BLOCKS,
                serializedBlocks,
                publicKeyToString(publicKey)
        );

        // Send the message to the recipient
        PeerInfo recipient = connectedPeers.get(recipientKey);
        if (recipient != null) {

            WriteMeThread thread = (WriteMeThread) recipient.getThread();
            thread.sendMessage(gson.toJson(blockMessage));
            Logger.log("Sent " + missingBlocks.size() + " missing blocks to peer.", LogLevel.Info);
        } else {
            Logger.log("Recipient not found in connected peers.", LogLevel.Error);
        }
    }




    public void handleNewBlock(String senderName,Message messageObject ){
        //handle multiple cases... poglej blockchain in ce je use kul ga dodaj ....
        miningCoordinator.interruptMining();
        blockchain = Blockchain.getInstance();

        Block recievedBlock = gson.fromJson(messageObject.getBody(),Block.class);
        Logger.log("RECIVED A NEW BLOCK from : " + senderName+" on index -> "+recievedBlock.getIndex(), LogLevel.Info);

        //blockchain method for handling new blocks comming and forking and stuff.
        blockchain.handleAddBlockForksAndSpoons(recievedBlock);
        //when you handle the block reset the miner so he can mine again.
        miningCoordinator.resetMiningFlag();
    }

    public void handleMessageBlockQueue( ){
        //handle multiple cases... poglej blockchain in ce je use kul ga dodaj ....
        miningCoordinator.interruptMining();
        blockchain = Blockchain.getInstance();

        while(!pendingBlockchainMessages.isEmpty()){
            Message blockMessage= pendingBlockchainMessages.poll();
            Block block = gson.fromJson(blockMessage.getBody(),Block.class);
            blockchain.handleAddBlockForksAndSpoons(block);
        }
        //when you handle the blocks from the sidequeue reset the miner so he can mine again.
        miningCoordinator.resetMiningFlag();
    }

    public PublicKey stringToPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public static String generateNameFromPublicKey(String publicKey) {
        // Generate a UUID based on the public key hash
        UUID uuid = UUID.nameUUIDFromBytes(publicKey.getBytes());
        return uuid.toString().split("-")[0]; // Use the first part for brevity
    }

    public synchronized void notifyUpdates() {
        synchronized (SharedResources.LOCK) {
            SharedResources.LOCK.notifyAll(); // Notify all waiting threads
            Logger.log("Updating threads");
        }

    }
}