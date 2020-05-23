package sd1920.trab2.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class InsecureHostnameVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String hostname, SSLSession session) {
		//Ignore the verification of hostname in the certificate
		//(This shoud not be used in production systems)
		return true;
	}

}
