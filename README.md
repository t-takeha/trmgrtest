Name
===
trmgrtest

## Overview

Spring Batch + Spring Bootで以下を検証する。ライブラリでもなんでもありません。単なる検証PG。

* 複数の非XAデータソース＆非XAトランザクションマネージャの混在
* 複数のXAデータソースの混在、XAトランザクションマネージャによるトランザクション制御
* Spring Batch用の内部リポジトリトランザクションを切り離す構成の検証

### 構成
Spring Batch + Spring Boot + MyBatis + Atomikos

### DBMS
H2、Oracle18c Express Edition、DB2 ver11

## Description

### 起動クラス

* ApplicationMain
    * @SpringBootApplicationが付いた唯一のSpring Bootアプリケーションクラス。それだけ。

### 設定クラス

* DevComDataSourceConfig  
    * 非XAのデータソース
    * H2に接続
    * DataSource：DriverManagerDataSource（コネクションプールなし）
    * トランザクションマネージャ：DataSourceTransactionManager
    * O/Rマッパー：なし

* DevDb1DataSourceConfig  
    * 非XAのデータソース
    * Oracle18c Express Editionに接続。ストアードファンクション呼び出しテスト用DB。
    * DataSource：HikariDataSource（コネクションプールあり）
    * トランザクションマネージャ：DataSourceTransactionManager
    * O/Rマッパー：MyBatis
 
* DevDb2DataSourceConfig  
    * 非XAのデータソース
    * DB2 ver11に接続。テーブルへのINSERT/UPDATE/SELECTテスト用DB。
    * DataSource：HikariDataSource（コネクションプールあり）
    * トランザクションマネージャ：DataSourceTransactionManager
    * O/Rマッパー：MyBatis

* XaDb1DataSourceConfig  
    * XAのデータソース
    * 接続先は、DevDb1DataSourceConfigと同じ。
    * DataSource：AtomikosDataSourceBeanを使用（コネクションプールあり）
    * トランザクションマネージャはAtomikos用に別途定義
    * O/Rマッパー：MyBatis

* XaDb2DataSourceConfig  
    * XAのデータソース
    * 接続先は、DevDb2DataSourceConfigと同じ。
    * DataSource：AtomikosDataSourceBeanを使用（コネクションプールあり）
    * トランザクションマネージャはAtomikos用に別途定義
    * O/Rマッパー：MyBatis

* XaDb2DataSourceConfig  
    * XAのデータソース
    * 接続先は、H2
    * DataSource：AtomikosDataSourceBeanを使用（コネクションプールあり）
    * トランザクションマネージャはAtomikos用に別途定義
    * O/Rマッパー：MyBatis

* XaTransactionConfig
    * Atomikos用トランザクションマネージャの定義
    * UserTransactionServiceImp、UserTransaction、UserTransactionManager、JtaTransactionManagerを定義
        * 本当は、AtomikosJtaConfigurationをそのまま使いたかったのだが、
        非XA用に定義したトランザクションマネージャがいると動いてくれないので、自前で用意した。

* AppConfig
    * 唯一のジョブと3つのStep＆Taskletの定義

### Tasklet

* LocalDb1TransactionTestTasklet
    * 非XAのデータソースを使って、MyBatis経由でストアードファンクションを実行
    * トランザクションマネージャは、DevDb1DataSourceConfigで定義したものを指定

* LocalDb2TransactionTestTasklet
    * 非XAのデータソースを使って、MyBatis経由でINSERT/UPDATE/SELECTを実行
    * トランザクションマネージャは、DevDb2DataSourceConfigで定義したものを指定

* XaDbTransactionTestTasklet
    * XAのデータソースを使って、MyBatis経由でストアードファンクション＆INSERT/UPDATE/SELECTを実行
    * トランザクションマネージャは、XaTransactionConfigで定義したものを指定

### 設定ファイル

* application.yml
    * Atomikos用に特別なプロパティ（jta.propertiesみたいなもの）を用意しなくてもできる方式を用いたので、
    設定は全部ここに書いてあります。

### ディレクトリ構成

* logs/
    * アプリケーションが吐き出すログ
    * Gitに上がっているのは全構成をアクティブにして起動したときのもの

* transaction-logs
    * atomikosのトランザクションマネージャが吐き出すログ
    * 毎回消されて作り直されるのでGitには上げていません

* otherlibs
    * Oracle18c Express EdtionとDB2のJDBCドライバを格納。ファイル名はbuild.gradle参照。
    * OracleとIBMからそれぞれダウンロードしてくる必要あります
    * いずれのJarもDBインストール時に一緒に入ったものをそのまま使用。

## セットアップ

* OpenJDK11の下で動かしています。intelliJ 2019.01付属のものでOK
* otherlibsにOracleとDB2のJDBCドライバ（いずれもType4）を入れる
* gradle使うか、intellijに読ませてビルド
* H2のメンテナンス  
    * `java -cp h2-1.4.199.jar org.h2.tools.Server -webAllowOthers -baseDir "(H2のデータファイルがある親パス)" `
    * コンソールが出てきたらURLに`jdbc:h2:./xatestdb`と入力して接続

以上。
