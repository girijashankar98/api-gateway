package org.inventum.unifyng.apigateway.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document("tenant_api_config")
@AllArgsConstructor
@NoArgsConstructor
public class TenantApiConfig {

    @Id
    private String id;

    @Field("hash_secret")
    private String hashSecret;
}
