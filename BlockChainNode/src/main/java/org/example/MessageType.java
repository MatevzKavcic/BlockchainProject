package org.example;

public enum MessageType {
    HANDSHAKE,
    HANDSHAKEKEYRETURN,

    PEERLIST,

    PEERLISTRETURN,

    BLOCKCHAINREQUEST,

    BLOCKCHAINRESPONSE,
    BLOCKCHAINSEND,
    BLOCKCHAINITIALIZE,
    UTXOPOOLINITIALIZATION, TRANSACTION, REQUESTTRANSPOOL, REQUESTUTXOPOOL, RESPONSETRANSPOOL, RESPONSEUTXOPOOL,BLOCK, BLOCKERROR, MISSING_BLOCKS,


    }