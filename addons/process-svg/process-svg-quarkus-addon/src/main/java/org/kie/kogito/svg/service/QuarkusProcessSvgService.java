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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.svg.ProcessSvgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class QuarkusProcessSvgService extends ProcessSvgService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusProcessSvgService.class);

    private Instance<WebClient> providedWebClient;

    @Inject
    public QuarkusProcessSvgService(
            @ConfigProperty(name = "kogito.dataindex.http.url", defaultValue = "http://localhost:8180") String dataIndexHttpURL,
            @ConfigProperty(name = "kogito.svg-diagram.folder.path", defaultValue = "META-INF/processSVG/") String svgResourcesPath,
            @ConfigProperty(name = "kogito.svg.color.completed", defaultValue = "#C0C0C0") String completedColor,
            @ConfigProperty(name = "kogito.svg.color.completed.border", defaultValue = "#030303") String completedBorderColor,
            @ConfigProperty(name = "kogito.svg.color.active.border", defaultValue = "#FF0000") String activeBorderColor,
            Instance<WebClient> providedWebClient) {
        this.dataIndexHttpURL = dataIndexHttpURL;
        this.svgResourcesPath = svgResourcesPath;
        this.completedColor = completedColor;
        this.completedBorderColor = completedBorderColor;
        this.activeBorderColor = activeBorderColor;
        this.providedWebClient = providedWebClient;
    }

    @PostConstruct
    public void initialize() {
        if (providedWebClient.isResolvable()) {
            this.dataIndexWebClient = providedWebClient.get();
        } else {
            setDataIndexWebClient(WebClient.create(Vertx.vertx(), getWebClientToURLOptions(this.dataIndexHttpURL)));
            LOGGER.debug("Creating new instance of web client");
        }
    }
}
