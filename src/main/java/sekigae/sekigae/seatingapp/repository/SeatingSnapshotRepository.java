package sekigae.sekigae.seatingapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sekigae.sekigae.seatingapp.entity.SeatingSnapshot;

@Repository
public interface SeatingSnapshotRepository extends JpaRepository<SeatingSnapshot, Long> {

  // 作成日時の降順でスナップショットを取得
  List<SeatingSnapshot> findAllByOrderByCreatedAtDesc();

  // スナップショット名で検索
  List<SeatingSnapshot> findBySnapshotNameContaining(String keyword);
}