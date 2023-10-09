package app.beautyminder.config.aws;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.sniff.SniffOnFailureListener;
import org.opensearch.client.sniff.Sniffer;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

/**
 * @author Seok Won
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "app.beautyminder.repository.elastic")
@Slf4j
public class OpenSearchRestClientConfiguration extends AbstractOpenSearchConfiguration {

    @Value("${aws.os.endpoint}")
    private String endpoint;

    @Value("${aws.os.region}")
    private String region;

    private AWSCredentialsProvider credentialsProvider = null;

    @Autowired
    public OpenSearchRestClientConfiguration(AWSCredentialsProvider provider) {
        credentialsProvider = provider;
    }

    /**
     * SpringDataOpenSearch data provides us the flexibility to implement our custom {@link RestHighLevelClient} instance by implementing the abstract method {@link AbstractOpenSearchConfiguration#opensearchClient()},
     *
     * @return RestHighLevelClient. Amazon OpenSearch Service Https rest calls have to be signed with AWS credentials, hence an interceptor {@link HttpRequestInterceptor} is required to sign every
     * API calls with credentials. The signing is happening through the below snippet
     * <code>
     * signer.sign(signableRequest, awsCredentialsProvider.getCredentials());
     * </code>
     */

    @Override
    @Bean
    public RestHighLevelClient opensearchClient() {
        AWS4Signer signer = new AWS4Signer();
        String serviceName = "es";
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);

        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(endpoint, 443, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.addInterceptorLast(interceptor))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout((int) Duration.ofSeconds(600).toMillis())
                        .setSocketTimeout((int) Duration.ofSeconds(300).toMillis()));

        SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
        restClientBuilder.setFailureListener(sniffOnFailureListener);

        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        Sniffer esSniffer = Sniffer.builder(client.getLowLevelClient())
                .setSniffIntervalMillis(999999999) // 11.574일 interval
                .setSniffAfterFailureDelayMillis(999999999)
                .build();

        sniffOnFailureListener.setSniffer(esSniffer);

        return client;
//        return RestClients.create(clientConfiguration).rest();
    }
}
