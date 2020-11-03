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

import javax.enterprise.inject.Instance;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.svg.ProcessSvgService;
import org.kie.kogito.svg.ProcessSvgServiceTest;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class QuarkusProcessSvgServiceTest extends ProcessSvgServiceTest {

    private QuarkusProcessSvgService tested;
    private Instance instanceMock;
    private WebClient webClientMock;

    @BeforeAll
    public void setup() {
        webClientMock = mock(WebClient.class);
        instanceMock = mock(Instance.class);
        vertxMock = mock(Vertx.class);

        tested = spy(new QuarkusProcessSvgService(dataIndexURL,
                                                  "",
                                                  "",
                                                  "",
                                                  "",
                                                  vertxMock,
                                                  instanceMock
        ));
        tested.setAddonWebClient(webClientMock);
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
        verify(tested).getWebClientToURLOptions(dataIndexURL);
    }

    @Override
    protected ProcessSvgService getTestedProcessSvgService() {
        return tested;
    }
}