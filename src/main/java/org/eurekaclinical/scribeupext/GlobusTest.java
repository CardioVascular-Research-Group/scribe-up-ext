package org.eurekaclinical.scribeupext;

/*
 * #%L
 * Eureka! Clinical ScribeUP Extensions
 * %%
 * Copyright (C) 2017 The Johns Hopkins University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.io.IOException;

import com.github.scribejava.core.model.OAuth2AccessToken;

import org.eurekaclinical.scribeupext.provider.GlobusProvider;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.exception.HttpException;

/**
* 
* This is an example of how to get the Globus OAuth to work in the version put in place on 8/9/2017.
* It is derived from examples found in the ScribeJava apis that can be found in GitHub
* (https://github.com/scribejava/scribejava).  Using the example, you copy the authorization URL created
* by the code below and paste it in your web browser.  If you're not logged into Globus, it will prompt 
* you to sign in.  Once signed in, you will be prompted to allow the scope(s) you've designated to be accessed
* by the application.  Then the browser will come back with the call back URL you've specified and the secret 
* and token code.  You paste those values into the prompt and voila, the tool should dispatch the access token
* and retrieve the values that you've specified in the scope(s).
*
* The libraries needed for this tool include scribe-up, scribejava-core and scribejava-api.  All are specified 
* in the POM.
*
* @author Stephen Granite
* 
*/
public class GlobusTest {

	private static final String NETWORK_NAME = "Globus";

	public GlobusTest() {

	}

	public static void main(String... args) throws IOException, InterruptedException, ExecutionException {
		// Replace these with your client id and secret
		final String clientId = "<your client id for your App>";
		final String clientSecret = "<your client secret for your App>";
		final String scope = "<your scope for the App>";
		final String secretState = "secret" + new Random().nextInt(999_999);
		final String callBackUrl = "<your callback URL>";
		final Scanner in = new Scanner(System.in, "UTF-8");
		GlobusProvider provider = new GlobusProvider();
		provider.setKey(clientId);
		provider.setSecret(clientSecret);
		provider.setScope(scope);
		provider.setState(secretState);
		provider.setCallBackUrl(callBackUrl);
		String body = null;

		try {

			System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
			System.out.println();

			// Obtain the Authorization URL
			System.out.println("Fetching the Authorization URL...");
			final String authorizationUrl = provider.getService().getAuthorizationUrl();
			System.out.println("Got the Authorization URL!");
			System.out.println("Now go and authorize ScribeJava here:");
			System.out.println(authorizationUrl);
			System.out.println("And paste the authorization code here");
			System.out.print(">>");
			final String code = in.nextLine();
			System.out.println();

			System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.");
			System.out.print(">>");
			final String value = in.nextLine();
			if (secretState.equals(value)) {
				System.out.println("State value does match!");
			} else {
				System.out.println("Ooops, state value does not match!");
				System.out.println("Expected = " + secretState);
				System.out.println("Got      = " + value);
				System.out.println();
			}

			// Trade the Request Token and Verifier for the Access Token
			System.out.println("Trading the Request Token for an Access Token...");

			final OAuth2AccessToken accessToken = provider.getService().getAccessToken(code);
			System.out.println("Got the Access Token!");

			// Now let's go and ask for a protected resource!
			System.out.println("Now we're going to access a protected resource...");
			body = provider.sendRequestForData(accessToken, "");
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Got it! Lets see what we found...");
		System.out.println();

		UserProfile user = provider.extractUserProfile(body);
		System.out.println(user.getAttributes());

		System.out.println();
		System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
		in.close();
	}	

}
