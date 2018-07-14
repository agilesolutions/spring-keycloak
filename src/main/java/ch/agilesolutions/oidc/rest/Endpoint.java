package ch.agilesolutions.oidc.rest;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class Endpoint {

	private static final Logger LOGGER = LoggerFactory.getLogger(Endpoint.class);
	
	@Autowired
    private KeycloakRestTemplate restTemplate;

	@Bean
	@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public AccessToken getAccessToken() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		return ((KeycloakPrincipal) request.getUserPrincipal()).getKeycloakSecurityContext().getToken();
	}

	@GetMapping(value = "/protected")
	public ResponseEntity<String> getVersion() {

		ResponseEntity<String> entity = null;
		
		AccessToken token = getAccessToken();
		
		LOGGER.info(String.format("Processing access token %s", token.getName()));

		try {

			entity = restTemplate.exchange("http://source/version",
					HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
					});

		} catch (Exception e) {
			LOGGER.error("Error returning version number from service");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		if (entity.getStatusCode() == HttpStatus.OK ) {
			LOGGER.info(String.format("Found IP Range for location code %s", entity.getBody().toString()));
			return new ResponseEntity<String>(entity.getBody().toString(), HttpStatus.OK);
			
		} else {
			LOGGER.info(String.format("IP Range failed due to HTTP code %s", entity.getStatusCode() ));
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

}
