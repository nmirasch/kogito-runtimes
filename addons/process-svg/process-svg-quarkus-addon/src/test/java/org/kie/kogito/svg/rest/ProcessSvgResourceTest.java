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

package org.kie.kogito.svg.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.kie.kogito.svg.service.QuarkusProcessSvgService;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessSvgResourceTest {

    private final static String PROCESS_INSTANCE_ID = "piId";
    private final static String PROCESS_ID = "travels";

    private ProcessSvgResource processSvgResourceTest;
    private QuarkusProcessSvgService processSvgServiceMock;
    private HttpResponse responseMock;

    @BeforeAll
    public void setup() {
        processSvgResourceTest = new ProcessSvgResource();
        processSvgServiceMock = mock(QuarkusProcessSvgService.class);
        responseMock = mock(HttpResponse.class);
        processSvgResourceTest.setProcessSvgService(processSvgServiceMock);

        lenient().when(processSvgServiceMock.getNodesQueryUni(PROCESS_ID, PROCESS_INSTANCE_ID))
                .thenReturn(Uni.createFrom().item(responseMock));
    }

    @Test
    void getProcessSvgTest() throws IOException {
        processSvgResourceTest.getProcessSvg(PROCESS_ID);
        verify(processSvgServiceMock).getProcessSvg(PROCESS_ID);
    }

    @Test
    void getExecutionPathByProcessInstanceIdTest() throws IOException {
        processSvgResourceTest.getExecutionPathByProcessInstanceId(PROCESS_ID, PROCESS_INSTANCE_ID);
        verify(processSvgServiceMock).getProcessInstanceSvg(PROCESS_ID, PROCESS_INSTANCE_ID);
    }
}