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

package org.kie.kogito.svg.processor;

import org.apache.batik.anim.dom.SVGOMTSpanElement;
import org.kie.kogito.svg.model.NodeSummary;
import org.kie.kogito.svg.model.SetBackgroundColorTransformation;
import org.kie.kogito.svg.model.SetBorderColorTransformation;
import org.kie.kogito.svg.model.SetSubProcessLinkTransformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JBPMDesignerSVGProcessor extends AbstractSVGProcessor {

    public JBPMDesignerSVGProcessor(Document svgDocument, boolean mapById) {
        super(svgDocument, mapById);
    }

    @Override
    public void defaultCompletedTransformation(String nodeId, String completedNodeColor, String completedBorderColor) {
        transform(new SetBackgroundColorTransformation(nodeId, completedNodeColor));
        transform(new SetBorderColorTransformation(nodeId, completedBorderColor));
    }

    @Override
    public void defaultActiveTransformation(String nodeId, String activeNodeBorderColor) {
        transform(new SetBorderColorTransformation(nodeId, activeNodeBorderColor));
    }

    @Override
    public void defaultSubProcessLinkTransformation(String nodeId, String link) {
        transform(new SetSubProcessLinkTransformation(nodeId, link));
    }

    @Override
    public void processNodes(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node svgIdNode = attributes.getNamedItem("id");
                if (svgIdNode != null) {
                    String svgId = svgIdNode.getNodeValue();
                    if (mapById) {
                        Node nodeIdNode = attributes.getNamedItem("bpmn2nodeid");
                        if (nodeIdNode != null) {
                            String nodeId = nodeIdNode.getNodeValue();
                            Element border = null;
                            Element background = null;
                            Element subProcessLink = null;
                            if (nodeId != null) {
                                background = svgDocument.getElementById(svgId + "fill_el");
                                border = svgDocument.getElementById(svgId + "bg_frame");
                                Element borderSubProcess = svgDocument.getElementById(svgId + "frame");

                                subProcessLink = svgDocument.getElementById(svgId + "pimg");
                                summary.addNode(new NodeSummary(nodeId, border, background, borderSubProcess, subProcessLink));
                            }
                        }
                    } else {
                        // map by name
                        if (svgId.endsWith("text_name")) {
                            svgId = svgId.substring(0, svgId.length() - 9);
                            StringBuilder taskLabel = new StringBuilder();
                            for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                                if (node.getChildNodes().item(j) instanceof SVGOMTSpanElement) {
                                    SVGOMTSpanElement spanElement = (SVGOMTSpanElement) node.getChildNodes().item(j);
                                    taskLabel.append(spanElement.getFirstChild().getNodeValue());
                                }
                            }
                            String name = taskLabel.toString();
                            // filtering out nodes with no name
                            if (!name.trim().isEmpty()) {
                                Element background = svgDocument.getElementById(svgId + "fill_el");
                                Element border = svgDocument.getElementById(svgId + "bg_frame");
                                Element borderSubProcess = svgDocument.getElementById(svgId + "frame");

                                Element subProcessLink = svgDocument.getElementById(svgId + "pimg");
                                summary.addNode(new NodeSummary(name, border, background, borderSubProcess, subProcessLink));
                            }
                        }
                    }
                }
            }
            processNodes(node.getChildNodes());
        }
    }
}
