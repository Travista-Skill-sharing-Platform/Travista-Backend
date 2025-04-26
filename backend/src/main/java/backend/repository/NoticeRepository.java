package backend.repository;

import backend.model.NoticeModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoticeRepository extends MongoRepository<NoticeModel, String> {
}
