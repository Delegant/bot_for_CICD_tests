package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.IncomingMessage;

import java.util.Collection;

@Repository
public interface IncomingMessageRepository extends JpaRepository<IncomingMessage, Long> {

    public Collection<IncomingMessage> findAllByReadStatusEquals(String readStatus);
}
