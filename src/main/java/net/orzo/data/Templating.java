/*
 * Copyright (C) 2014 Tomas Machalek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.orzo.data;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import net.orzo.tools.ResourceLoader;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.TemplateLoader;

/**
 * Simple wrapper for Jade templating engine for producing HTML outputs with
 * embedded CSS.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class Templating {

    /**
     *
     * @param templatePath
     * @param cssURI
     * @param model
     * @return
     * @todo This is still quite experimental
     */
    public String renderTemplate(String templateURI, String cssURI, Object model)
            throws IOException {

        Map<String, Object> javaModel = (Map<String, Object>) model; // TODO type unwrapping???
        JadeConfiguration config = new JadeConfiguration();
        javaModel.put("__css__", loadStylesheet(cssURI));

        config.setPrettyPrint(true);
        config.setTemplateLoader(new TemplateLoader() {

            @Override
            public Reader getReader(String templateName) throws IOException {
                ResourceLoader loader = new ResourceLoader();
                return new StringReader(loader
                        .getResourceAsString(templateName));
            }

            @Override
            public long getLastModified(String arg0) throws IOException {
                return 0;
            }
        });

        return config
                .renderTemplate(config.getTemplate(templateURI), javaModel);
    }

    /**
     *
     */
    private String loadStylesheet(String cssURI) throws IOException {
        ResourceLoader loader = new ResourceLoader();
        return loader.getResourceAsString(cssURI);
    }
}
