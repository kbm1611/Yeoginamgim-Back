package com.yeginamgim.auth.service;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class SmtpRecipientProbe {
    private static final int SMTP_PORT = 25;
    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int READ_TIMEOUT_MILLIS = 3000;
    private static final String HELO_DOMAIN = "localhost";

    public boolean acceptsRecipient(String mailServer, String email) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(mailServer, SMTP_PORT), CONNECT_TIMEOUT_MILLIS);
            socket.setSoTimeout(READ_TIMEOUT_MILLIS);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(),
                    StandardCharsets.US_ASCII
            ));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(),
                    StandardCharsets.US_ASCII
            ));

            if (!isPositiveCompletion(readReply(reader))) {
                return false;
            }
            if (!sendCommand(writer, reader, "HELO " + HELO_DOMAIN)) {
                return false;
            }
            if (!sendCommand(writer, reader, "MAIL FROM:<>")) {
                return false;
            }

            int rcptReply = sendCommandAndReadReply(writer, reader, "RCPT TO:<" + email + ">");
            sendCommandAndReadReply(writer, reader, "QUIT");

            return rcptReply == 250 || rcptReply == 251;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        return isPositiveCompletion(sendCommandAndReadReply(writer, reader, command));
    }

    private int sendCommandAndReadReply(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();
        return readReply(reader);
    }

    private int readReply(BufferedReader reader) throws IOException {
        String line;
        int replyCode = -1;
        do {
            line = reader.readLine();
            if (line == null || line.length() < 3) {
                return -1;
            }

            replyCode = parseReplyCode(line);
        } while (line.length() > 3 && line.charAt(3) == '-');

        return replyCode;
    }

    private int parseReplyCode(String line) {
        try {
            return Integer.parseInt(line.substring(0, 3));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isPositiveCompletion(int replyCode) {
        return replyCode >= 200 && replyCode < 300;
    }
}
