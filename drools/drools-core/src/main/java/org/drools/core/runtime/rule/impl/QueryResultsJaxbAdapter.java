/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.runtime.rule.impl;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.drools.core.QueryResultsImpl;


public class QueryResultsJaxbAdapter extends XmlAdapter<QueryResultsImpl, FlatQueryResults>{

    @Override
    public QueryResultsImpl marshal(FlatQueryResults v) throws Exception {
        return null;
    }

    @Override
    public FlatQueryResults unmarshal(QueryResultsImpl v) throws Exception {
        return new FlatQueryResults(v);
    }


}
