/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.svg.dataindex;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.kie.kogito.svg.ProcessSVGException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

@Component
public class SpringBootDataIndexClient implements DataIndexClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootDataIndexClient.class);
    private SecurityContext context = SecurityContextHolder.getContext();
    private String dataIndexHttpURL;

    private RestTemplate restTemplate;

    @Autowired
    public SpringBootDataIndexClient(
            @Value("${kogito.dataindex.http.url:http://localhost:8180}") String dataIndexHttpURL,
            @Autowired(required = false) RestTemplate restTemplate) {
        this.dataIndexHttpURL = dataIndexHttpURL;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initialize() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            LOGGER.debug("No RestTemplate found, creating a default one");
        }
    }

    @Override
    public List<NodeInstance> getNodeInstancesFromProcessInstance(String processInstanceId) {
        String query = getNodeInstancesQuery(processInstanceId);
        CompletableFuture<List<NodeInstance>> cf = new CompletableFuture<>();

        String requestJson = "{\"query\":\"" + query + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getToken());
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(dataIndexHttpURL + "/graphql",
                                                                   entity, String.class);
        if (result.getStatusCode().ordinal() == 200) {
            cf.complete(getNodeInstancesFromResponse(result.getBody()));
        }
        //else {
        //    cf.completeExceptionally(result.);
        //}

        try {
            return cf.get();
        } catch (Exception e) {
            throw new ProcessSVGException("Exception while trying to get data from Data Index service", e);
        }
    }

    protected List<NodeInstance> getNodeInstancesFromResponse(Object response) {
        JsonArray pInstancesArray = response.getJsonObject("data").getJsonArray("ProcessInstances");
        if (pInstancesArray != null && !pInstancesArray.isEmpty()) {
            List<NodeInstance> nodes = new ArrayList<>();
            JsonArray nodesArray = pInstancesArray.getJsonObject(0).getJsonArray("nodes");
            nodesArray.forEach(node -> {
                JsonObject json = (JsonObject) node;
                nodes.add(new NodeInstance(nonNull(json.getString("exit")), json.getString("definitionId")));
            });
            return nodes;
        } else {
            return emptyList();
        }
    }

    protected String getToken() {
        // SecurityContextHolder.getContext().getAuthentication().getCredentials().;
        // if (identity != null && identity.getCredential(TokenCredential.class) != null) {
        //     return "Bearer " + identity.getCredential(TokenCredential.class).getToken();
        // }
        // return "";
    }
}
