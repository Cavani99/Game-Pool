package project.config;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;

import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfiguration {
    @Value("${paypal.client.id}")
    private String paypalClientId;

    @Value("${paypal.client.secret}")
    private String paypalClientSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public PaypalServerSdkClient paypalClient() {
        return new PaypalServerSdkClient.Builder()
                .loggingConfig(builder -> builder
                        .level(Level.DEBUG)
                        .requestConfig(logConfigBuilder -> logConfigBuilder.body(true))
                        .responseConfig(logConfigBuilder -> logConfigBuilder.headers(true)))
                .httpClientConfig(configBuilder -> configBuilder
                        .timeout(0))
                .environment(Environment.SANDBOX)
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(
                                paypalClientId,
                                paypalClientSecret
                        ).build()
                )
                .build();
    }
}
