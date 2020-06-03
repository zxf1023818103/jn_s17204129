package s17204129;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;

public class HttpsServer extends HttpServer {

    public HttpsServer() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        super();
        KeyStore keyStore = KeyStore.getInstance(System.getProperty("keystoreType", "jks"));
        keyStore.load(new FileInputStream(System.getProperty("keystoreFile")), System.getProperty("keystorePass").toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, System.getProperty("keystorePass").toCharArray());
        SSLContext context = SSLContext.getInstance(System.getProperty("sslProtocol", "TLSv1.2"));
        context.init(keyManagerFactory.getKeyManagers(), null, null);
        SSLServerSocketFactory serverSocketFactory = context.getServerSocketFactory();
        ServerSocket serverSocket = serverSocketFactory.createServerSocket(Integer.parseInt(System.getProperty("sslPort", "443")));
        setServerSocket(serverSocket);
    }


}
