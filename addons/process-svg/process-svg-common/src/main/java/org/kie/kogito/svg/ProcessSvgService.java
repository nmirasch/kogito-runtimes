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

package org.kie.kogito.svg;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.kie.kogito.svg.processor.SVGProcessor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

public abstract class ProcessSvgService {

    public static final int RESPONSE_STATUS_CODE_OK = 200;
    private static Path svgDir = Paths.get("META-INF", "processSVG");
    protected WebClient dataIndexWebClient;
    protected WebClient addonWebClient;
    protected String dataIndexHttpURL;
    protected String svgResourcesPath;
    protected String completedColor;
    protected String completedBorderColor;
    protected String activeBorderColor;
    protected Vertx vertx;

    public WebClient getDataIndexWebClient() {
        return dataIndexWebClient;
    }

    public void setDataIndexWebClient(WebClient dataIndexWebClient) {
        this.dataIndexWebClient = dataIndexWebClient;
    }

    public void setAddonWebClient(WebClient addonWebClient) {
        this.addonWebClient = addonWebClient;
    }

    public Uni<HttpResponse<Buffer>> getNodesQueryUni(String processId, String processInstanceId) {
        String query = "{ ProcessInstances ( where: { and : {  id: {  equal : \"" + processInstanceId + "\" }, processId : { equal : \"" + processId + "\"} } }) { nodes { definitionId exit } } }";
        return getDataIndexWebClient().post("/graphql")
                .sendJson(JsonObject.mapFrom(Collections.singletonMap("query", query)));
    }

    public String getProcessSvg(String processId) throws IOException {
        try {
            return vertx.fileSystem()
                    .readFileBlocking(svgResourcesPath + "/" + processId + ".svg")
                    .toString(UTF_8);
        } catch (Exception exception) {
            return readFileContentFromClassPath(processId + ".svg");
        }
    }

    protected String readFileContentFromClassPath(String fileName) throws IOException {
        Path svgFile = svgDir.resolve(fileName);
        try (
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(svgFile.toString());
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ) {
            Objects.requireNonNull(inputStream, "Could not resolve file path: " + svgFile.toString());
            int result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            return buf.toString(StandardCharsets.UTF_8.name());
        } catch (Exception exception) {
            throw new FileNotFoundException();
        }
    }

    public String annotateExecutedPath(String svg, List<String> completedNodes, List<String> activeNodes) {
        if (svg != null && !svg.isEmpty()) {
            if (!(completedNodes.isEmpty() && activeNodes.isEmpty())) {
                try (InputStream svgStream = new ByteArrayInputStream(svg.getBytes())) {
                    SVGProcessor processor = new SVGImageProcessor(svgStream).getProcessor();
                    completedNodes.forEach(nodeId -> processor.defaultCompletedTransformation(nodeId, completedColor, completedBorderColor));
                    activeNodes.forEach(nodeId -> processor.defaultActiveTransformation(nodeId, activeBorderColor));
                    return processor.getSVG();
                } catch (Exception e) {
                    return svg;
                }
            } else {
                return svg;
            }
        } else {
            return "";
        }
    }

    public void fillNodeArrays(HttpResponse<Buffer> response, List<String> completedNodes, List<String> activeNodes) {
        if (response.statusCode() == RESPONSE_STATUS_CODE_OK) {
            JsonArray pInstancesArray = response.bodyAsJsonObject().getJsonObject("data")
                    .getJsonArray("ProcessInstances");
            if (pInstancesArray != null && !pInstancesArray.isEmpty()) {
                JsonArray nodesArray = pInstancesArray.getJsonObject(0).getJsonArray("nodes");
                nodesArray.forEach(node -> {
                    if (isNull(((JsonObject) node).getInstant("exit"))) {
                        activeNodes.add(((JsonObject) node).getString("definitionId"));
                    } else {
                        completedNodes.add(((JsonObject) node).getString("definitionId"));
                    }
                });
            }
        }
    }

    public WebClientOptions getWebClientToURLOptions(String targetHttpURL) {
        try {
            URL dataIndexURL = new URL(targetHttpURL);
            return new WebClientOptions()
                    .setDefaultHost(dataIndexURL.getHost())
                    .setDefaultPort(dataIndexURL.getPort())
                    .addEnabledSecureTransportProtocol(dataIndexURL.getProtocol());
        } catch (MalformedURLException malformedURLException) {
            return null;
        }
    }

    public Uni<String> getProcessInstanceSvg(String processId, String processInstanceId) throws IOException {
        Uni<HttpResponse<Buffer>> queryNodesUni = getNodesQueryUni(processId, processInstanceId);
        String svg = getProcessSvg(processId);
        return queryNodesUni.onItem().transform(queryResults -> {
            List<String> completedNodes = new ArrayList<>();
            List<String> activeNodes = new ArrayList<>();
            fillNodeArrays(queryResults, completedNodes, activeNodes);
            return annotateExecutedPath(svg, completedNodes, activeNodes);
        });
    }
}
