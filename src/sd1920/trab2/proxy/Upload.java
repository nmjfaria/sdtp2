package sd1920.trab2.proxy;
import java.io.IOException;
import java.util.Scanner;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import sd1920.trab2.proxy.arguments.CreateFolderV2Args;
import sd1920.trab2.proxy.arguments.DeleteArgs;
import sd1920.trab2.proxy.arguments.UploadArgs;

public class Upload {

	private static final String apiKey = "076d198t371jnvk";
	private static final String apiSecret = "7risfel7adp7chd";
	private static final String accessTokenStr = "EU9nEtmAz9oAAAAAAAA5aBijMp7mHxRM69s-HK-VFiXxULKN-kGy7DhMv1sstBPP";

	protected static final String JSON_CONTENT_TYPE = "application/octet-stream";
	
	private static final String OP_URL = "https://content.dropboxapi.com/2/files/upload";
	
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	private Gson json;
	
	public Upload() {
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);

		json = new Gson();
	}
	
	public boolean execute( String path, String mode, boolean autorename, boolean mute, boolean strict_conflict, Object obj ) {
		OAuthRequest operation = new OAuthRequest(Verb.POST, OP_URL);
		operation.addHeader("Content-Type", JSON_CONTENT_TYPE);
		operation.addHeader("Dropbox-API-Arg", json.toJson(new UploadArgs("/"+path, mode, autorename, mute, strict_conflict)));

		operation.setPayload(json.toJson(obj));

		service.signRequest(accessToken, operation);
		
		Response r = null;
		
		try {
			r = service.execute(operation);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if(r.getCode() == 200) {
			return true;
		} else {
			System.err.println("HTTP Error Code: " + r.getCode() + ": " + r.getMessage());
			try {
				System.err.println(r.getBody());
			} catch (IOException e) {
				System.err.println("No body in the response");
			}
			return false;
		}
		
	}
}
