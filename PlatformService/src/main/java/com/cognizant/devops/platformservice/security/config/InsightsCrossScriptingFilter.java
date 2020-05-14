/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.security.config;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsCrossScriptingFilter extends OncePerRequestFilter {
	private static Logger LOG = LogManager.getLogger(InsightsCrossScriptingFilter.class);

	/**
	 * This filter is used to validate Header,Cookies and Paramater for each and
	 * every reuest
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponce,
			FilterChain filterChain) {
		LOG.info("Inside Filter == InsightsCrossScriptingFilter ............... {} method {}  ",
				httpRequest.getRequestURL(),
				httpRequest.getMethod());

		try {
			validateHeaders(httpRequest, httpResponce);
			RequestWrapper requestMapper = new RequestWrapper(httpRequest, httpResponce);
			filterChain.doFilter(requestMapper, httpResponce);
			LOG.debug("Completed .. in InsightsCrossScriptingFilter");

		} catch (Exception e) {
			String msg;
			if (e.getMessage().contains("InsightsCustomException")) {
				LOG.error("Invalid request in InsightsCustomException CrossScriptingFilter {} ", e.getMessage());
				JsonParser parser = new JsonParser();
				JsonObject element = parser.parse(e.getMessage()).getAsJsonObject();
				msg = PlatformServiceUtil.buildFailureResponse(
						"InsightsCustomException Invalid request,Someting is wrong in cookies,Header or Parameter"
								+ e.getMessage() + " status " + element.get("StatusCode"))
						.toString();
				AuthenticationUtils.setResponseMessage(httpResponce, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
			} else {
				LOG.error("Invalid request in CrossScriptingFilter {}", e.getMessage());
				msg = PlatformServiceUtil.buildFailureResponse(
								"Invalid request,Someting is wrong in cookies,Header or Parameter" + e.getMessage())
						.toString();
				AuthenticationUtils.setResponseMessage(httpResponce, HttpServletResponse.SC_BAD_REQUEST, msg);
			}
		}
		LOG.info("Out doFilter CrossScriptingFilter ...............");
	}

	/**
	 * used to validate header host with trusted host list
	 * 
	 * @param request
	 * @param response
	 */
	public void validateHeaders(HttpServletRequest request, HttpServletResponse response) {
		LOG.info(" InsightsCrossScriptingFilter validate header ............... ");
		String origin = request.getHeader(HttpHeaders.ORIGIN);
		String host = AuthenticationUtils.getHost(request);
		LOG.debug(" host and origin information ===== {}  origin {}", host, origin);
		if (host == null) {
			LOG.error(" Invalid request " + PlatformServiceConstants.HOST_NOT_FOUND);
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.INFORMATION_MISMATCH,
					PlatformServiceConstants.HOST_NOT_FOUND);
		}
		if (!ApplicationConfigProvider.getInstance().getTrustedHosts().contains(host)) {
			LOG.error(" Invalid request " + PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
			AuthenticationUtils.setResponseMessage(response, AuthenticationUtils.INFORMATION_MISMATCH,
					PlatformServiceConstants.INVALID_REQUEST_ORIGIN);
		}
	}
}
