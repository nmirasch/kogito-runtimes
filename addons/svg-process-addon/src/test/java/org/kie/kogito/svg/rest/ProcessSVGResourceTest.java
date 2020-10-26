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

import java.util.Arrays;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.kie.kogito.svg.service.ProcessSVGService;
import org.kie.kogito.svg.service.ProcessSVGsServiceTest;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessSVGResourceTest {

    private final static String PROCESS_INSTANCE_ID = "piId";
    private final static String PROCESS_ID = "travels";

    private ProcessSVGResource processSVGResourceTest;
    private ProcessSVGService processSVGServiceMock;
    private HttpResponse responseMock;
    private String svgFileContent;

    @BeforeAll
    public void setup() {
        processSVGResourceTest = new ProcessSVGResource();
        processSVGServiceMock = mock(ProcessSVGService.class);
        responseMock = mock(HttpResponse.class);
        svgFileContent = "svg";
        processSVGResourceTest.setProcessSVGService(processSVGServiceMock);

        lenient().when(processSVGServiceMock.getSvgFileUni(PROCESS_ID))
                .thenReturn(Uni.createFrom().item(ProcessSVGsServiceTest.getTravelsSVGFile()));
        lenient().when(processSVGServiceMock.getNodesQueryUni(PROCESS_ID, PROCESS_INSTANCE_ID))
                .thenReturn(Uni.createFrom().item(responseMock));
    }

    @Test
    void getSVGExecutionPathByProcessInstanceTest() {
        processSVGResourceTest.getSVGExecutionPathByProcessInstance(PROCESS_ID, PROCESS_INSTANCE_ID);

        verify(processSVGServiceMock).getNodesQueryUni(PROCESS_ID, PROCESS_INSTANCE_ID);
        verify(processSVGServiceMock).getSvgFileUni(PROCESS_ID);
    }

    @Test
    void getProcessUnisCombinedResultsTest() throws Exception {
        processSVGResourceTest.processUnisCombinedResults(Arrays.asList(responseMock, svgFileContent));
        verify(processSVGServiceMock).fillNodesArrays(eq(responseMock), anyList(), anyList());
        verify(processSVGServiceMock).svgTransformToShowExecutedPath(eq(svgFileContent), anyList(), anyList());
    }
}