package org.springframework.cloud.openfeign;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-10-22 09:03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@FeignClient
public @interface XkFeignClient {

    /**
     * The name of the service with optional protocol prefix. Synonym for {@link #name()
     * name}. A name must be specified for all clients, whether or not a url is provided.
     * Can be specified as property key, eg: ${propertyKey}.
     */
//    @AliasFor("name")
    @AliasFor(annotation = FeignClient.class)
    String value() default "";

    /**
     * The service id with optional protocol prefix. Synonym for {@link #value() value}.
     */
//    @AliasFor("value")
    @AliasFor(annotation = FeignClient.class)
    String name() default "";

    /**
     * Sets the <code>@Qualifier</code> value for the feign client.
     */
    @AliasFor(annotation = FeignClient.class)
    String qualifier() default "";

    /**
     * An absolute URL or resolvable hostname (the protocol is optional).
     */
    @AliasFor(annotation = FeignClient.class)
    String url() default "";

    /**
     * Whether 404s should be decoded instead of throwing FeignExceptions
     */
    @AliasFor(annotation = FeignClient.class)
    boolean decode404() default false;

    /**
     * A custom <code>@Configuration</code> for the feign client. Can contain override
     * <code>@Bean</code> definition for the pieces that make up the client, for instance
     * {@link feign.codec.Decoder}, {@link feign.codec.Encoder}, {@link feign.Contract}.
     *
     * @see org.springframework.cloud.netflix.feign.FeignClientsConfiguration for the defaults
     */
    @AliasFor(annotation = FeignClient.class)
    Class<?>[] configuration() default {};

    /**
     * Fallback class for the specified Feign client interface. The fallback class must
     * implement the interface annotated by this annotation and be a valid spring bean.
     * <br/>
     * 指定的class会自动加入springbean，所以对应类上不需要加入@Component等注解
     */
    @AliasFor(annotation = FeignClient.class)
    Class<?> fallback() default void.class;

    /**
     * Define a fallback factory for the specified Feign client interface. The fallback
     * factory must produce instances of fallback classes that implement the interface
     * annotated by {@link XkFeignClient}. The fallback factory must be a valid spring
     * bean.
     * <br/>
     * 指定的class会自动加入springbean，所以对应类上不需要加入@Component等注解
     *
     * @see feign.hystrix.FallbackFactory for details.
     */
    @AliasFor(annotation = FeignClient.class)
    Class<?> fallbackFactory() default void.class;

    /**
     * Path prefix to be used by all method-level mappings. Can be used with or without
     * <code>@RibbonClient</code>.
     */
    @AliasFor(annotation = FeignClient.class)
    String path() default "";

}
