package org.inventum.unifyng.apigateway.tenant;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenanApiConfigRepo extends MongoRepository<TenantApiConfig,String> {

}
