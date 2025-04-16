package backend.repository;

import backend.model.CommunityModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CommunityRepository extends MongoRepository<CommunityModel, String> {
    Optional<CommunityModel> findByName(String name);
}
