package org.eurekaclinical.scribeupext.provider;

/*
 * #%L
 * Eureka! Clinical ScribeUP Extensions
 * %%
 * Copyright (C) 2014, 2017 Emory University & The Johns Hopkins University
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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eurekaclinical.scribeupext.GlobusApi;
import org.eurekaclinical.scribeupext.profile.EurekaAttributesDefinition;
import org.eurekaclinical.scribeupext.profile.GlobusAttributesDefinition;
import org.eurekaclinical.scribeupext.profile.GlobusProfile;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.exception.HttpException;

/**
 *
 * @author Andrew Post
 * @author Stephen Granite
 */
public class GlobusProvider {

	private OAuth20Service service;
	private String key, secret, callbackUrl, scope, state;

	protected GlobusProvider newProvider() {
		return new GlobusProvider();
	}

	protected void internalInit() {
		setService(new ServiceBuilder(key)
				.apiSecret(secret)
				.callback(callbackUrl)
				.scope(scope)
				.state(state)
				.build(GlobusApi.instance()));
	}

	public String getProfileUrl() {
		return "https://auth.globus.org/v2/oauth2/userinfo";
	}

	public String sendRequestForData(Token accessToken, String dataUrl) throws HttpException {
		String body = "";
		try {
			OAuthRequest request = new OAuthRequest(Verb.GET, getProfileUrl());
			getService().signRequest((OAuth2AccessToken) accessToken, request);
			Response response = getService().execute(request);
			int code = response.getCode();
			body = response.getBody();
			if (code != 200) {
				throw new HttpException(code, body);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return body;
	}


	public UserProfile extractUserProfile(final String body) {
		GlobusProfile profile = new GlobusProfile();
		JsonNode json = JsonHelper.getFirstNode(body);
		if (json != null) {
			profile.setId(JsonHelper.get(json, GlobusAttributesDefinition.USERNAME));
			profile.addAttribute(EurekaAttributesDefinition.USERNAME, JsonHelper.get(json, GlobusAttributesDefinition.USERNAME).toString().split("@")[0]);
			profile.addAttribute(EurekaAttributesDefinition.FULLNAME, JsonHelper.get(json, GlobusAttributesDefinition.FULLNAME));
			profile.addAttribute(EurekaAttributesDefinition.EMAIL, JsonHelper.get(json, GlobusAttributesDefinition.EMAIL));
		}
		return profile;
	}

	public OAuth20Service getService() {
		internalInit();
		return service;
	}

	protected void setService(OAuth20Service service) {
		this.service = service;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callBackUrl) {
		this.callbackUrl = callBackUrl;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
}
