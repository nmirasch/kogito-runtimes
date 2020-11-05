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

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.kie.kogito.svg.model.SVGSummary;
import org.kie.kogito.svg.model.Transformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractSVGProcessor implements SVGProcessor {

    protected Document svgDocument;
    protected SVGSummary summary = new SVGSummary();
    protected boolean mapById = true;

    public AbstractSVGProcessor(Document svgDocument, boolean mapById) {
        this.svgDocument = svgDocument;
        this.mapById = mapById;
    }

    @Override
    public void transform(Transformation t) {
        t.transform(summary);
    }

    @Override
    public String getSVG() {
        try {
            DOMSource domSource = new DOMSource(svgDocument.getFirstChild());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            ((Element) svgDocument.getFirstChild()).setAttribute("viewBox", "0 0 " +
                    ((Element) svgDocument.getFirstChild()).getAttribute("width") + " " +
                    ((Element) svgDocument.getFirstChild()).getAttribute("height"));
            ((Element) svgDocument.getFirstChild()).removeAttribute("width");
            ((Element) svgDocument.getFirstChild()).removeAttribute("height");
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("Could not transform svg", e);
        }
    }

    @Override
    public void defaultCompletedTransformation(String nodeId) {
        defaultCompletedTransformation(nodeId, COMPLETED_COLOR, COMPLETED_BORDER_COLOR);
    }

    @Override
    public void defaultActiveTransformation(String nodeId) {
        defaultActiveTransformation(nodeId, ACTIVE_BORDER_COLOR);
    }
}
