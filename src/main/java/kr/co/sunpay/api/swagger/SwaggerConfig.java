package kr.co.sunpay.api.swagger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicates;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Value("${api.key-header-name}")
	private String headerKeyName;

	public static final Contact DEFAULT_CONTACT = new Contact("Sunpay", "http://sunpay.co.kr", "black.bum19@gmail.com");
	
	public static final ApiInfo DEFAULT_API_INFO = new ApiInfo("Sunpay API 1.0", "Sunpay API", "1.0", "urn:tos", DEFAULT_CONTACT, "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
	
	// What content type this API support.
	public static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<String>(java.util.Arrays.asList("application/json"));
	
	private ApiInfo apiInfo() {
		return new ApiInfo(
				"Sunpay API 1.0", 
				"Sunpay API", 
				"1.0", 
				"urn:tos", 
				DEFAULT_CONTACT, 
				"Apache 2.0", 
				"http://www.apache.org/licenses/LICENSE-2.0", 
				Collections.emptyList());
	}
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.paths(Predicates.not(PathSelectors.regex("/error.*")))
				.build()
				.apiInfo(apiInfo())
				.produces(DEFAULT_PRODUCES_AND_CONSUMES)
				.consumes(DEFAULT_PRODUCES_AND_CONSUMES)
				.globalOperationParameters(Arrays.asList(
						new ParameterBuilder()
							.name(headerKeyName)
							.description("Sunpay에서 발급받은 API Key")
							.modelRef(new ModelRef("string"))
							.parameterType("header")
							.required(true)
							.build()));
	}
	
	@Bean
	public UiConfiguration uiConfig() {
		return UiConfigurationBuilder.builder()
				.displayRequestDuration(true)
				.validatorUrl("")
				.build();
	}
}
