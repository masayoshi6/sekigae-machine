package sekigae.sekigae.seatingapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sekigae.sekigae.seatingapp.entity.SeatingSnapshotDetail;

@Repository
public interface SeatingSnapshotDetailRepository extends
    JpaRepository<SeatingSnapshotDetail, Long> {

  // 特定のスナップショットに関連する詳細データを取得
  List<SeatingSnapshotDetail> findBySnapshotId(Long snapshotId);

  // スナップショットIDで削除
  void deleteBySnapshotId(Long snapshotId);
}