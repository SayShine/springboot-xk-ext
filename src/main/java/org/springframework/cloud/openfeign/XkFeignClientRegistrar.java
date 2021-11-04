package org.springframework.cloud.openfeign;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.openfeign.copy.CopyFeignClientsRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-10-21 17:07
 * 啥也不干，哎，就是玩
 */
public class XkFeignClientRegistrar extends CopyFeignClientsRegistrar {

    @Override
    protected void registerFeignClient(BeanDefinitionRegistry registry,
                                       AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(XkFeignFactoryBean.class);
        validate(attributes);
        definition.addPropertyValue("url", getUrl(attributes));
        definition.addPropertyValue("path", getPath(attributes));
        String name = getName(attributes);
        definition.addPropertyValue("name", name);
        definition.addPropertyValue("type", className);
        definition.addPropertyValue("decode404", attributes.get("decode404"));
        definition.addPropertyValue("fallback", attributes.get("fallback"));
        definition.addPropertyValue("fallbackFactory", attributes.get("fallbackFactory"));
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(true);

        String alias = getQualifier(attributes);
        BeanDefinitionHolder holder;
        if(StringUtils.hasText(alias)){
            holder = new BeanDefinitionHolder(beanDefinition, className, new String[] { alias });
        }else {
            holder = new BeanDefinitionHolder(beanDefinition, className);
        }

        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }
}
