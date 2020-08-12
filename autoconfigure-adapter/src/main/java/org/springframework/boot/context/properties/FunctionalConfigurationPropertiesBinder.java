/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.boot.context.properties;

import org.springframework.boot.context.properties.bind.*;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySources;

/**
 * Allow to access to package private Boot classes like {@code PropertySourcesDeducer}.
 */
public class FunctionalConfigurationPropertiesBinder {

	private final Binder binder;
	private final Boolean strict;

	public FunctionalConfigurationPropertiesBinder(ConfigurableApplicationContext context, Boolean strict) {
		PropertySources propertySources = new FunctionalPropertySourcesDeducer(context).getPropertySources();
		this.strict = strict;
		this.binder = new Binder(ConfigurationPropertySources.from(propertySources),
        				new PropertySourcesPlaceholdersResolver(propertySources),
        				null,
				(registry) -> context.getBeanFactory().copyRegisteredEditorsTo(registry));
	}

	public <T> BindResult<T> bind(String prefix, Bindable<T> target) {
		UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
		NoUnboundElementsBindHandler strictHandler = new NoUnboundElementsBindHandler(new IgnoreTopLevelConverterNotFoundBindHandler(), filter);
		BindHandler handler = strict ? strictHandler : new IgnoreErrorsBindHandler(strictHandler);
		return binder.bind(prefix, target, handler);
	}
}
