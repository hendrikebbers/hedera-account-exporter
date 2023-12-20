package de.devlodge.hedera.account.export.repositories;

import de.devlodge.hedera.account.export.entities.TransactionEntity;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<TransactionEntity, UUID> {
}
