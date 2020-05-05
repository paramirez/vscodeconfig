package com.kiwidev.paramirez;

import com.infinit.exception.InfinItException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Properties;

public class SftpClient {
    
    private final static Logger logger = LoggerFactory.getLogger(SftpClient.class);
    
    private String server;
    private int port;
    private String login;
    private String password;
    
    private JSch jsch = null;
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp c = null;
    
    public SftpClient(String server, int port, String login, String password) {
        this.server = server;
        this.port = port;
        this.login = login;
        this.password = password;
    }

    public void connect() {
        try {
            logger.debug("Initializing jsch");
            jsch = new JSch();
            session = jsch.getSession(login, server, port);

            // Java 6 version
            session.setPassword(password.getBytes(Charset.forName("ISO-8859-1")));
            
            // Java 5 version
            // session.setPassword(password.getBytes("ISO-8859-1"));

            logger.debug("Jsch set to StrictHostKeyChecking=no");
            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            logger.info("Connecting to " + server + ":" + port);
            session.connect();
            logger.info("Connected !");

            // Initializing a channel
            logger.debug("Opening a channel ...");
            channel = session.openChannel("sftp");
            channel.connect();
            c = (ChannelSftp) channel;
            logger.debug("Channel sftp opened");


        } catch (JSchException e) {
            logger.error("", e);
        }
    }

    public void uploadFile(String sourceFile, String destinationFile) throws InfinItException {
        if (c == null || session == null || !session.isConnected() || !c.isConnected()) {
            throw new InfinItException("Connection to server is closed. Open it first.");
        }
        
        try {
            logger.debug("Uploading file to server");
            c.put(sourceFile, destinationFile);
            logger.info("Upload successfull.");
        } catch (SftpException e) {
            throw new InfinItException(e);
        }
    }

    /**
     * Recupera un archivo del servidor sftp
     * @param destinationFile String ruta al archivo remoto en el servidor
     * @param sourceFile String ruta en el sistema de archivos local
     * @throws InfinItException si la conexión y el canal no están disponibles o si se produce un error durante la descarga.
     */
    public void retrieveFile(String sourceFile, String destinationFile) throws InfinItException {
        if (c == null || session == null || !session.isConnected() || !c.isConnected()) {
            throw new InfinItException("Connection to server is closed. Open it first.");
        }

        try {
            logger.debug("Downloading file to server");
            c.get(sourceFile, destinationFile);
            logger.info("Download successfull.");
        } catch (SftpException e) {
            throw new InfinItException(e.getMessage(), e);
        }
    }
    
    public void disconnect() {
        if (c != null) {
            logger.debug("Disconnecting sftp channel");
            c.disconnect();
        }
        if (channel != null) {
            logger.debug("Disconnecting channel");
            channel.disconnect();
        }
        if (session != null) {
            logger.debug("Disconnecting session");
            session.disconnect();
        }
    }
    
    public static void main(String[] args) {
        SftpClient client = new SftpClient();
        client.setServer("localhost");
        client.setPort(22);
        client.setLogin("test");
        client.setPassword("testPassword");

        client.connect();

        try {
            client.uploadFile("src/main/resources/upload.txt", "/uploaded.txt");

            client.retrieveFile("/uploaded.txt", "target/downloaded.txt");
        } catch (InfinItException e) {
            logger.error("", e);
        } finally {
            client.disconnect();
        }
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SftpClient() {}
}
