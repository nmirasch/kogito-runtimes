/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.svg.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.FileSystem;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ProcessSvgServiceTest {

    private final static String PROCESS_INSTANCE_ID = "piId";
    private final static String PROCESS_ID = "travels";
    private final static String jsonString = "{\n" +
            "  \"data\": {\n" +
            "    \"ProcessInstances\": [\n" +
            "      {\n" +
            "        \"id\": \"piId\",\n" +
            "        \"processId\": \"processId\",\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"definitionId\": \"_1A708F87-11C0-42A0-A464-0B7E259C426F\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.26Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_2140F05A-364F-40B3-BB7B-B12927065DF8\",\n" +
            "            \"exit\": null\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_5D0733B5-53FE-40E9-9900-4CC13419C67A\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.288Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_F543B3F0-AB44-4A5B-BF17-8D9DEB505815\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.287Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_175DC79D-C2F1-4B28-BE2D-B583DFABF70D\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.26Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_B34ADDEE-DEA5-47C5-A913-F8B85ED5641F\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.225Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_5EA95D17-59A6-4567-92DF-74D36CE7F35A\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.224Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_54ABE1ED-61BE-45F9-812C-795A5D4ED35E\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.223Z\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"definitionId\": \"_1B11BEC9-402A-4E73-959A-296BD334CAB0\",\n" +
            "            \"exit\": \"2020-10-11T06:49:47.088Z\"\n" +
            "          }\n" +
            "        ]\n" +
            "      } " +
            "    ]\n" +
            "  }\n" +
            "}";

    private ProcessSvgService tested;

    private Instance instanceMock;
    private Vertx vertxMock;
    private WebClient webClientMock;
    private String dataindexURL = "http://localhost:8180";

    @BeforeAll
    public void setup() {
        vertxMock = mock(Vertx.class);
        webClientMock = mock(WebClient.class);
        instanceMock = mock(Instance.class);

        tested = spy(new ProcessSvgService(dataindexURL,
                                           "",
                                           "",
                                           "",
                                           "",
                                           vertxMock,
                                           instanceMock
        ));
        tested.setClient(webClientMock);
    }

    @Test
    public void testInitializeService() {
        lenient().when(instanceMock.isResolvable()).thenReturn(true);
        tested.initialize();
        verify(instanceMock).get();
    }

    @Test
    public void testInitializeWebOptionsService() {
        lenient().when(instanceMock.isResolvable()).thenReturn(false);
        tested.initialize();
        verify(instanceMock, never()).get();
        verify(tested).getDataIndexWebClientOptions(dataindexURL);
    }

    @Test
    public void testGetDataIndexWebclientOptions() {
        ProcessSvgService testService = new ProcessSvgService(dataindexURL,
                                                              "",
                                                              "",
                                                              "",
                                                              "",
                                                              vertxMock,
                                                              instanceMock);
        WebClientOptions options = testService.getDataIndexWebClientOptions("http://localhost:8180");
        assertThat(options.getDefaultHost()).isEqualTo("localhost");
        assertThat(options.getDefaultPort()).isEqualTo(8180);

        options = testService.getDataIndexWebClientOptions("malformedURL");
        assertThat(options).isNull();
    }

    @Test
    public void getProcessSVGFromVertxFileSystemTest() {
        FileSystem fileSystemMock = mock(FileSystem.class);
        Buffer bufferMock = mock(Buffer.class);
        String fileContent = "svg";

        lenient().when(vertxMock.fileSystem()).thenReturn(fileSystemMock);
        lenient().when(fileSystemMock.readFileBlocking(PROCESS_ID + ".svg")).thenReturn(bufferMock);
        lenient().when(bufferMock.toString(UTF_8)).thenReturn(fileContent);

        String svgContent = tested.getSvgFromVertxFileSystem(PROCESS_ID);
        assertThat(fileContent).isEqualTo(svgContent);
        verify(vertxMock).fileSystem();
        verify(fileSystemMock).readFileBlocking(PROCESS_ID + ".svg");
        verify(bufferMock).toString(UTF_8);
    }

    @Test
    public void getNodesQueryUniTest() {
        HttpRequest<Buffer> request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        lenient().when(webClientMock.post("/graphql")).thenReturn(request);
        lenient().when(request.sendJson(any())).thenReturn(Uni.createFrom().item(response));
        tested.setClient(webClientMock);

        tested.getNodesQueryUni(PROCESS_ID, PROCESS_INSTANCE_ID);

        verify(webClientMock).post("/graphql");
        verify(request).sendJson(any());
    }

    @Test
    public void getSvgFileUniTest() {
        HttpRequest<Buffer> request = mock(HttpRequest.class);
        Uni<HttpResponse<Buffer>> tUni = mock(Uni.class);
        tested.setClient(webClientMock);

        lenient().when(webClientMock.get("/diagram/" + PROCESS_ID + ".svg")).thenReturn(request);
        lenient().when(request.send()).thenReturn(tUni);

        tested.getSvgUni(PROCESS_ID);
        verify(webClientMock).get("/diagram/" + PROCESS_ID + ".svg");
        verify(request).send();
    }

    @Test
    public void getSvgFileContentSuccessFromDataIndexTest() {
        HttpResponse responseMock = mock(HttpResponse.class);
        tested.setClient(webClientMock);

        lenient().when(responseMock.statusCode()).thenReturn(200);
        lenient().when(responseMock.bodyAsString()).thenReturn(getTravelsSVGFile());

        assertThat(tested.getSvgContent(responseMock, PROCESS_ID)).isEqualTo(getTravelsSVGFile());
        verifyNoMoreInteractions(vertxMock);
    }

    @Test
    public void getSvgFileContentFailFromDataIndexTest() {
        Vertx vertxMock1 = mock(Vertx.class);
        String fileContent = "svg content";
        ProcessSvgService testService = new ProcessSvgService(dataindexURL,
                                                              "",
                                                              "",
                                                              "",
                                                              "",
                                                              vertxMock1,
                                                              instanceMock);
        HttpResponse responseMock = mock(HttpResponse.class);
        FileSystem fileSystemMock = mock(FileSystem.class);
        Buffer bufferMock = mock(Buffer.class);
        testService.setClient(webClientMock);

        lenient().when(vertxMock1.fileSystem()).thenReturn(fileSystemMock);
        lenient().when(fileSystemMock.readFileBlocking(PROCESS_ID + ".svg")).thenReturn(bufferMock);
        lenient().when(bufferMock.toString(UTF_8)).thenReturn(fileContent);
        lenient().when(responseMock.statusCode()).thenReturn(200);
        lenient().when(responseMock.bodyAsString()).thenReturn("<title>404 - Resource Not Found</title>");

        testService.getSvgContent(responseMock, PROCESS_ID);
        verify(vertxMock1).fileSystem();
    }

    @Test
    public void svgTransformToShowExecutedPathTest() {
        assertThat(tested.transformSvgToShowExecutedPath(
                getTravelsSVGFile(),
                Arrays.asList("_1A708F87-11C0-42A0-A464-0B7E259C426F"),
                Collections.emptyList())).isNotEqualTo("SVG Not processed");
        assertThat(tested.transformSvgToShowExecutedPath(
                null,
                Arrays.asList("_1A708F87-11C0-42A0-A464-0B7E259C426F"),
                Collections.emptyList())).isEqualTo("SVG Not processed");
        assertThat(tested.transformSvgToShowExecutedPath(
                getTravelsSVGFile(),
                Collections.emptyList(),
                Collections.emptyList())).isEqualTo(getTravelsSVGFile());
    }

    @Test
    public void fillNodesArraysTest() {
        HttpResponse response = mock(HttpResponse.class);
        List<String> completedNodes = new ArrayList<>();
        List<String> activedNodes = new ArrayList<>();

        lenient().when(response.statusCode()).thenReturn(200);
        lenient().when(response.bodyAsJsonObject()).thenReturn(new JsonObject(jsonString));

        tested.fillNodeArrays(response, completedNodes, activedNodes);

        assertThat(completedNodes.size()).isEqualTo(8);
        assertThat(activedNodes.size()).isEqualTo(1);
    }

    public static String getTravelsSVGFile() {
        try {
            return readFileContent("travels.svg");
        } catch (Exception e) {
            return "No svg found";
        }
    }

    public static String readFileContent(String file) throws URISyntaxException, IOException {
        Path path = Paths.get(Thread.currentThread().getContextClassLoader().getResource(file).toURI());
        return new String(Files.readAllBytes(path));
    }
}