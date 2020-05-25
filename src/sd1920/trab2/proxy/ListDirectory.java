package sd1920.trab2.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import sd1920.trab2.proxy.arguments.ListFolderArgs;
import sd1920.trab2.proxy.arguments.ListFolderContinueArgs;
import sd1920.trab2.proxy.replies.ListFolderReturn;
import sd1920.trab2.proxy.replies.ListFolderReturn.FolderEntry;

public class ListDirectory {

	private static final String apiKey = "076d198t371jnvk";
	private static final String apiSecret = "7risfel7adp7chd";
	private static final String accessTokenStr = "EU9nEtmAz9oAAAAAAAA5aBijMp7mHxRM69s-HK-VFiXxULKN-kGy7DhMv1sstBPP";
	
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private static final String LIST_FOLDER_URL = "https://api.dropboxapi.com/2/files/list_folder";
	private static final String LIST_FOLDER_CONTINUE_URL = "https://api.dropboxapi.com/2/files/list_folder/continue";
	
	
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	
	public ListDirectory() {
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);
		
		json = new Gson();
	}
	
	public List<Long> execute(String directoryName) {
		List<Long> directoryContents = new ArrayList<Long>();
		
		OAuthRequest listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_URL);
		listDirectory.addHeader("Content-Type", JSON_CONTENT_TYPE);
		listDirectory.setPayload(json.toJson(new ListFolderArgs("/"+directoryName, false)));
		
		service.signRequest(accessToken, listDirectory);
		
		Response r = null;
		
		try {
			while(true) {

				r = service.execute(listDirectory);
				
				if(r.getCode() != 200) {
					System.err.println("Failed to list directory '" + directoryName + "'. Status " + r.getCode() + ": " + r.getMessage());
					System.err.println(r.getBody());

					return null;
				}
				
				ListFolderReturn reply = json.fromJson(r.getBody(), ListFolderReturn.class);
				
				for(FolderEntry e: reply.getEntries()) {
					String name = e.toString();
					long mid = Long.parseLong(name.substring(0, name.indexOf(".")));
					directoryContents.add(mid);
				}
				
				if(reply.has_more()) {
					//There are more elements to read, prepare a new request (now a continuation)
					listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_CONTINUE_URL);
					listDirectory.addHeader("Content-Type", JSON_CONTENT_TYPE);
					//In this case the arguments is just an object containing the cursor that was returned in the previous reply.
					listDirectory.setPayload(json.toJson(new ListFolderContinueArgs(reply.getCursor())));
					service.signRequest(accessToken, listDirectory);
				} else {
					break; //There are no more elements to read. Operation can terminate.
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
		return directoryContents;
	}
}


