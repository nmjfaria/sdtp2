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

public class CreateDirectory {

	private static final String apiKey = "076d198t371jnvk";
	private static final String apiSecret = "7risfel7adp7chd";
	private static final String accessTokenStr = "EU9nEtmAz9oAAAAAAAA5aBijMp7mHxRM69s-HK-VFiXxULKN-kGy7DhMv1sstBPP";

	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
	
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	private Gson json;
	
	public CreateDirectory() {
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);

		json = new Gson();
	}
	
	public boolean execute( String directoryName ) {
		OAuthRequest createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
		createFolder.addHeader("Content-Type", JSON_CONTENT_TYPE);

		createFolder.setPayload(json.toJson(new CreateFolderV2Args("/"+directoryName, false)));

		service.signRequest(accessToken, createFolder);
		
		Response r = null;
		
		try {
			r = service.execute(createFolder);
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
