package backend.repository;

import backend.model.QuizModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends MongoRepository<QuizModel, String> {
}
