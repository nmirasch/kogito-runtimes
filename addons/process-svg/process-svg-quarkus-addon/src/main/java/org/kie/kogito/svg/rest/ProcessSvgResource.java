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
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import org.kie.kogito.svg.service.QuarkusProcessSvgService;

@ApplicationScoped
@Path("/svg/")
public class ProcessSvgResource {

    @Inject
    QuarkusProcessSvgService service;

    @Path("processes/{processId}/instances/{processInstanceId}")
    @GET
    @Produces(MediaType.APPLICATION_SVG_XML)
    public Uni<String> getExecutionPathByProcessInstanceId(
            @PathParam("processId") String processId,
            @PathParam("processInstanceId") String processInstanceId) {
        Uni<HttpResponse<Buffer>> queryNodesUni = service.getNodesQueryUni(processId, processInstanceId);
        String svg = service.getSvgFromVertxFileSystem(processId);
        return queryNodesUni.onItem().transform(queryResults -> {
            List<String> completedNodes = new ArrayList<>();
            List<String> activeNodes = new ArrayList<>();
            service.fillNodeArrays(queryResults, completedNodes, activeNodes);
            return service.annotateExecutedPath(svg, completedNodes, activeNodes);
        });
    }

    protected void setProcessSvgService(QuarkusProcessSvgService service) {
        this.service = service;
    }
}

