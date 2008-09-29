/*
 * Copyright (c) 2007-2008, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.sgs.protocol.simple;

/**
 * SGS Protocol constants.
 * <p>
 * A protocol message is constructed as follows:
 * <ul>
 * <li> (unsigned short) payload length, not including this field
 * <li> (byte) operation code
 * <li> optional content, depending on the operation code.
 * </ul>
 * <p>
 * A {@code ByteArray} is encoded in a context dependent fashion. If the ByteArray
 * is the only content, or if the ByteArray is the last piece of content (that is,
 * if the length of the ByteArray can be determined by the payload length and
 * the length of what has come before), the ByteArray is encoded as
 * <ul>
 * <li> (byte[]) the bytes in the array
 * </ul>
 * If there is other content that follows the ByteArray, then the ByteArray 
 * is encoded as
 * <ul>
 * <li> (unsigned short) number of bytes in the array
 * <li> (byte[]) content
 * </ul>
 * <p>
 * A {@code String} is encoded as follows:
 * <ul>
 * <li> (unsigned short) number of bytes of modified UTF-8 encoded String
 * <li> (byte[]) String encoded in modified UTF-8 as described
 * in {@link java.io.DataInput}
 * </ul>
 * Note that these encodings only apply to those data items that are specified
 * explicitly in the protocol. Application data, passed as a ByteArray, may 
 * contain any information, but will need to be parsed (and, if necessary, 
 * converted to and from a network representation) by the application or 
 * client.
 * <p>
 * The total length of a message must not be greater than 65535 bytes; given the
 * header information this means that the payload of a message cannot be 
 * greater than 65532 bytes. If a message larger than this must be sent, it is
 * the responsibility of the sender to break the message into pieces and
 * of the receiver to re-assemble those pieces.
 */
public interface SimpleSgsProtocol {
    
    /**
     * The maximum length of a protocol message:
     * {@value #MAX_MESSAGE_LENGTH} bytes.
     */
    final int MAX_MESSAGE_LENGTH = 65535;

    /**
     * The maximum payload length:
     * {@value #MAX_PAYLOAD_LENGTH} bytes.
     */
    final int MAX_PAYLOAD_LENGTH = 65532;

    /** The version number, currently {@code 0x04}. */
    final byte VERSION = 0x04;

    /**
     * Login request from a client to a server. This message should only be 
     * received by a server; if received by a client it should be ignored.
     * <br>
     * Opcode: {@code 0x10}
     * <br>
     * Payload:
     * <ul>
     * <li>(byte)   protocol version
     * <li>(String) name
     * <li>(String) password
     * </ul>
     * The protocol version will be checked by the server to insure that the
     * client and server are using the same protocol; if the two do not match
     * the server will disconnect from the client. Since the protocols being used
     * are not the same, no other communication between the client and the 
     * server can be guaranteed to be understood.
     * <p>
     * The name and password strings will be passed to the server's authentication
     * mechanism. The result of attempting to login will be the sending of
     * either a {@link #LOGIN_SUCCESS}, {@link #LOGIN_FAILURE}, or 
     * {@link #LOGIN_REDIRECT} message from the server to the client.
     * <p>
     * Sending a login request on a session that is currently logged in will 
     * result in 
     */
    final byte LOGIN_REQUEST = 0x10;

    /**
     * Login success.  Server response to a client's {@link #LOGIN_REQUEST}. 
     * <br>
     * Opcode: {@code 0x11}
     * <br>
     * Payload:
     * <ul>
     * <li> (ByteArray) reconnectionKey
     * </ul>
     * The reconnectionKey is an opaque reference that can be held by the client
     * for use in case the client is disconnected and wishes to reconnect to the
     * server with the same identity using a {@link #RECONNECT_REQUEST}. 
     * <br>
     * The behavior of a server receiving this message from a client that is 
     * already logged in is determined by the property com.sun.sgs.impl.service.session.allow.new.login.
     * If the value of this property is false (which is the default value) the
     * second login request is ignored. If the value is true, the second login
     * request will cause the currently active login to be replaced by the new
     * request.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
     */
    final byte LOGIN_SUCCESS = 0x11;

    /**
     * Login failure.  Server response to a client's {@link #LOGIN_REQUEST}.
     * <br>
     * Opcode: {@code 0x12}
     * <br>
     * Payload:
     * <ul>
     * <li> (String) reason
     * </ul>
     * This message indicates a failure of the login process initiated by a 
     * {@link #LOGIN_REQUEST}. The reason for the failure is encoded in the returned
     * string; the interpretation of the reason is application specific.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
     */
    final byte LOGIN_FAILURE = 0x12;

    /**
     * Login redirect.  Server response to a client's {@link #LOGIN_REQUEST}.
     * <br>
     * Opcode: {@code 0x13}
     * <br>
     * Payload:
     * <ul>
     * <li> (String) hostname
     * <li> (int) port
     * </ul>
     * This message indicates a redirection from the 
     * machine to which the {@link #LOGIN_REQUEST} was sent to another machine.
     * The client receiving this request should shut down the connection to the
     * original machine and establish a connection to the redirection machine, 
     * indicated by the hostname and port in the payload. The client should 
     * then attempt to log in to the machine to which it has been redirected
     * by sending a {@link #LOGIN_REQUEST} to that machine.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
     */
    final byte LOGIN_REDIRECT = 0x13;

    /**
     * Reconnection request.  Client requesting reconnect to a server.
     * <br>
     * Opcode: {@code 0x20}
     * <br>
     * Payload:
     * <ul>
     * <li> (byte)      protocol version
     * <li> (ByteArray) reconnectionKey
     * </ul>
     * This message requests that the client be reconnected to an existing 
     * login session with the server. The reconnectionKey must be the same that
     * was sent to the client by the server as part of the {@link #LOGIN_SUCCESS}
     * for the session to which reconnection is requested. Support for this message,
     * and the length of time after a disconnect that a server will allow 
     * reconnect, may vary from server to server. If the message is received by
     * the server when the server state indicates that the client is currently 
     * logged in with a valid session, the server will respond with a {@link 
     * #RECONNECT_SUCCESS} message, and will continue with the current client
     * session.
     * <br>
     * <b>Note: this message is not currently supported by the Project Darkstar
     * Server</b>
     */
    final byte RECONNECT_REQUEST = 0x20;

    /**
     * Reconnect success.  Server response to a client's
     * {@link #RECONNECT_REQUEST}.
     * <br>
     * Opcode: {@code 0x21}
     * <br>
     * Payload:
     * <ul>
     * <li> (ByteArray) reconnectionKey
     * </ul>
     * Indicates that a {@link #RECONNECT_REQUEST} has been successful. The 
     * message will include a reconnect key that can be used in subsequent 
     * reconnect requests from the client. Reciept of this message indicates that
     * the client session has been re-established in the state it was in when the
     * client was disconnected from the server.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
     * <br>
     * <b>Note: this message is not currently supported by the Project Darkstar
     * Server</b>
     */
    final byte RECONNECT_SUCCESS = 0x21;

    /**
     * Reconnect failure.  Server response to a client's
     * {@link #RECONNECT_REQUEST}.
     * <br>
     * Opcode: {@code 0x22}
     * <br>
     * Payload:
     * <ul>
     * <li> (String) reason
     * </ul>
     * This response indicates that a reconnect request could not be honored by
     * the server. This could have been because of an invalid reconnect key, or 
     * because too much time had elapsed between the client disconnection and 
     * the reconnect request (which, in turn, caused the server to discard
     * the client state). The string returned details the reason for the
     * denial of reconnection.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
     * <br>
     * <b>Note: this message is not currently supported by the Project Darkstar
     * Server</b>
     */
    final byte RECONNECT_FAILURE = 0x22;

    /**
     * Session message.  May be sent by the client or the server.
     * Maximum length is {@value #MAX_PAYLOAD_LENGTH} bytes.
     * Larger messages require fragmentation and reassembly above
     * this protocol layer.
     * <br>
     * Opcode: {@code 0x30}
     * <br>
     * Payload:
     * <ul>
     * <li> (ByteArray) message
     * </ul>
     * This message allows information to be sent between the client and the
     * server. The content of the message is application dependent, and the
     * mechanisms for constructing and parsing these messages is an application-level
     * task. 
     * <br>
     * If this message is sent by a client to a server and the client is not
     * currently logged in to the server, the message will be ignored. 
     */
    final byte SESSION_MESSAGE = 0x30;


    /**
     * Logout request from a client to a server.
     * <br>
     * Opcode: {@code 0x40}
     * <br>
     * No payload.
     * <br>
     * This message will cause the client to be logged out of the server, and
     * the connection to be closed. Membership in any channels by the client will 
     * be dropped. Any message (other than {@link #LOGIN_REQUEST} sent by the
     * client after sending this message will be ignored, and any message will 
     * need to be sent on a new connection to the server.
     */
    final byte LOGOUT_REQUEST = 0x40;

    /**
     * Logout success.  Server response to a client's {@link #LOGOUT_REQUEST}.
     * <br>
     * Opcode: {@code 0x41}
     * <br>
     * No payload.
      * <br>
     * This message is sent from the server to the client to indicate that a
     * {@link #LOGOUT_REQUEST} has been received and that the client has been
     * logged out of the current session. On receipt of this message, the client
     * should shut down any networking resources that are used to communicate
     * with the server.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
    */
    final byte LOGOUT_SUCCESS = 0x41;


    /**
     * Channel join.  Server notifying a client that it has joined a channel.
     * <br>
     * Opcode: {@code 0x50}
     * <br>
     * Payload:
     * <ul>
     * <li> (String) channel name
     * <li> (ByteArray) channel ID
     * </ul>
     * This message is sent from the server to the client to indicate that the 
     * client has been added to the channel identified by the name and channel ID
     * contained in the message. The client should establish a way of receiving 
     * messages that will be sent on the channel.
     * <br>
     * This message should only be received by a client; if received by a server 
     * the client sending the message will be disconnected.
     */
    final byte CHANNEL_JOIN = 0x50;

    /**
     * Channel leave.  Server notifying a client that the client has left a channel.
     * <br>
     * Opcode: {@code 0x51}
     * <br>
     * Payload:
     * <ul>
     * <li> (ByteArray) channel ID
     * </ul>
     * This message is sent from the server to the client indicating to the client
     * that it has been removed from the channel with the indicated channel ID.
     * The client can no longer send messages on the channel, and any objects
     * that are currently listening for messages on the channel can be removed.
     */
    final byte CHANNEL_LEAVE = 0x51;
    
    /**
     * Channel message.  May be sent by the client or the server.
     * Maximum length is {@value #MAX_PAYLOAD_LENGTH} bytes minus the channel ID
     * size plus two bytes (the size of the unsigned short indicating the 
     * channel Id size).
     * Larger messages require fragmentation and reassembly above
     * this protocol layer.
     * <br>
     * Opcode: {@code 0x52}
     * <br>
     * Payload:
     * <ul>
     * <li> (unsigned short) channel ID size
     * <li> (ByteArray) channel ID
     * <li> (ByteArray) message
     * </ul>
     * This message will initiate the channel sending logic on the server, 
     * requesting that the indicated message content be sent to all of the 
     * members of the indicated channel. If the client sending the request
     * is not a member of the channel, the message will be rejected by the server.
     * The server may also refuse to send the message, or alter the message, 
     * because of application-specific logic.
     */
    final byte CHANNEL_MESSAGE = 0x52;
}
