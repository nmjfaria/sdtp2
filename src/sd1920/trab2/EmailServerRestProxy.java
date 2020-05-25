package sd1920.trab2;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd1920.trab2.impl.MessageResourceProxy;
import sd1920.trab2.impl.UserResourceProxy;
import sd1920.trab2.proxy.CreateDirectory;
import sd1920.trab2.proxy.Delete;
import sd1920.trab2.util.InsecureHostnameVerifier;

public class EmailServerRestProxy {

    private static Logger Log = Logger.getLogger(EmailServerRestProxy.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8080;

    public static void main(String[] args) throws UnknownHostException {
    	//ARG 0 = true or false of clean folder
    	boolean cleanState = Boolean.parseBoolean(args[0]);
    	String internalSecret = args[1];
        InetAddress localHost = InetAddress.getLocalHost();
        String ip = localHost.getHostAddress();
        String domain = localHost.getHostName();

        URI serverURI = URI.create(String.format("https://%s:%s/rest", ip, PORT));

        //This will allow client code executed by this process to ignore hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
        
        ResourceConfig config = new ResourceConfig();
        config.register(new MessageResourceProxy(domain, serverURI, ByteBuffer.wrap(localHost.getAddress()).getInt(), internalSecret));
        config.register(new UserResourceProxy(domain, serverURI, internalSecret));

        try {
        	JdkHttpServerFactory.createHttpServer(serverURI, config, SSLContext.getDefault());	
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Invalid SSLL/TLS configuration.");
			e.printStackTrace();
			System.exit(1);
		}
        
        //CREATE FOLDER ON DROPBOX
        System.out.println("CLEAN STATE: " + cleanState);
        if(cleanState)
        {
        	//DELETE OLDER FOLDER
        	Delete pd = new Delete();
            boolean success = pd.execute(domain);
            if(success)
            	Log.info(String.format("Folder domain %s deleted", domain));
            
            else
            	Log.info(String.format("Folder NOT domain %s deleted", domain));
            
        	//CREATE AGAIN
        	CreateDirectory cd = new CreateDirectory();
            success = cd.execute(domain);
            if(success)
            	Log.info(String.format("%s Folder created", domain));
            else
            	Log.info(String.format("%s Folder NOT created", domain));
        }
        
        
        
        Discovery.startAnnounce(domain, serverURI);
        Discovery.startDiscovery();
        
    }

}
