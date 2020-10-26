/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.svg.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

import org.jbpm.process.svg.SVGImageProcessor;
import org.jbpm.process.svg.processor.SVGProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessSvgService {
    public static final int RESPONSE_STATUS_CODE_OK = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSvgService.class);

    private Vertx vertx;

    private Instance<WebClient> providedWebClient;

    private WebClient client;
    private String dataIndexHttpURL;
    private String svgResourcesPath;
    private String completedColor;
    private String completedBorderColor;
    private String activeBorderColor;

    @Inject
    public ProcessSvgService(
            @ConfigProperty(name = "kogito.dataindex.http.url", defaultValue = "http://localhost:8180") String dataIndexHttpURL,
            @ConfigProperty(name = "kogito.svg.resources.path", defaultValue = "META-INF/processSVG/") String svgResourcesPath,
            @ConfigProperty(name = "kogito.svg.color.completed", defaultValue = "#C0C0C0") String completedColor,
            @ConfigProperty(name = "kogito.svg.color.completed.border", defaultValue = "#030303") String completedBorderColor,
            @ConfigProperty(name = "kogito.svg.color.active.border", defaultValue = "#FF0000") String activeBorderColor,
            Vertx vertx,
            Instance<WebClient> providedWebClient) {
        this.dataIndexHttpURL = dataIndexHttpURL;
        this.svgResourcesPath = svgResourcesPath;
        this.completedColor = completedColor;
        this.completedBorderColor = completedBorderColor;
        this.activeBorderColor = activeBorderColor;
        this.vertx = vertx;
        this.providedWebClient = providedWebClient;
    }

    @PostConstruct
    public void initialize() {
        if (providedWebClient.isResolvable()) {
            this.client = providedWebClient.get();
        } else {
            WebClientOptions webClientOptions = getDataIndexWebClientOptions(this.dataIndexHttpURL);
            setClient(WebClient.create(Vertx.vertx(), webClientOptions));
            LOGGER.debug("Creating new instance of web client");
        }
    }

    public WebClient getClient() {
        return client;
    }

    public void setClient(WebClient client) {
        this.client = client;
    }

    public String getSvgFromVertxFileSystem(String processId) {
        return vertx.fileSystem()
                .readFileBlocking(svgResourcesPath + processId + ".svg")
                .toString(UTF_8);
    }

    public Uni<HttpResponse<Buffer>> getNodesQueryUni(String processId, String processInstanceId) {
        String query = "{ ProcessInstances ( where: { and : {  id: {  equal : \"" + processInstanceId + "\" }, processId : { equal : \"" + processId + "\"} } }) { nodes { definitionId exit } } }";
        return getClient().post("/graphql")
                .sendJson(JsonObject.mapFrom(Collections.singletonMap("query", query)));
    }

    public Uni<String> getSvgUni(String processId) {
        return getClient().get("/diagram/" + processId + ".svg")
                .send()
                .map(resp -> getSvgContent(resp, processId));
    }

    protected String getSvgContent(HttpResponse<Buffer> resp, String processId) {
        if (resp != null &&
                resp.bodyAsString() != null &&
                !resp.bodyAsString().contains("<title>404 - Resource Not Found</title>")) {
            return resp.bodyAsString();
        }
        return getSvgFromVertxFileSystem(processId);
    }

    public String transformSvgToShowExecutedPath(String svg, List<String> completedNodes, List<String> activeNodes) {
        if (svg != null) {
            if (!(completedNodes.isEmpty() && activeNodes.isEmpty())) {
                InputStream svgStream = new ByteArrayInputStream(svg.getBytes());
                SVGProcessor processor = new SVGImageProcessor(svgStream).getProcessor();
                completedNodes.stream().forEach(nodeId -> processor.defaultCompletedTransformation(nodeId, completedColor, completedBorderColor));
                activeNodes.stream().forEach(nodeId -> processor.defaultActiveTransformation(nodeId, activeBorderColor));
                return processor.getSVG();
            } else {
                return svg;
            }
        } else {
            return "SVG Not processed";
        }
    }

    public void fillNodeArrays(HttpResponse<Buffer> response, List<String> completedNodes, List<String> activeNodes) {
        if (response.statusCode() == RESPONSE_STATUS_CODE_OK) {
            JsonArray pInstancesArray = response.bodyAsJsonObject().getJsonObject("data")
                    .getJsonArray("ProcessInstances");
            if (pInstancesArray != null && !pInstancesArray.isEmpty()) {
                JsonArray nodesArray = pInstancesArray.getJsonObject(0).getJsonArray("nodes");
                nodesArray.stream()
                        .forEach(node -> {
                            if (isNull(((JsonObject) node).getInstant("exit"))) {
                                activeNodes.add(((JsonObject) node).getString("definitionId"));
                            } else {
                                completedNodes.add(((JsonObject) node).getString("definitionId"));
                            }
                        });
            }
        }
    }

    protected WebClientOptions getDataIndexWebClientOptions(String dataIndexHttpURL) {
        try {
            URL dataIndexURL = new URL(dataIndexHttpURL);
            return new WebClientOptions()
                    .setDefaultHost(dataIndexURL.getHost())
                    .setDefaultPort(dataIndexURL.getPort())
                    .addEnabledSecureTransportProtocol(dataIndexURL.getProtocol());
        } catch (MalformedURLException malformedURLException) {
        }
        return null;
    }
}
