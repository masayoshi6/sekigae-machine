package sekigae.sekigae.seatingapp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sekigae.sekigae.seatingapp.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

  // 名前で検索する（例：あいまい検索などに活用）
  List<Student> findByNameContaining(String keyword);

  // 性別で検索する
  List<Student> findByGender(String gender);

  // 座席位置で検索（ユニークな生徒を探す）
  Student findBySeatRowAndSeatColumn(Integer seatRow, Integer seatColumn);

  void deleteById(Integer id);

}
