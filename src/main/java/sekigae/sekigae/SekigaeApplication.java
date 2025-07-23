package sekigae.sekigae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 席替えアプリケーションのメインクラス
 * <p>
 * このアプリケーションは学校現場で使用する席替え機能を提供します。 主な機能: - 学生情報の管理 - 教室レイアウトの設定 - 様々な条件に基づく席替えの実行 - 席替え履歴の管理
 *
 * @author [あなたの名前]
 * @version 1.0.0
 * @since 2024-07-21
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "sekigae.sekigae.seatingapp.repository")
@EnableTransactionManagement
public class SekigaeApplication {

  /**
   * アプリケーションのエントリーポイント
   *
   * @param args コマンドライン引数
   */
  public static void main(String[] args) {
    SpringApplication.run(SekigaeApplication.class, args);

    // アプリケーション起動完了ログ
    System.out.println("=================================================");
    System.out.println("  席替えアプリケーションが正常に起動しました！");
    System.out.println("  アクセス URL: http://localhost:8080");
    System.out.println("=================================================");

  }

}