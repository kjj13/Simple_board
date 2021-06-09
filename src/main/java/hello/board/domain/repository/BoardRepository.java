package hello.board.domain.repository;

import hello.board.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>{ //매핑할 Entity와 Id의 타입
}
