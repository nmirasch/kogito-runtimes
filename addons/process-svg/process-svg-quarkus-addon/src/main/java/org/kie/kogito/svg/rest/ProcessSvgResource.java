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

package org.kie.kogito.svg.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.vertx.web.Route;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.kie.kogito.svg.service.QuarkusProcessSvgService;

@ApplicationScoped
public class ProcessSvgResource {

    @Inject
    QuarkusProcessSvgService service;

    public void setupRouter(@Observes Router router) {
        StaticHandler diagramHandler = StaticHandler.create();
        diagramHandler.setAllowRootFileSystemAccess(true);
        diagramHandler.setWebRoot(service.getSvgResourcesPath());
        router.route("/diagram/*").handler(diagramHandler);
    }

    @Route(path = "/svg/process/:processId/instances/:processInstanceId", methods = HttpMethod.GET, produces = "application/svg+xml")
    public Uni<String> getSvgExecutionPathByProcessInstance(RoutingContext context) {
        String processId = context.pathParam("processId");
        String processInstanceId = context.pathParam("processInstanceId");
        Uni<HttpResponse<Buffer>> queryNodesUni = service.getNodesQueryUni(processId, processInstanceId);
        Uni<String> getSvgUni = service.getSvgUni(processId, context);

        List<Uni<?>> list = Arrays.asList(queryNodesUni, getSvgUni);
        return Uni.combine().all().unis(list).combinedWith(results -> processUnisCombinedResults(results));
    }

    protected String processUnisCombinedResults(List combinedResults) {
        List<String> completedNodes = new ArrayList<>();
        List<String> activeNodes = new ArrayList<>();
        service.fillNodeArrays((HttpResponse<Buffer>) combinedResults.get(0), completedNodes, activeNodes);
        return service.transformSvgToShowExecutedPath(((String) combinedResults.get(1)), completedNodes, activeNodes);
    }

    protected void setProcessSvgService(QuarkusProcessSvgService service) {
        this.service = service;
    }
}

