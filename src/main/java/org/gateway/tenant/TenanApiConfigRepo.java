package org.gateway.tenant;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenanApiConfigRepo extends MongoRepository<TenantApiConfig,String> {

}
