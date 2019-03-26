/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.compiler.compiler;

import java.io.IOException;
import java.io.InputStream;

import org.kie.api.KieBase;
import org.kie.api.internal.utils.ServiceRegistry;

public class GuidedScoreCardFactory {
    private static GuidedScoreCardProvider provider = ServiceRegistry.getInstance().get(GuidedScoreCardProvider.class);

    public static String loadFromInputStream(InputStream is) throws IOException {
        return getGuidedScoreCardProvider().loadFromInputStream(is);
    }

    public static KieBase getKieBaseFromInputStream(InputStream is) throws IOException {
    	return getGuidedScoreCardProvider().getKieBaseFromInputStream(is);
    }

    public static String getPMMLStringFromInputStream(InputStream is) throws IOException {
    	return getGuidedScoreCardProvider().getPMMLStringFromInputStream(is);
    }

    public static synchronized GuidedScoreCardProvider getGuidedScoreCardProvider() {
        return provider;
    }

}
