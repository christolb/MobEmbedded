package edu.ucsb.mobemb.mars.CloudCommunication;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucsb.mobemb.mars.Global;

//See the Vuforia Web Services Developer API Specification - https://developer.vuforia.com/resources/dev-guide/updating-target-cloud-database

public class UpdateTarget {

	//Server Keys

    //Server Keys
    private String accessKey = Global.ServerAccessKey;
    private String secretKey = Global.ServerSecretKey;
		
	private String targetId = Global.targetID;
	private String url = "https://vws.vuforia.com";

    private String targetName;
    private String imageLocation;

    public UpdateTarget(String targName, String imgLocation)
    {
        targetName = targName;
        imageLocation = imgLocation; // "/storage/sdcard0/Download/UCSB_largemap.jpg";//imgLocation;
    }

	public void updateTarget(String metadata) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
		HttpPut putRequest = new HttpPut();
		HttpClient client = new DefaultHttpClient();
		putRequest.setURI(new URI(url + "/targets/" + targetId));
		JSONObject requestBody = new JSONObject();
		
		setRequestBody(requestBody,metadata);
		putRequest.setEntity(new StringEntity(requestBody.toString()));
		setHeaders(putRequest); // Must be done after setting the body
		
		HttpResponse response = client.execute(putRequest);
		System.out.println(EntityUtils.toString(response.getEntity()));
	}
	
	private void setRequestBody(JSONObject requestBody, String metadata) throws IOException, JSONException {
        File imageFile = new File(imageLocation);
        if(!imageFile.exists()) {
            Log.e("GP", "File location does not exist!");
            return;
        }
        byte[] image = FileUtils.readFileToByteArray(imageFile);
        requestBody.put("name", targetName); // Mandatory
        requestBody.put("width", 320.0); // Mandatory
        //requestBody.put("image", Base64.encodeBase64String(image)); // Mandatory
        requestBody.put("image", new String(Base64.encodeBase64(image))); // Mandatory
        requestBody.put("active_flag", 1); // Optional



		//requestBody.put("active_flag", true); // Optional
		//requestBody.put("application_metadata", Base64.encodeBase64String(metadata.getBytes())); // Optional
        requestBody.put("application_metadata", new String(Base64.encodeBase64(metadata.getBytes())));
    }
	
	private void setHeaders(HttpUriRequest request) {
		SignatureBuilder sb = new SignatureBuilder();
		request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
		request.setHeader(new BasicHeader("Content-Type", "application/json"));
		request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
	}
//
//	public static void main(String[] args) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
//		UpdateTarget u = new UpdateTarget();
//		u.updateTarget();
//	}
}
