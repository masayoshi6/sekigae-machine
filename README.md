# 席替えアプリ

## 📌はじめに

**席替えアプリ**は、教室や会議室などの座席割り当てを簡単に管理・実行できるWebアプリです。  
ユーザーは登録された受講生の座席表を閲覧・シャッフル・固定などの操作が可能です。  
学校現場やイベント運営など、座席管理が必要な場面で利用できます。
Spring Boot + MySQL + Thymeleaf を利用したWebアプリケーションです。

---

## 🎯 主な機能

- **現在の座席表の表示**
- **座席一覧表示**  
  登録された生徒情報を座席表形式で表示
- **ランダム席替え**  
  ボタンひとつで座席をランダムに再配置
- **座席固定**  
  特定の生徒の席を固定したまま席替え
- **重複チェック**  
  学籍番号や座席位置の重複を登録時に防止
- **登録・更新・削除**  
  生徒の追加や座席変更が可能
- **性別を考慮した席替え**（オプション）

  男女別に座席の列を分けることも可能です

---

## 技術スタック

- **言語**: Java 21
- **フレームワーク**: Spring Boot 3.2.x
    - Spring Web
    - Spring Data JPA
    - Thymeleaf
    - Validation
    - Actuator
- **DB**: MySQL 8.x（開発環境）、H2（テスト環境）
- **ビルドツール**: Gradle
- **その他ライブラリ**
    - Lombok
    - Apache Commons Lang / Collections
    - Jackson (JSON処理)
    - Testcontainers（統合テスト用）

---

### 開発環境

**使用技術**  
<img src="https://img.shields.io/badge/language-Java 21-007396.svg">
<img src="https://img.shields.io/badge/framework-springboot 3.4.3-6DB33F.svg?logo=springboot&logoColor=#000000">
<img src="https://img.shields.io/badge/-MySQL-4479A1.svg?logo=mysql&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-MyBatis-990000.svg">
<img src="https://img.shields.io/badge/-JUnit5-25A162.svg?logo=JUnit5&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-h2database-09476B.svg?logo=h2database&logoColor=FFFFFF">

**使用ツール**  
<img src="https://img.shields.io/badge/-IntelliJ IDEA-000000.svg?logo=intellijidea&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-Git-F05032.svg?logo=git&logoColor=F8A899">
<img src="https://img.shields.io/badge/-GitHub-181717.svg?logo=github&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-Postman-FF6C37.svg?logo=postman&logoColor=FFFFFF">
<img src="https://img.shields.io/badge/-OpenAPI-6BA539.svg?logo=openapiinitiative&logoColor=FFFFFF">

## 想定ユーザー

- 高校・中学校の教員

- 学校管理者

- 学級運営に携わる教育支援スタッフ

## 今後の改善予定

- 出席番号や成績に基づく配置条件の追加

- モバイル端末対応UI

- クラウド環境（AWS）でのデプロイ

---

# プロジェクトのクローン

git clone https://github.com/masayoshi6/sekigae-machine.git

cd sekigae

## 📂 アーキテクチャ図解

### クラス図

```mermaid
classDiagram
    class Student {
        +int id
        +String name
        +String studentNumber
        +int seatRow
        +int seatColumn
    }
    class Seat {
        +int row
        +int column
        +boolean isOccupied
    }
    class SeatingController {
        +listSeats()
        +shuffleSeats()
        +assignSeat()
    }
    class SeatingService {
        +List<Seat> getSeats()
        +void shuffleSeats()
    }
    class StudentRepository {
        +findAll()
        +save(Student student)
    }

    Student --> Seat
    SeatingController --> SeatingService
    SeatingService --> StudentRepository

    style Student fill:#f4d03f;stroke:#333;stroke-width:2px
    style Seat fill:#aed6f1;stroke:#333;stroke-width:2px
    style SeatingController fill:#f5b7b1;stroke:#333;stroke-width:2px
    style SeatingService fill:#abebc6;stroke:#333;stroke-width:2px
    style StudentRepository fill:#d7bde2;stroke:#333;stroke-width:2px

