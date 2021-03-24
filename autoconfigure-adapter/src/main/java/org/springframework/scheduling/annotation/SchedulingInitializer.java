package org.springframework.scheduling.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

/**
 * {@link ApplicationContextInitializer} adapter for {@link EnableScheduling} and
 */
public class SchedulingInitializer {
	public void initialize(GenericApplicationContext context) {
		context.registerBean(
				TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME,
				ScheduledAnnotationBeanPostProcessor.class,
				() -> new SchedulingConfiguration().scheduledAnnotationProcessor(),
				bd -> bd.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
		);
	}
}
